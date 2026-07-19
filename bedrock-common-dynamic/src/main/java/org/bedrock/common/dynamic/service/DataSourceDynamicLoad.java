package org.bedrock.common.dynamic.service;

import org.springframework.core.Ordered;

import javax.sql.DataSource;

/**
 * 数据源动态加载
 * 运行时加载
 */
@FunctionalInterface
public interface DataSourceDynamicLoad extends Ordered {

	/**
	 * 加载数据源
	 *
	 * @param dsKey 数据源key
	 * @return 数据源
	 */
	DataSource load(String dsKey);

	/**
	 * 默认优先级最低
	 */
	@Override
	default int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}
