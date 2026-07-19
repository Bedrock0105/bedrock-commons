package org.bedrock.common.authentication.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bedrock.common.authentication.model.OauthTokenResult;
import org.bedrock.common.code.util.StringUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class OauthTokenResultSerializer extends StdSerializer<OauthTokenResult> {

	// 必须提供无参构造函数（Jackson 反射调用）
	public OauthTokenResultSerializer() {
		super(OauthTokenResult.class);
	}

	@Override
	public void serialize(OauthTokenResult token, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		jgen.writeStartObject();
		jgen.writeStringField(OauthTokenResult.ACCESS_TOKEN, token.getAccessToken());
		jgen.writeStringField(OauthTokenResult.TOKEN_TYPE, token.getTokenType());
		String refreshToken = token.getRefreshToken();
		if (StringUtil.isNotBlank(refreshToken)) {
			jgen.writeStringField(OauthTokenResult.REFRESH_TOKEN, refreshToken);
		}
		long now = System.currentTimeMillis();
		Long expireTime = token.getExpireTime();
		if (expireTime != null) {
			jgen.writeNumberField(OauthTokenResult.EXPIRES_IN, (expireTime - now) / 1000);
		}
		Long refreshExpireTime = token.getRefreshExpireTime();
		if (refreshExpireTime != null) {
			jgen.writeNumberField(OauthTokenResult.REFRESH_EXPIRE_IN, (refreshExpireTime - now) / 1000);
		}
		Set<String> scope = token.getScope();
		if (scope != null && !scope.isEmpty()) {
			StringBuilder scopes = new StringBuilder();
			for (String s : scope) {
				scopes.append(s);
				scopes.append(" ");
			}
			jgen.writeStringField(OauthTokenResult.SCOPE, scopes.substring(0, scopes.length() - 1));
		}
		Map<String, Object> additionalInformation = token.getAdditionalInformation();
		for (String key : additionalInformation.keySet()) {
			jgen.writeObjectField(key, additionalInformation.get(key));
		}
		jgen.writeEndObject();
	}
}
