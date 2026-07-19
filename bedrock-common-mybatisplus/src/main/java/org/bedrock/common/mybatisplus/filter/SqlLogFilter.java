package org.bedrock.common.mybatisplus.filter;

import com.alibaba.druid.DbType;
import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.*;
import com.alibaba.druid.sql.SQLUtils;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.mybatisplus.props.MybatisPlusProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * sql 日志打印器
 */
@Slf4j
public class SqlLogFilter extends FilterEventAdapter {

	private final SQLUtils.FormatOption statementSqlFormatOption = new SQLUtils.FormatOption(false, false);

	private final MybatisPlusProperties properties;

	private static final String SQL_TEMPLATE = """

		┌─────────────────────────────────────────────────────
		│ 连接信息: conn-{} ({})
		│ 执行状态: ✅ 执行成功
		│ 执行时间: {} 毫秒
		│ SQL语句: {}
		└─────────────────────────────────────────────────────
		""";

	private DbType dbType;

	public SqlLogFilter(MybatisPlusProperties properties) {
		this.properties = properties;
	}

	@Override
	public void init(DataSourceProxy dataSource) {
		this.dbType = DbType.of(dataSource.getDbType());
	}

	/**
	 * sql 执行前 开始及时
	 */
	@Override
	protected void statementExecuteBefore(StatementProxy statement, String sql) {
		statement.setLastExecuteStartNano();
	}

	/**
	 * sql 执行前 开始及时
	 */
	@Override
	protected void statementExecuteQueryBefore(StatementProxy statement, String sql) {
		statement.setLastExecuteStartNano();
	}

	/**
	 * sql 执行前 开始及时
	 */
	@Override
	protected void statementExecuteUpdateBefore(StatementProxy statement, String sql) {
		statement.setLastExecuteStartNano();
	}

	/**
	 * sql 执行前 开始及时
	 */
	@Override
	protected void statementExecuteBatchBefore(StatementProxy statement) {
		statement.setLastExecuteStartNano();
	}

	@Override
	protected void statementExecuteBatchAfter(StatementProxy statement, int[] result) {
		String sql;
		if (statement instanceof PreparedStatementProxy) {
			sql = ((PreparedStatementProxy) statement).getSql();
		} else {
			sql = statement.getBatchSql();
		}

		logExecutableSql(statement, sql);
	}

	@Override
	protected void statementExecuteAfter(StatementProxy statement, String sql, boolean firstResult) {
		logExecutableSql(statement, sql);
	}

	@Override
	protected void statementExecuteQueryAfter(StatementProxy statement, String sql, ResultSetProxy resultSet) {
		logExecutableSql(statement, sql);
	}

	@Override
	protected void statementExecuteUpdateAfter(StatementProxy statement, String sql, int updateCount) {
		logExecutableSql(statement, sql);
	}

	private void logExecutableSql(StatementProxy statement, String sql) {
		statement.setLastExecuteTimeNano();
		/**
		 * sql 日志
		 */
		if (!properties.isSqlLog()) {
			return;
		}
		/**
		 * 忽略表
		 */
		if (isIgnoreTable(sql)) {
			return;
		}
		double nanos = statement.getLastExecuteTimeNano();
		double millis = nanos / (1000 * 1000);
		int parametersSize = statement.getParametersSize();
		if (parametersSize == 0) {
			/**
			 * sql格式化一下，避免换行
			 */
			String formattedSql = SQLUtils.format(sql, dbType, this.statementSqlFormatOption);
			log.info(SQL_TEMPLATE, statement.getConnectionProxy().getId(), stmtId(statement), millis, formattedSql);
			return;
		}

		List<Object> parameters = new ArrayList<>(parametersSize);
		for (int i = 0; i < parametersSize; ++i) {
			JdbcParameter jdbcParam = statement.getParameter(i);
			parameters.add(jdbcParam != null
				? jdbcParam.getValue()
				: null);
		}
		String formattedSql = SQLUtils.format(sql, dbType, parameters, this.statementSqlFormatOption);
		log.info(SQL_TEMPLATE, statement.getConnectionProxy().getId(), stmtId(statement), millis, formattedSql);

	}

	public boolean isIgnoreTable(String sql) {
		return properties.getIgnoreTable()
			.stream()
			.anyMatch(sql::contains);
	}

	private String stmtId(StatementProxy statement) {
		StringBuilder buf = new StringBuilder();
		if (statement instanceof CallableStatementProxy) {
			buf.append("cstmt-");
		} else if (statement instanceof PreparedStatementProxy) {
			buf.append("pstmt-");
		} else {
			buf.append("stmt-");
		}
		buf.append(statement.getId());
		return buf.toString();
	}

}
