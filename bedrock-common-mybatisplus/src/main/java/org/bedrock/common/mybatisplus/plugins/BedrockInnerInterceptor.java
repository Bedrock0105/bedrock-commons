package org.bedrock.common.mybatisplus.plugins;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.core.Ordered;

/**
 * 对mybatisplus内置插件进行扩展
 */
public interface BedrockInnerInterceptor extends Ordered {

	/**
	 * 执行 {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler, CacheKey, BoundSql)} 前
	 *
	 * @param executor
	 * @param ms
	 * @param parameter
	 * @param rowBounds
	 * @param resultHandler
	 * @param boundSql
	 */
	default void willDoQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
	}

	/**
	 * 执行 {@link Executor#update(MappedStatement, Object)} 前调用
	 *
	 * @param executor
	 * @param ms
	 * @param parameter
	 */
	default void willDoUpdate(Executor executor, MappedStatement ms, Object parameter) {
	}

	/**
	 * 获取执行顺序
	 * @return
	 */
	default int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}
