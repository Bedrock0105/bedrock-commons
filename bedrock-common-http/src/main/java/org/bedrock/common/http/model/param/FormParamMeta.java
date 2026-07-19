package org.bedrock.common.http.model.param;

import lombok.Getter;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.code.util.UriUtil;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.support.HttpSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 表单参数元数据容器
 * 管理表单参数及编码配置
 */
@Getter
public class FormParamMeta {

	/**
	 * 关联的HTTP请求实例
	 */
	private final HttpRequest request;

	/**
	 * 表单参数列表
	 */
	private List<Param> params = new ArrayList<>();

	/**
	 * 添加单个需编码的表单参数
	 *
	 * @param name  参数名
	 * @param value 参数值
	 */
	public void add(String name, Object value) {
		params.add(new Param(name, value));
	}

	/**
	 * 批量添加表单参数
	 *
	 * @param map 表单参数键值对集合
	 */
	public void add(Map<String, Object> map) {
		map.forEach(this::add);
	}

	/**
	 * 添加单个无需编码的表单参数
	 *
	 * @param name  参数名
	 * @param value 参数值
	 */
	public void addEncoded(String name, Object value) {
		params.add(new Param(name, value, false));
	}

	/**
	 * 批量添加无需编码的表单参数
	 *
	 * @param map 表单参数键值对集合
	 */
	public void addEncoded(Map<String, Object> map) {
		map.forEach(this::addEncoded);
	}

	/**
	 * 判断表单参数列表是否为空
	 *
	 * @return 空返回true，非空返回false
	 */
	public boolean isEmpty() {
		return params.isEmpty();
	}

	// ===================== 构造方法与请求关联方法 =====================

	/**
	 * 构造参数元数据实例
	 *
	 * @param request 关联的HTTP请求实例
	 */
	public FormParamMeta(HttpRequest request) {
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
		StringJoiner sj = new StringJoiner("&");
		for (Param param : params) {
			String name = param.name();
			// 处理单个有效名称参数
			if (StringUtil.isNotBlank(name)) {
				String convertedValue = HttpSupport.paramConverter(request, name, param.value());
				if (param.encoded()) {
					convertedValue = UriUtil.encode(convertedValue);
				}
				sj.add(name + "=" + convertedValue);
				continue;
			}
			// 处理结构化参数，转换为Map后批量添加
			Map<String, Object> paramMap = request.getConverter().convertToMultiValueMap(param.value(), request);
			paramMap.forEach((k, v) -> {
				String valueStr = StringUtil.toStr(v, "");
				if (param.encoded()) {
					valueStr = UriUtil.encode(valueStr);
				}
				sj.add(k + "=" + valueStr);
			});
		}
		return sj.toString();
	}
}
