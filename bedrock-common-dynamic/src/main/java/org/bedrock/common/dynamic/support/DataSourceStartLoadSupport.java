package org.bedrock.common.dynamic.support;

import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import lombok.RequiredArgsConstructor;
import org.bedrock.common.code.util.CollectionUtil;
import org.bedrock.common.dynamic.service.DataSourceStartLoad;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DataSourceStartLoadSupport implements InitializingBean {

	private final List<DataSourceStartLoad> dataSourceStartLoads;

	/**
	 * 加载数据源
	 */
	public Map<String, DataSource> load(Statement statement,
										DefaultDataSourceCreator creator,
										DynamicDataSourceProperties properties) {
		return dataSourceStartLoads.stream()
			.map(dataSourceStartLoad -> dataSourceStartLoad.load(statement, creator, properties))
			.filter(CollectionUtil::isNotEmpty)
			.flatMap(List::stream)
			.collect(Collectors.toMap(
				DataSourceStartLoad.DataSourceLoad::dsKey,
				DataSourceStartLoad.DataSourceLoad::dataSource,
				(key1, key2) -> key2
			));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		dataSourceStartLoads.sort(Comparator.comparingInt(DataSourceStartLoad::getOrder));
	}
}
