package org.bedrock.common.resource.service.sms.impl;

import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.util.JsonUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.resource.model.sms.SmsProperties;
import org.bedrock.common.resource.model.sms.SmsResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 七牛云短信实现
 * <p>
 * 依赖：com.qiniu:qiniu-java-sdk
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * apiKey: your-access-key
 * apiSecret: your-secret-key
 * signature: 您的签名
 * templateId: your-template-id
 * </pre>
 * </p>
 */
@Slf4j
public class QiniuSmsTemplate extends AbstractSmsTemplate {

	private static final String DEFAULT_ENDPOINT = "https://sms.qiniuapi.com";

	private final Auth auth;
	private final Client client;
	private final String endpoint;

	/**
	 * 通过工厂注入七牛云 Auth
	 */
	public QiniuSmsTemplate(Auth auth, SmsProperties smsProperties) {
		super(smsProperties);
		this.auth = auth;
		this.client = new Client();
		this.endpoint = StringUtil.isBlank(smsProperties.getEndpoint()) ? DEFAULT_ENDPOINT : smsProperties.getEndpoint();
	}

	@Override
	public SmsResponse send(Map<String, Object> params, Collection<String> phones) {
		try {
			for (String phone : phones) {
				Map<String, Object> body = new HashMap<>(4);
				body.put("template_id", smsProperties.getTemplateId());
				body.put("mobile", phone);
				if (params != null && !params.isEmpty()) {
					body.put("parameters", params);
				}
				String url = endpoint + "/v1/message/single";
				byte[] bodyBytes = JsonUtil.toJsonBytes(body);
				StringMap headers = auth.authorizationV2(url, "POST", bodyBytes, "application/json");
				Response response = client.post(url, bodyBytes, headers, "application/json");
				if (!response.isOK()) {
					String errorMsg = response.bodyString();
					log.error("七牛云短信发送失败：{}", errorMsg);
					return fail(response.statusCode, errorMsg);
				}
			}
			return success("发送成功");
		} catch (Exception e) {
			log.error("七牛云短信发送异常：{}", e.getMessage(), e);
			return fail(500, e.getMessage());
		}
	}

}
