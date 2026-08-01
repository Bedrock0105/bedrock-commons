package org.bedrock.common.crypto.resolver;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.RequiredArgsConstructor;
import org.bedrock.common.code.util.AnnotationUtil;
import org.bedrock.common.code.util.JsonUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.crypto.algorithm.CryptoAlgorithm;
import org.bedrock.common.crypto.algorithm.CryptoAlgorithmRegistry;
import org.bedrock.common.crypto.annotation.ApiDecrypt;
import org.bedrock.common.crypto.annotation.ApiExcludeCrypto;
import org.bedrock.common.crypto.enums.CryptoAlgorithmEnum;
import org.bedrock.common.crypto.props.CryptoProperties;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Parameter;
import java.util.Collection;

/**
 * Query / Form 参数解密解析器。
 * <p>
 * 从配置的统一参数名（默认 {@code params}）读取密文，解密后按方法参数类型做 JSON 反序列化绑定。
 * 当方法或参数存在 {@link ApiDecrypt}（含 {@code @ApiCrypto} 元注解），且未被
 * {@link ApiExcludeCrypto} 排除时生效。
 * </p>
 *
 * @see HandlerMethodArgumentResolver
 */
@RequiredArgsConstructor
public class ApiDecryptParamResolver implements HandlerMethodArgumentResolver {

	private final CryptoProperties properties;

	private final CryptoAlgorithmRegistry registry;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return needDecrypt(parameter);
	}

	@Nullable
	@Override
	public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer,
								  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		Parameter parameter = methodParameter.getParameter();

		// 参数级注解优先，否则取方法/类上的解密注解
		ApiDecrypt apiDecrypt = AnnotatedElementUtils.getMergedAnnotation(parameter, ApiDecrypt.class);
		if (apiDecrypt == null) {
			apiDecrypt = AnnotationUtil.findElementAnnotation(methodParameter.getMethod(), ApiDecrypt.class);
		}

		String text = webRequest.getParameter(properties.getParamName());
		if (StringUtil.isBlank(text)) {
			return null;
		}

		Class<? extends CryptoAlgorithm> algorithmClass = resolveAlgorithmClass(apiDecrypt);
		CryptoAlgorithm algorithm = registry.get(algorithmClass);
		byte[] decrypt = algorithm.decrypt(text, apiDecrypt.key(), apiDecrypt.publicKey(), apiDecrypt.privateKey());
		JsonNode jsonNode = JsonUtil.parseJsonNode(decrypt);
		if (jsonNode == null) {
			return null;
		}

		Class<?> type = parameter.getType();

		// JSON 为对象且目标为简单类型时，按参数名取字段（枚举 / Number 子类用 isAssignableFrom）
		if (jsonNode.getNodeType() == JsonNodeType.OBJECT && isSimpleBindType(type)) {
			return JsonUtil.convertValue(jsonNode.get(parameter.getName()), type);
		}

		// 集合参数且 JSON 为对象时，从同名字段取数组/集合节点
		if (jsonNode.getNodeType() == JsonNodeType.OBJECT && Collection.class.isAssignableFrom(type)) {
			jsonNode = jsonNode.get(parameter.getName());
		}

		JavaType javaType = TypeFactory.defaultInstance().constructType(methodParameter.getGenericParameterType());
		return JsonUtil.convertValue(jsonNode, javaType);
	}

	/**
	 * 是否可从 JSON 对象中按参数名取单字段绑定的简单类型。
	 * <p>
	 * 使用 {@link Class#isAssignableFrom} 识别枚举、{@link Number} 及其数组元素，
	 * 避免 {@code List.contains(Enum.class)} 无法匹配业务枚举子类的问题。
	 * </p>
	 *
	 * @param type 参数类型
	 * @return {@code true} 表示按字段名绑定
	 */
	private static boolean isSimpleBindType(Class<?> type) {
		if (ClassUtils.isPrimitiveOrWrapper(type) || type == String.class) {
			return true;
		}
		if (type.isArray()) {
			Class<?> component = type.getComponentType();
			return ClassUtils.isPrimitiveOrWrapper(component)
				|| component == String.class
				|| Enum.class.isAssignableFrom(component)
				|| Number.class.isAssignableFrom(component);
		}
		return Enum.class.isAssignableFrom(type) || Number.class.isAssignableFrom(type);
	}

	/**
	 * 判断当前参数是否需要解密。
	 * <p>存在 {@link ApiExcludeCrypto} 时返回 {@code false}；否则方法/参数带 {@link ApiDecrypt} 时返回 {@code true}。</p>
	 *
	 * @param parameter 方法参数
	 * @return {@code true} 表示需要解密
	 */
	public boolean needDecrypt(MethodParameter parameter) {
		if (AnnotationUtil.isParameterAnnotation(parameter.getParameter(), ApiExcludeCrypto.class)
			|| AnnotationUtil.isAnnotation(parameter.getMethod(), ApiExcludeCrypto.class)) {
			return false;
		}
		return AnnotationUtil.isElementAnnotation(parameter.getMethod(), ApiDecrypt.class)
			|| AnnotatedElementUtils.hasAnnotation(parameter.getParameter(), ApiDecrypt.class);
	}

	/**
	 * 解析算法实现 Class：注解 handler &gt; 注解 algorithm 枚举 &gt; 配置。
	 *
	 * @param annotation 解密注解（可为 null 时由调用方保证）
	 * @return 算法实现类型
	 */
	private Class<? extends CryptoAlgorithm> resolveAlgorithmClass(ApiDecrypt annotation) {
		Class<? extends CryptoAlgorithm> handler = annotation.handler();
		if (handler != CryptoAlgorithm.class) {
			return handler;
		}
		CryptoAlgorithmEnum algorithm = annotation.algorithm();
		handler = algorithm.getAlgorithmClass();
		if (handler != CryptoAlgorithm.class) {
			return handler;
		}
		return properties.getAlgorithmClass();
	}
}
