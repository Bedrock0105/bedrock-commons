package org.bedrock.common.dynamic.service;

import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import org.springframework.core.Ordered;

import javax.sql.DataSource;
import java.sql.Statement;
import java.util.List;

/**
 * 数据源启动项目时加载
 */
@FunctionalInterface
public interface DataSourceStartLoad extends Ordered {

	/**
	 * 加载数据源
	 *
	 * @param dsKey 数据源key
	 * @return 数据源
	 */
	List<DataSourceLoad> load(Statement statement,
							  DefaultDataSourceCreator creator,
							  DynamicDataSourceProperties properties);

	/**
	 * 默认优先级最低
	 */
	@Override
	default int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	/**
	 * 数据源加载
	 *
	 * @param dsKey      数据源key
	 * @param dataSource 数据源
	 */
	record DataSourceLoad(String dsKey, DataSource dataSource) {

	}

}
