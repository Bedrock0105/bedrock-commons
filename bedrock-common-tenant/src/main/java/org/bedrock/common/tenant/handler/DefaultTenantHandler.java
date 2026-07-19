package org.bedrock.common.tenant.handler;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.tenant.props.TenantProperties;
import org.springframework.boot.CommandLineRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class DefaultTenantHandler implements TenantLineHandler, CommandLineRunner {

	private final Set<String> defaultIncludes = new HashSet<>();

	private final TenantProperties properties;

	@Override
	public Expression getTenantId() {
		return new StringValue(TenantHandler.getTenantId());
	}

	@Override
	public String getTenantIdColumn() {
		return properties.getTenantColumn();
	}

	@Override
	public boolean ignoreTable(String tableName) {
		if (!properties.isEnabled()) {
			return true;
		}
		if (TenantHandler.isIgnore()) {
			return true;
		}
//		if (AuthUtil.isAdministrator()) {
//			return true;
//		}
		if (StringUtil.isBlank(TenantHandler.getTenantId())) {
			return true;
		}
		if (properties.getExcludes().stream().anyMatch(tableName::equalsIgnoreCase)) {
			return true;
		}
		return defaultIncludes.stream().noneMatch(tableName::equalsIgnoreCase);
	}

	@Override
	public void run(String... args) throws Exception {
		try {
			/**
			 * 没有租户id的字段 排除多租户查询
			 */
			List<TableInfo> tableInfos = TableInfoHelper.getTableInfos();
			tableInfos.forEach(tableInfo -> {
				if (defaultIncludes.contains(tableInfo.getTableName()) || properties.getExcludes().contains(tableInfo.getTableName())) {
					return;
				}
				for (TableFieldInfo tableFieldInfo : tableInfo.getFieldList()) {
					if (tableFieldInfo.getColumn().equalsIgnoreCase(properties.getTenantColumn())) {
						defaultIncludes.add(tableInfo.getTableName());
						return;
					}
				}
			});
		} catch (Exception ignored) {
		}
	}
}
