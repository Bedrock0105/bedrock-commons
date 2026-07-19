package org.bedrock.common.http.model.param;

import lombok.Getter;
import org.bedrock.common.code.util.JsonUtil;
import org.bedrock.common.http.HttpRequest;

import java.util.Map;

/**
 * 请求体参数元数据容器
 * 管理单个对象/键值对两种请求体数据，二选一使用，优先bodyMap
 */
@Getter
public class BodyParamMeta {

	/**
	 * 关联的HTTP请求实例
	 */
	private final HttpRequest request;

	/**
	 * 单个请求体对象（如POJO、JSON字符串）
	 */
	private Object body;

	/**
	 * 请求体键值对映射（表单/JSON对象键值对）
	 */
	private Map<String, Object> bodyMap;

	/**
	 * 设置单个请求体对象
	 *
	 * @param body 单个请求体数据对象
	 */
	public void add(Object body) {
		this.body = body;
	}

	/**
	 * 批量添加请求体键值对
	 *
	 * @param bodyMap 请求体键值对集合
	 */
	public void add(Map<String, Object> bodyMap) {
		bodyMap.forEach(this::add);
	}

	/**
	 * 添加单个请求体键值对
	 *
	 * @param name  参数名
	 * @param value 参数值
	 */
	public void add(String name, Object value) {
		if (bodyMap == null) {
			bodyMap = new java.util.HashMap<>();
		}
		bodyMap.put(name, value);
	}

	/**
	 * 判断是否设置了单个请求体对象
	 *
	 * @return 是返回true，否返回false
	 */
	public boolean isBody() {
		return body != null;
	}

	/**
	 * 判断是否存在请求体键值对
	 *
	 * @return 是返回true，否返回false
	 */
	public boolean isBodyMap() {
		return bodyMap != null && !bodyMap.isEmpty();
	}
	// ===================== 构造方法与请求关联方法 =====================

	/**
	 * 构造参数元数据实例
	 *
	 * @param request 关联的HTTP请求实例
	 */
	public BodyParamMeta(HttpRequest request) {
		this.request = request;
	}

	/**
	 * 获取关联的HTTP请求实例
	 *
	 * @return HttpRequest实例
	 */
	public HttpRequest request() {
		return request;
	}

	@Override
	public String toString() {
		if (isBodyMap()) {
			return JsonUtil.toJson(bodyMap);
		}
		return JsonUtil.toJson(body);
	}
}
