package org.bedrock.common.datascope.plugins;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.bedrock.common.auth.entity.AuthUser;
import org.bedrock.common.auth.util.AuthUtil;
import org.bedrock.common.datascope.annotation.DataScope;
import org.bedrock.common.datascope.handler.DataScopeAuthHolder;
import org.bedrock.common.datascope.model.DataScopeModel;
import org.bedrock.common.datascope.props.DataScopeProperties;
import org.bedrock.common.datascope.support.DataPermissionSqlProcessor;
import org.bedrock.common.mybatisplus.plugins.BedrockInnerInterceptor;
import org.bedrock.common.code.util.*;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据权限插件：借鉴MyBatis-Plus分页插件的核心实现逻辑（如MyBatis拦截器机制、SQL动态改写），用于实现基于角色/用户的数据访问范围控制
 *
 * <p>核心定位：
 * 参考MyBatis-Plus分页插件的"SQL拦截→解析→改写"链路，实现数据权限的自动化处理——通过拦截Mapper方法执行，
 * 根据当前用户的角色权限动态解析并修改SQL，自动追加数据范围过滤条件（如WHERE dept_id IN (1,2,3)），最终实现"不同权限用户仅能访问其权限范围内的数据"。
 *
 * <p>设计参考：
 * 主要借鉴MyBatis-Plus分页插件的实现思路（参考其拦截器设计、SQL解析器架构），包括：
 * <ul>
 *   <li>利用MyBatis的Interceptor接口拦截StatementHandler，在SQL执行前介入处理；
 *   <li>通过SQL解析器（如JSqlParser）分析原始SQL结构，精准定位WHERE子句位置；
 *   <li>基于权限规则动态生成过滤条件，并改写SQL完成拼接（类似分页插件追加分页条件的逻辑）。
 * </ul>
 * （参考思路实现文章：https://juejin.cn/post/6875248363592220679）
 *
 * <p>核心功能：
 * <ul>
 *   <li>权限规则匹配：根据当前用户角色+Mapper方法标识（mapperId）匹配预定义的数据权限规则（如部门范围、用户可见性）；
 *   <li>SQL动态改写：自动向查询SQL追加数据范围条件（支持单表查询、关联查询等复杂场景）；
 *   <li>多数据源兼容：适配MySQL、Oracle等不同数据库的SQL语法差异（类似分页插件的方言适配）；
 *   <li>无侵入集成：无需修改原始Mapper接口和SQL，通过拦截器机制实现权限控制，保持业务代码纯净。
 * </ul>
 *
 * <p>适用场景：
 * 需精细化控制数据访问范围的业务系统（如多部门协同系统中，限制用户仅能查看本部门数据；多角色系统中，管理员查看全部数据而普通用户仅查看本人数据）。
 */
@RequiredArgsConstructor
public class DataScopeInnerInterceptor implements BedrockInnerInterceptor {

	private static final Map<String, DataScope> DATA_SCOPE_MAP = new ConcurrentHashMap<>();

	private final DataScopeProperties dataScopeProperties;

	private final DataPermissionSqlProcessor dataPermissionSqlProcessor;

	@Override
	public void willDoQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
		/**
		 * 仅对查询操作进行数据权限过滤
		 */
		if (SqlCommandType.SELECT != ms.getSqlCommandType()
			|| StatementType.CALLABLE == ms.getStatementType()) {
			return;
		}
		/**
		 * 禁用数据权限
		 */
		if (!dataScopeProperties.isEnabled()) {
			return;
		}
		/**
		 * 获取当前用户信息
		 */
		AuthUser authUser = AuthUtil.getAuthUser();
		if (authUser == null) {
			return;
		}
		/**
		 * 管理员无需数据权限过滤
		 */
		if (AuthUtil.isAdmin() || AuthUtil.isAdministrator()) {
			return;
		}

		DataScope dataScope = findDataAuthAnnotation(ms);
		if (dataScope == null) {
			return;
		}
		/**
		 * 注解上面的mapperId
		 * 优先使用注解里面的mapperId
		 */
		String mapperId = StringUtil.isNotBlank(dataScope.mapperId()) ? dataScope.mapperId() : ms.getId();
		DataScopeModel dataScopeModel = new DataScopeModel(mapperId, dataScope);
		boolean auth = DataScopeAuthHolder.isAuth();
		DataScopeAuthHolder.setIsAuth(false);
		String condition = dataPermissionSqlProcessor.sqlCondition(dataScopeModel, authUser, boundSql.getSql());
		DataScopeAuthHolder.setIsAuth(auth);
		if (StringUtil.isNotBlank(condition)) {
			PluginUtils.mpBoundSql(boundSql)
				.sql(condition);
		}
	}

	/**
	 * 获取数据权限注解信息
	 *
	 * @param mappedStatement mappedStatement
	 * @return DataAuth
	 */
	private DataScope findDataAuthAnnotation(MappedStatement mappedStatement) {
		String id = mappedStatement.getId();
		return DATA_SCOPE_MAP.computeIfAbsent(id, k -> {
			//获取mapper名称
			String className = id.substring(0, id.lastIndexOf("."));

			Class<?> aClass = ClassUtil.forName(className);
			if (aClass == null) {
				return null;
			}
			//获取方法名
			String methodName = id.substring(id.lastIndexOf(".") + 1);
			//获取当前mapper 的方法
			Method method = ReflectionUtil.findMethod(aClass, methodName);
			if (method == null) {
				return null;
			}
			return AnnotationUtils.findAnnotation(method, DataScope.class);
		});

	}

	@Override
	public int getOrder() {
		return 1;
	}

}
