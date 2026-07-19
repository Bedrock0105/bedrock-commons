package org.bedrock.common.dynamic.support;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import lombok.RequiredArgsConstructor;
import org.bedrock.common.dynamic.service.DataSourceDynamicLoad;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class DataSourceDynamicLoadSupport implements InitializingBean {

	private final List<DataSourceDynamicLoad> dataSourceDynamicLoads;

	private final DataSource dataSource;

	/**
	 * 加载数据源
	 */
	public void load(String dsKey) {
		if (isExist(dsKey)) {
			return;
		}
		dataSourceDynamicLoads.stream()
			.map(dynamicLoad -> dynamicLoad.load(dsKey)) // 映射为load方法的返回值
			.filter(Objects::nonNull) // 过滤掉null结果
			.findFirst() // 取第一个非null的结果（短路操作，找到即停止）
			.ifPresent(load -> addDataSource(dsKey, load)); // 若存在则执行添加操作
	}

	/**
	 * 判断数据源是否存在
	 */
	public boolean isExist(String dsKey) {
		if (dataSource instanceof DynamicRoutingDataSource dynamicRoutingDataSource) {
			return dynamicRoutingDataSource.getDataSources().containsKey(dsKey);
		}
		return false;
	}

	/**
	 * 添加数据源
	 */
	public void addDataSource(String dsKey, DataSource dataSource) {
		if (dataSource instanceof DynamicRoutingDataSource dynamicRoutingDataSource) {
			dynamicRoutingDataSource.addDataSource(dsKey, dataSource);
		}
	}

	public void removeDataSource(String dsKey) {
		if (dataSource instanceof DynamicRoutingDataSource dynamicRoutingDataSource) {
			dynamicRoutingDataSource.removeDataSource(dsKey);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		dataSourceDynamicLoads.sort(Comparator.comparingInt(DataSourceDynamicLoad::getOrder));
	}
}
