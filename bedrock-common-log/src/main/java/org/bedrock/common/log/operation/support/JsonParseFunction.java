package org.bedrock.common.log.operation.support;

import org.bedrock.common.code.util.JsonUtil;

public class JsonParseFunction implements IParseFunction {

	@Override
	public String functionName() {
		return "TO_JSON";
	}

	@Override
	public String apply(Object value) {
		return JsonUtil.toJson(value);
	}
}
