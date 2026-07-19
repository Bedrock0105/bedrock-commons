package org.bedrock.common.http.support;

import org.bedrock.common.code.reflects.MethodMeta;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.annotation.LogConf;
import org.bedrock.common.http.annotation.Param;
import org.bedrock.common.http.converter.ParameterConverter;
import org.bedrock.common.http.props.HttpClientProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * 工具方法
 */
public class HttpSupport {

	/**
	 * 获取方法参数
	 */
	public static Map<String, Integer> getMethodParamsMap(MethodMeta methodMeta) {
		Map<String, Integer> params = new HashMap<>();
		for (MethodParameter parameter : methodMeta.getParameters()) {
			Param param = parameter.getParameterAnnotation(Param.class);
			if (param != null) {
				params.put(param.value(), parameter.getParameterIndex());
				continue;
			}
			params.put(parameter.getParameterName(), parameter.getParameterIndex());
		}
		return params;
	}

	/**
	 * 解析占位符 只能在spring容器中才会替换
	 */
	public static String resolvePlaceholders(String str) {
		ApplicationContext applicationContext = SpringUtil.getApplicationContext();
		if (applicationContext != null) {
			return applicationContext.getEnvironment().resolvePlaceholders(str);
		}
		return str;
	}

	public static String resolvePlaceholdersParam(String str, HttpRequest request) {
		// 1. 获取请求参数索引映射、方法参数元数据
		Map<String, Integer> placeholderIndexMap = request.getArgsMap();
		Map<Integer, MethodParameter> methodParamMap = request.getMethodMeta().getParametersMap();

		// 2. 解析占位符，适配参数值
		return StringUtil.format(str, placeholderKey -> {
			// 获取占位符对应的参数索引
			Integer paramIndex = placeholderIndexMap.get(placeholderKey);
			if (paramIndex == null) {
				return "";
			}
			// 获取方法参数元数据（防护空指针）
			MethodParameter methodParameter = methodParamMap.get(paramIndex);
			if (methodParameter == null) {
				return "";
			}
			// 获取原始参数值
			Object paramValue = request.args(paramIndex);
			// 遍历转换器，匹配并解析参数值
			for (ParameterConverter converter : request.getParameterConverters()) {
				if (converter.supportsParameter(methodParameter, placeholderKey, paramValue)) {
					return StringUtil.toStr(converter.resolveArgument(methodParameter, placeholderKey, paramValue), "");
				}
			}
			// 无匹配转换器，转为字符串默认值
			return StringUtil.toStr(paramValue, "");
		});
	}

	/**
	 * 解析字符串中的占位符
	 * 基于请求参数转换器适配参数值，无匹配转换器时转为字符串默认值
	 */
	public static String resolvePlaceholders(String str, HttpRequest request) {
		return resolvePlaceholdersParam(resolvePlaceholders(str), request);
	}

	/**
	 * 参数转换
	 *
	 * @param request HTTP请求上下文
	 * @param key     参数名
	 * @param value   参数值
	 * @return 转换后的参数值
	 */
	public static String paramConverter(HttpRequest request, String key, Object value) {
		if (value == null) {
			return null;
		}
		MethodParameter parameter = null;
		for (int i = 0; i < request.args().length; i++) {
			if (request.args(i) == value) {
				parameter = request.getMethodMeta().getParametersMap().get(i);
				break;
			}
		}
		for (ParameterConverter parameterConverter : request.getParameterConverters()) {
			if (parameterConverter.supportsParameter(parameter, key, value)) {
				return parameterConverter.resolveArgument(parameter, key, value);
			}
		}
		return value.toString();
	}

	/**
	 * 构建日志配置
	 *
	 * @param logConf 日志配置注解
	 * @return 日志配置对象
	 */
	public static HttpClientProperties.Log buildLog(LogConf logConf) {
		HttpClientProperties.Log log = new HttpClientProperties.Log();
		log.setEnabled(logConf.enabled());
		log.setRequestLog(logConf.requestLog());
		log.setRequestHeaderLog(logConf.requestHeaderLog());
		log.setRequestBodyLog(logConf.requestBodyLog());
		log.setResponseLog(logConf.responseLog());
		log.setResponseHeaderLog(logConf.responseHeaderLog());
		log.setResponseBodyLog(logConf.responseBodyLog());
		return log;
	}

}
