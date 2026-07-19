package org.bedrock.common.dynamic.provider;

import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.exception.CannotFindDataSourceException;
import com.baomidou.dynamic.datasource.provider.AbstractDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.dynamic.datasource.toolkit.DsStrUtils;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.util.CollectionUtil;
import org.bedrock.common.dynamic.support.DataSourceStartLoadSupport;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;

@Slf4j
public class DefaultAbstractJdbcDataSourceProvider extends AbstractDataSourceProvider {

	/**
	 * 配置文件
	 */
	private final DynamicDataSourceProperties dynamicDataSourceProperties;

	/**
	 * 创建数据源
	 */
	private final DefaultDataSourceCreator defaultDataSourceCreator;

	/**
	 * 启动加载数据源
	 */
	private final DataSourceStartLoadSupport support;

	/**
	 * 构造
	 */
	public DefaultAbstractJdbcDataSourceProvider(DefaultDataSourceCreator defaultDataSourceCreator,
												 DataSourceStartLoadSupport support,
												 DynamicDataSourceProperties dynamicDataSourceProperties) {
		super(defaultDataSourceCreator);
		this.defaultDataSourceCreator = defaultDataSourceCreator;
		this.dynamicDataSourceProperties = dynamicDataSourceProperties;
		this.support = support;
	}

	/**
	 * 关闭资源
	 *
	 * @param con 资源
	 */
	private static void closeResource(AutoCloseable con) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException ex) {
				log.debug("Could not close ", ex);
			} catch (Throwable ex) {
				log.debug("Unexpected exception on closing", ex);
			}
		}
	}

	@Override
	public Map<String, DataSource> loadDataSources() {
		Connection conn = null;
		Statement stmt = null;
		try {
			DataSourceProperty dataSourceProperty = dynamicDataSourceProperties.getDatasource().get(dynamicDataSourceProperties.getPrimary());
			if (dataSourceProperty == null) {
				throw new CannotFindDataSourceException("dynamic-datasource can not find primary datasource");
			}
			// 由于 SPI 的支持，现在已无需显示加载驱动了
			// 但在用户显示配置的情况下，进行主动加载
			if (!DsStrUtils.isEmpty(dataSourceProperty.getDriverClassName())) {
				Class.forName(dataSourceProperty.getDriverClassName());
				log.info("成功加载数据库驱动程序");
			}
			conn = DriverManager.getConnection(dataSourceProperty.getUrl(), dataSourceProperty.getUsername(), dataSourceProperty.getPassword());
			log.info("成功获取数据库连接");
			stmt = conn.createStatement();
			Map<String, DataSourceProperty> dataSourcePropertiesMap = createDataSourceMap();
			if (CollectionUtil.isEmpty(dataSourcePropertiesMap)) {
				return support.load(stmt, defaultDataSourceCreator, dynamicDataSourceProperties);
			}
			Map<String, DataSource> dataSourceMap = createDataSourceMap(dataSourcePropertiesMap);
			Map<String, DataSource> load = support.load(stmt, defaultDataSourceCreator, dynamicDataSourceProperties);
			dataSourceMap.putAll(load);
			return dataSourceMap;
		} catch (Exception e) {
			log.error("loadDataSources error", e);
		} finally {
			closeResource(conn);
			closeResource(stmt);
		}
		return null;
	}

	/**
	 * 创建数据源 配置文件
	 */
	protected Map<String, DataSourceProperty> createDataSourceMap() {
		return Collections.emptyMap();
	}
}
