package org.bedrock.common.mybatisplus.plugins;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.bedrock.common.code.util.CollectionUtil;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class MybatisCustomizeInnerInterceptor implements InnerInterceptor {

	private final List<BedrockInnerInterceptor> interceptors;

	public MybatisCustomizeInnerInterceptor(List<BedrockInnerInterceptor> interceptors) {
		interceptors.sort(Comparator.comparingInt(BedrockInnerInterceptor::getOrder));
		this.interceptors = interceptors;
	}

	@Override
	public boolean willDoQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
		if (CollectionUtil.isEmpty(interceptors)) {
			return true;
		}
		interceptors.forEach(interceptor -> interceptor.willDoQuery(executor, ms, parameter, rowBounds, resultHandler, boundSql));
		return true;
	}

	@Override
	public boolean willDoUpdate(Executor executor, MappedStatement ms, Object parameter) throws SQLException {
		if (CollectionUtil.isEmpty(interceptors)) {
			return true;
		}
		interceptors.forEach(interceptor -> interceptor.willDoUpdate(executor, ms, parameter));
		return true;
	}
}
