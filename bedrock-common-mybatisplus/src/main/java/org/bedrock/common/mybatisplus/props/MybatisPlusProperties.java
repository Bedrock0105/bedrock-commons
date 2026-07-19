package org.bedrock.common.mybatisplus.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@ConfigurationProperties(prefix = MybatisPlusProperties.PREFIX)
public class MybatisPlusProperties {

	public static final String PREFIX = "bedrock.mybatis-plus";

	/**
	 * 是否打印sql
	 */
	private boolean sqlLog = true;

	/**
	 * 是否打印sql颜色
	 */
	private boolean sqlLogColor = true;

	/**
	 * 忽略打印sql日志的表
	 */
	private Set<String> ignoreTable = Set.of();

	/**
	 * 溢出总页数后是否进行处理
	 */
	private boolean overflow = false;

	/**
	 * 单页分页条数限制
	 */
	private Long maxLimit = 1000L;

	/**
	 * 开启左连接优化，优化 mybatis-plus right join
	 */
	private boolean optimizeJoin = false;

}
