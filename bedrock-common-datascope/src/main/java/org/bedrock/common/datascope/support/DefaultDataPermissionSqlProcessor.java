package org.bedrock.common.datascope.support;

import com.baomidou.mybatisplus.extension.parser.JsqlParserGlobal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperationList;
import org.bedrock.common.auth.entity.AuthUser;
import org.bedrock.common.auth.util.AuthUtil;
import org.bedrock.common.datascope.enums.DataScopeTypeEnum;
import org.bedrock.common.datascope.model.DataScopeModel;
import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.util.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class DefaultDataPermissionSqlProcessor implements DataPermissionSqlProcessor {

	private final ScopeModelSupport scopeModelSupport;

	@Override
	public String sqlCondition(DataScopeModel dataScope, AuthUser authUser, String originalSql) {

		//根据mapperId从数据库中获取对应模型
		DataScopeModel dataScopeDbs = scopeModelSupport.selectDataScopeById(dataScope.getMapperId(), authUser.getRoleIds());
		//未从数据库找到对应配置则采用默认
		dataScope = (dataScopeDbs != null) ? dataScopeDbs : dataScope;

		//判断数据权限类型并组装对应Sql
		DataScopeTypeEnum type = Objects.requireNonNull(dataScope).getScopeType();
		Set<Long> ids = new HashSet<>();
		String whereSql = "where scope.{} in ({})";
		if (DataScopeTypeEnum.DATA_SCOPE_ALL == type || AuthUtil.isAdministrator() || AuthUtil.isAdmin()) {
			return null;
		} else if (DataScopeTypeEnum.DATA_SCOPE_CUSTOM_AUTH == type) {
			Set<Long> longs = scopeModelSupport.selectAuthHandlerUserId(dataScope, authUser);
			if (CollectionUtil.isEmpty(longs)) {
				return null;
			}
			ids.addAll(longs);
		} else if (DataScopeTypeEnum.DATA_SCOPE_CUSTOM == type) {
			whereSql = PlaceholderUtil.resolveByMap(PlaceholderUtil.DEFAULT_PLACEHOLDER_PREFIX,
				PlaceholderUtil.DEFAULT_PLACEHOLDER_SUFFIX, dataScope.getScopeValue(), BeanUtil.beanToMap(authUser));
		} else if (DataScopeTypeEnum.DATA_SCOPE_SELF == type) {
			ids.add(authUser.getUserId());
		} else if (DataScopeTypeEnum.DATA_SCOPE_DEPT == type) {
			ids.addAll(authUser.getDeptIds());
		} else if (DataScopeTypeEnum.DATA_SCOPE_DEPT_AND_CHILD == type) {
			List<Long> deptIds = authUser.getDeptIds();
			deptIds.forEach(deptId -> {
				Set<Long> deptChildList = scopeModelSupport.selectDeptChildById(deptId);
				ids.addAll(deptChildList);
			});
		}
		try {
			return createSql(originalSql, dataScope.getScopeField(), dataScope.getScopeColumn(), ids, type, whereSql);
		} catch (Exception e) {
			log.error("解析sql发生异常,采取原始写法", e);
		}
		return StringUtil.format("select {} from ({}) scope " + whereSql,
			StringUtil.toStr(dataScope.getScopeField(), "*"),
			originalSql,
			dataScope
				.getScopeColumn()
				.replaceFirst("(.+(?<=\\.))", ""),
			ids.stream()
				.map(String::valueOf)
				.collect(Collectors.joining(StringPool.COMMA)));
	}

	/**
	 * @param sql                                 原始sql（要处理的sql）
	 * @param scopeField                          要查询的字段
	 * @param fieldName（DataScopeEnum非自定义的时候条件字段）
	 * @param whereSegment                        DataScopeEnum非自定义的时候条件字段的值
	 * @param type                                （权限类型 是否是自定义判断）
	 * @param whereSql（类型是自定义的时候要拼接的where）
	 * @Discription 生成新的sql
	 */
	private static String createSql(String sql,
									String scopeField,
									String fieldName,
									Set<Long> whereSegment,
									DataScopeTypeEnum type,
									String whereSql) throws JSQLParserException {
		/**
		 * 解析查询的字段和where条件
		 * 查询是*的时候不做处理
		 *
		 */
		PlainSelect ps = null;
		List<SelectItem<?>> selectItems = null;
		if (!(StringUtil.toStr(scopeField, "*").equals("*"))) {
			String scopeSql = StringUtil.format("select {} from scope {}", scopeField, StringUtil.toStr(whereSql, ""));
			ps = ((Select) JsqlParserGlobal.parse(scopeSql)).getPlainSelect();
			selectItems = ps.getSelectItems();
		}
		/**
		 * 解析原始的sql，进行数据权限条件的拼接，和修改要查询的字段（如果有的话）
		 */
		Select statement = (Select) JsqlParserGlobal.parse(sql);
		/**
		 * 判断是否是非自定义sql查询
		 */
		if (type != DataScopeTypeEnum.DATA_SCOPE_CUSTOM) {
			if (statement instanceof PlainSelect) {
				return createSqlByPlainSelect(statement.getPlainSelect(), selectItems, fieldName, whereSegment).toString();
			} else if (statement instanceof SetOperationList) {
				return createSqlBySetOperationList(statement.getSetOperationList(), selectItems, fieldName, whereSegment).toString();
			}
		}
		/**
		 * 自定义sql处理
		 */
		if (StringUtil.isBlank(whereSql)) {
			return sql;
		}
		/**
		 * 如果查询条件是*的时候就是上面没有解析sql  要重新解析查询条件
		 */
		if (ps == null) {
			String scopeSql = "select * from scope " + whereSql;
			ps = ((Select) JsqlParserGlobal.parse(scopeSql)).getPlainSelect();
		}

		Expression where = ps.getWhere();
		if (statement instanceof PlainSelect) {
			return createSqlByPlainSelect((PlainSelect) statement, selectItems, where).toString();
		} else if (statement instanceof SetOperationList) {
			return createSqlBySetOperationList((SetOperationList) statement, selectItems, where).toString();
		}
		return sql;
	}

	/**
	 * 创建条件sql
	 */
	private static PlainSelect createSqlByPlainSelect(PlainSelect plainSelect,
													  List<SelectItem<?>> selectItems,
													  String fieldName,
													  Set<Long> whereSegment) {
		/**
		 * 合并查询条件
		 */
		Expression where = andExpression(plainSelect.getWhere(), fieldName, whereSegment);
		plainSelect.setWhere(where);
		/**
		 * 判断是否有新的要查询的字段
		 */
		if (CollectionUtil.isNotEmpty(selectItems)) {
			plainSelect.setSelectItems(selectItems);
		}

		return plainSelect;
	}

	/**
	 * 创建条件sql
	 */
	private static SetOperationList createSqlBySetOperationList(SetOperationList setOperationList,
																List<SelectItem<?>> selectItems,
																String fieldName,
																Set<Long> whereSegment) {
		List<Select> selects = setOperationList.getSelects();
		for (Select select : selects) {
			if (select instanceof PlainSelect) {
				createSqlByPlainSelect((PlainSelect) select, selectItems, fieldName, whereSegment);
			}
		}
		return setOperationList;
	}

	/**
	 * 创建条件sql
	 */
	private static PlainSelect createSqlByPlainSelect(PlainSelect plainSelect,
													  List<SelectItem<?>> selectItems,
													  Expression whereExpression) {
		/**
		 * 合并查询条件
		 */
		Expression where = andExpression(plainSelect.getWhere(), whereExpression);
		plainSelect.setWhere(where);
		/**
		 * 判断是否有新的要查询的字段
		 */
		if (CollectionUtil.isNotEmpty(selectItems)) {
			plainSelect.setSelectItems(selectItems);
		}
		return plainSelect;
	}

	/**
	 * 创建条件sql
	 */
	private static SetOperationList createSqlBySetOperationList(SetOperationList setOperationList,
																List<SelectItem<?>> selectItems,
																Expression whereExpression) {
		List<Select> selects = setOperationList.getSelects();
		for (Select select : selects) {
			if (select instanceof PlainSelect) {
				createSqlByPlainSelect((PlainSelect) select, selectItems, whereExpression);
			}
		}
		return setOperationList;
	}

	private static Expression andExpression(Expression where, String fieldName, Set<Long> whereSegment) {
		//获得where条件表达式
		Expression expression = buildTableExpression(fieldName, whereSegment);
		return andExpression(where, expression);
	}

	private static Expression andExpression(Expression where, Expression expression) {
		if (expression == null) {
			return where;
		}
		/**
		 *  判断expression 是否是OrExpression
		 *  如果是，则需要添加括号
		 */
		if (expression instanceof OrExpression) {
			expression = new ParenthesedExpressionList<>(expression);
		}
		if (where != null) {
			if (where instanceof OrExpression) {
				return new AndExpression(new ParenthesedExpressionList<>(where), expression);
			} else {
				return new AndExpression(where, expression);
			}
		}
		return expression;
	}

	private static Expression buildTableExpression(String fieldName, Set<Long> whereSegment) {
		if (CollectionUtil.isEmpty(whereSegment)) {
			return null;
		}
		//获得条件表达式
		if (whereSegment.size() == 1) {
			return new EqualsTo(new Column(fieldName), new LongValue(whereSegment.iterator().next()));
		}

		ExpressionList<LongValue> expressionList = new ExpressionList<>(whereSegment.stream().map(LongValue::new).collect(Collectors.toList()));
		return new InExpression(new Column(fieldName), ParenthesedExpressionList.from(expressionList));
	}
}
