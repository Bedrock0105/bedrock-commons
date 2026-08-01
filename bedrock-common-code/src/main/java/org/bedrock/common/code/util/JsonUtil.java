package org.bedrock.common.code.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class JsonUtil {

	/**
	 * 检查对象是否可以被序列化为 JSON
	 *
	 * @param obj 待检查对象
	 * @return true 可以转 JSON，false 不可以
	 */
	public static boolean canConvertToJson(Object obj) {
		if (obj == null) return true; // null 可以转为 "null"

		return getInstance().canSerialize(obj.getClass());
	}

	/**
	 * java 转 jsonString
	 */
	public static String toJson(Object obj) {
		try {
			return getInstance().writeValueAsString(obj);
		} catch (Exception e) {
			log.error("json 转换异常", e);
			return null;
		}
	}

	/**
	 * java 转 bytes
	 */
	public static byte[] toJsonBytes(Object obj) {
		try {
			return getInstance().writeValueAsBytes(obj);
		} catch (Exception e) {
			log.error("json 转换异常", e);
			return null;
		}
	}

	/**
	 * json 转 javaType
	 */
	public static <T> T parse(String json, JavaType type) {
		try {
			return getInstance().readValue(json, type);
		} catch (Exception e) {
			log.error("json 转换异常", e);
			return null;
		}
	}

	/**
	 * byts数组 转 java
	 */
	public static <T> T parse(byte[] bytes, JavaType type) {
		try {
			return getInstance().readValue(bytes, type);
		} catch (Exception e) {
			log.error("json 转换异常", e);
			return null;
		}
	}

	/**
	 * byts数组 转 java
	 */
	public static <T> T parse(byte[] bytes, Class<T> clazz) {
		try {
			return getInstance().readValue(bytes, clazz);
		} catch (Exception e) {
			log.error("json 转换异常", e);
			return null;
		}
	}

	/**
	 * json 转 java
	 */
	public static <T> T parse(String json, Class<T> clazz) {
		try {
			return getInstance().readValue(json, clazz);
		} catch (Exception e) {
			log.error("json 转换异常", e);
			return null;
		}
	}

	/**
	 * json 转 java
	 */
	public static <T> T parse(String json, TypeReference<T> typeReference) {
		try {
			return getInstance().readValue(json, typeReference);
		} catch (Exception e) {
			log.error("json 转换异常", e);
			return null;
		}
	}

	/**
	 * json 转 java
	 */
	public static <T> T parse(byte[] json, TypeReference<T> typeReference) {
		try {
			return getInstance().readValue(json, typeReference);
		} catch (Exception e) {
			log.error("json 转换异常", e);
			return null;
		}
	}

	/**
	 * json 转 list 对象
	 */
	public static <T> List<T> parseList(String json, Class<T> clazz) {
		try {
			return getInstance().readValue(json, getCollectionType(clazz));
		} catch (Exception e) {
			log.error("json 转换异常", e);
			return null;
		}
	}

	/**
	 * json 转 list 对象
	 */
	public static <T> List<T> parseList(byte[] json, Class<T> clazz) {
		try {
			return getInstance().readValue(json, getCollectionType(clazz));
		} catch (Exception e) {
			log.error("json 转换异常", e);
			return null;
		}
	}

	/**
	 * json 转 JsonNode 对象
	 */
	public static JsonNode parseJsonNode(String json) {
		try {
			return getInstance().readTree(json);
		} catch (Exception e) {
			log.error("json 转换异常", e);
			return null;
		}
	}

	/**
	 * json 转 JsonNode 对象
	 */
	public static JsonNode parseJsonNode(byte[] json) {
		try {
			return getInstance().readTree(json);
		} catch (Exception e) {
			log.error("json 转换异常", e);
			return null;
		}
	}

	/**
	 * json 转 map
	 */
	public static <V> Map<String, V> parseMap(String json, Class<V> valueType) {
		return parseMap(json, String.class, valueType);
	}

	/**
	 * json 转 map
	 */
	public static <K, V> Map<K, V> parseMap(String json, Class<K> keyType, Class<V> valueType) {
		return parse(json, getMapType(keyType, valueType));
	}

	/**
	 * 获取MapType
	 */
	public static <K, V> MapType getMapType(Class<K> keyType, Class<V> valueType) {
		return getInstance().getTypeFactory().constructMapType(Map.class, keyType, valueType);
	}

	/**
	 * 对象转换
	 */
	public static <T> T convertValue(Object value, Class<T> toValueType) {
		return getInstance().convertValue(value, toValueType);
	}

	/**
	 * 对象转换
	 */
	public static <T> T convertValue(Object value, TypeReference<T> toValueType) {
		return getInstance().convertValue(value, toValueType);
	}

	/**
	 * 对象转换
	 */
	public static <T> T convertValue(Object value, JavaType toValueType) {
		return getInstance().convertValue(value, toValueType);
	}

	/**
	 * 对象转换
	 */
	public static JsonNode convertValue(Object value) {
		return getInstance().valueToTree(value);
	}

	/**
	 * 对象转换
	 */
	public static <T> T convertValue(TreeNode node, Class<T> valueType) {
		try {
			return getInstance().treeToValue(node, valueType);
		} catch (JsonProcessingException e) {
			log.error("json 转换异常", e);
			return null;
		}
	}

	/**
	 * 获取CollectionType
	 */
	public static <E> CollectionType getCollectionType(Class<E> valueType) {
		return getInstance().getTypeFactory().constructCollectionType(List.class, valueType);
	}

	/**
	 * 获取CollectionType
	 */
	public static <T> JavaType getJavaType(Type valueType) {
		return getInstance().getTypeFactory().constructType(valueType);
	}

	/**
	 * 获取ObjectMapper
	 */
	public static ObjectMapper getInstance() {
		return JsonHolder.INSTANCE;
	}

	/**
	 * 创建ObjectMapper
	 */
	private static final class JsonHolder {

		private static final ObjectMapper INSTANCE = createObjectMapper();

		private static ObjectMapper createObjectMapper() {
			ObjectMapper mapper = Jackson2ObjectMapperBuilder
				.json()
				.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
				.simpleDateFormat(DateUtil.PATTERN_DATETIME)
				.build();
			mapper.findAndRegisterModules();
			return mapper;
		}
	}
}
