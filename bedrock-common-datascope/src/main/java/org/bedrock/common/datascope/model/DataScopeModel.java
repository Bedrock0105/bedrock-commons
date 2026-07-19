package org.bedrock.common.datascope.model;

import lombok.Getter;
import lombok.Setter;
import org.bedrock.common.datascope.annotation.DataScope;
import org.bedrock.common.datascope.annotation.DataScopeFilter;
import org.bedrock.common.datascope.enums.DataScopeTypeEnum;
import org.bedrock.common.code.constant.StringPool;

import java.io.Serializable;

@Setter
@Getter
public class DataScopeModel implements Serializable {

	/**
	 * mapperId
	 */
	private String mapperId;

	/**
	 * 数据权限类型
	 */
	private DataScopeTypeEnum scopeType = DataScopeTypeEnum.DATA_SCOPE_ALL;

	/**
	 * 数据权限字段
	 */
	private String scopeColumn = "dept_id";

	/**
	 * 可见字段
	 */
	private String scopeField = StringPool.ASTERISK;

	/**
	 * 数据权限规则值域
	 */
	private String scopeValue;

	public DataScopeModel() {

	}

	public DataScopeModel(String mapperId, DataScope dataScope) {
		this.mapperId = mapperId;
		this.scopeType = dataScope.type();
		this.scopeColumn = dataScope.column();
		this.scopeField = dataScope.field();
		this.scopeValue = dataScope.value();
	}

	public DataScopeModel(String mapperId, DataScopeFilter dataScopeFilter) {
		this.mapperId = mapperId;
		this.scopeType = dataScopeFilter.type();
		this.scopeColumn = dataScopeFilter.column();
		this.scopeField = dataScopeFilter.field();
		this.scopeValue = dataScopeFilter.value();
	}
}
