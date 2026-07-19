package org.bedrock.common.http.model.param;

import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.model.ContentType;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import java.util.Collections;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 统一管理HTTP各类请求参数元数据（Query/Form/Body/MultipartForm/BinaryData）
 */
public class ParamMeta {

	/**
	 * 关联的HTTP请求实例
	 */
	private final HttpRequest request;

	/**
	 * Query参数元数据（懒加载）
	 */
	private QueryParamMeta queryParamMeta;

	/**
	 * 普通表单参数元数据（懒加载）
	 */
	private FormParamMeta formParamMeta;

	/**
	 * 多部分表单参数元数据（懒加载）
	 */
	private MultipartFormMeta multipartFormMeta;

	/**
	 * 请求体参数元数据（懒加载）
	 */
	private BodyParamMeta bodyParamMeta;

	/**
	 * 二进制数据参数元数据（懒加载）
	 */
	private BinaryDataParamMeta binaryDataParamMeta;

	// ===================== Query参数相关方法 =====================

	/**
	 * 添加单个需URL编码的Query参数
	 *
	 * @param name  参数名
	 * @param value 参数值
	 */
	public ParamMeta addQuery(String name, Object value) {
		if (queryParamMeta == null) {
			queryParamMeta = new QueryParamMeta(request);
		}
		queryParamMeta.add(name, value);
		return this;
	}

	/**
	 * 批量添加Query参数（空指针防护）
	 *
	 * @param map 参数键值对集合
	 */
	public ParamMeta addQuery(Map<String, Object> map) {
		if (map == null || map.isEmpty()) {
			return this;
		}
		map.forEach(this::addQuery);
		return this;
	}

	/**
	 * 添加单个无需URL编码的Query参数
	 *
	 * @param name  参数名
	 * @param value 参数值
	 */
	public ParamMeta addQueryEncoded(String name, Object value) {
		if (queryParamMeta == null) {
			queryParamMeta = new QueryParamMeta(request);
		}
		queryParamMeta.addEncoded(name, value);
		return this;
	}

	/**
	 * 批量添加无需URL编码的Query参数（空指针防护）
	 *
	 * @param map 参数键值对集合
	 */
	public ParamMeta addQueryEncoded(Map<String, Object> map) {
		if (map == null || map.isEmpty()) {
			return this;
		}
		map.forEach(this::addQueryEncoded);
		return this;
	}

	/**
	 * 判断Query参数是否为空
	 *
	 * @return 空返回true，非空返回false
	 */
	public boolean isQueryEmpty() {
		return queryParamMeta == null || queryParamMeta.isEmpty();
	}

	/**
	 * 获取Query参数元数据
	 *
	 * @return QueryParamMeta实例
	 */
	public QueryParamMeta queryParamMeta() {
		if (queryParamMeta == null) {
			queryParamMeta = new QueryParamMeta(request);
		}
		return queryParamMeta;
	}

	/**
	 * 获取不可修改的Query参数列表
	 *
	 * @return Query参数列表
	 */
	public List<Param> queryParams() {
		if (queryParamMeta == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(queryParamMeta.getParams());
	}

	// ===================== 普通Form参数相关方法 =====================

	/**
	 * 添加单个需编码的普通表单参数
	 *
	 * @param name  参数名
	 * @param value 参数值
	 */
	public ParamMeta addForm(String name, Object value) {
		if (formParamMeta == null) {
			formParamMeta = new FormParamMeta(request);
		}
		formParamMeta.add(name, value);
		return this;
	}

	/**
	 * 批量添加普通表单参数（空指针防护）
	 *
	 * @param map 表单参数键值对集合
	 */
	public ParamMeta addForm(Map<String, Object> map) {
		if (map == null || map.isEmpty()) {
			return this;
		}
		map.forEach(this::addForm);
		return this;
	}

	/**
	 * 添加单个无需编码的普通表单参数
	 *
	 * @param name  参数名
	 * @param value 参数值
	 */
	public ParamMeta addFormEncoded(String name, Object value) {
		if (formParamMeta == null) {
			formParamMeta = new FormParamMeta(request);
		}
		formParamMeta.addEncoded(name, value);
		return this;
	}

	/**
	 * 批量添加无需编码的普通表单参数（空指针防护）
	 *
	 * @param map 表单参数键值对集合
	 */
	public ParamMeta addFormEncoded(Map<String, Object> map) {
		if (map == null || map.isEmpty()) {
			return this;
		}
		map.forEach(this::addFormEncoded);
		return this;
	}

	/**
	 * 判断普通表单参数是否为空
	 *
	 * @return 空返回true，非空返回false
	 */
	public boolean isFormEmpty() {
		return formParamMeta == null || formParamMeta.isEmpty();
	}

	/**
	 * 获取普通表单参数元数据
	 *
	 * @return FormParamMeta实例
	 */
	public FormParamMeta formParamMeta() {
		if (formParamMeta == null) {
			formParamMeta = new FormParamMeta(request);
		}
		return formParamMeta;
	}

	/**
	 * 获取不可修改的普通表单参数列表
	 *
	 * @return Form参数列表
	 */
	public List<Param> formParams() {
		if (formParamMeta == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(formParamMeta.getParams());
	}

	// ===================== Body参数相关方法 =====================

	/**
	 * 设置单个请求体对象
	 *
	 * @param body 单个请求体数据对象
	 */
	public ParamMeta addBody(Object body) {
		if (bodyParamMeta == null) {
			bodyParamMeta = new BodyParamMeta(request);
		}
		bodyParamMeta.add(body);
		return this;
	}

	/**
	 * 批量添加请求体键值对（空指针防护）
	 *
	 * @param bodyMap 请求体键值对集合
	 */
	public ParamMeta addBody(Map<String, Object> bodyMap) {
		if (bodyMap == null || bodyMap.isEmpty()) {
			return this;
		}
		bodyMap.forEach(this::addBody);
		return this;
	}

	/**
	 * 添加单个请求体键值对
	 *
	 * @param name  参数名
	 * @param value 参数值
	 */
	public ParamMeta addBody(String name, Object value) {
		if (bodyParamMeta == null) {
			bodyParamMeta = new BodyParamMeta(request);
		}
		bodyParamMeta.add(name, value);
		return this;
	}

	/**
	 * 判断是否设置了单个请求体对象
	 *
	 * @return 是返回true，否返回false
	 */
	public boolean isBody() {
		return bodyParamMeta != null && bodyParamMeta.isBody();
	}

	/**
	 * 判断是否存在请求体键值对
	 *
	 * @return 是返回true，否返回false
	 */
	public boolean isBodyMap() {
		return bodyParamMeta != null && bodyParamMeta.isBodyMap();
	}

	/**
	 * 获取请求体数据（优先返回键值对，再返回单个对象）
	 *
	 * @return 请求体数据
	 */
	public Object getBodyData() {
		if (bodyParamMeta == null) {
			return null;
		}
		if (bodyParamMeta.isBodyMap()) {
			return bodyParamMeta.getBodyMap();
		}
		return bodyParamMeta.getBody();
	}

	/**
	 * 获取请求体参数元数据
	 *
	 * @return BodyParamMeta实例
	 */
	public BodyParamMeta bodyParamMeta() {
		if (bodyParamMeta == null) {
			bodyParamMeta = new BodyParamMeta(request);
		}
		return bodyParamMeta;
	}

	// ===================== MultipartForm参数相关方法 =====================

	/**
	 * 添加单个普通多部分表单参数
	 *
	 * @param name  参数名
	 * @param value 参数值
	 */
	public ParamMeta addMultipartForm(String name, Object value) {
		addMultipartForm(name, value, ContentType.TEXT_PLAIN);
		return this;
	}

	/**
	 * 添加带内容类型的普通多部分表单参数
	 *
	 * @param name        参数名
	 * @param value       参数值
	 * @param contentType 内容类型
	 */
	public ParamMeta addMultipartForm(String name, Object value, String contentType) {
		addMultipartForm(name, null, value, contentType);
		return this;
	}

	/**
	 * 添加带内容类型的普通多部分表单参数
	 *
	 * @param name        参数名
	 * @param value       参数值
	 * @param contentType 内容类型
	 */
	public ParamMeta addMultipartForm(String name, String fileName, Object value, String contentType) {
		if (multipartFormMeta == null) {
			multipartFormMeta = new MultipartFormMeta(request);
		}
		multipartFormMeta.add(name, fileName, value, contentType);
		return this;
	}

	/**
	 * 添加字节数组类型多部分表单参数（默认二进制流）
	 *
	 * @param name  参数名
	 * @param bytes 字节数组数据
	 */
	public ParamMeta addMultipartForm(String name, byte[] bytes) {
		addMultipartForm(name, bytes, ContentType.OCTET_STREAM);
		return this;
	}

	/**
	 * 添加带文件名的字节数组多部分表单参数（默认二进制流）
	 *
	 * @param name     参数名
	 * @param fileName 文件名
	 * @param bytes    字节数组数据
	 */
	public ParamMeta addMultipartForm(String name, String fileName, byte[] bytes) {
		addMultipartForm(name, fileName, bytes, ContentType.OCTET_STREAM);
		return this;
	}

	/**
	 * 添加带内容类型的字节数组多部分表单参数
	 *
	 * @param name        参数名
	 * @param bytes       字节数组数据
	 * @param contentType 内容类型
	 */
	public ParamMeta addMultipartForm(String name, byte[] bytes, String contentType) {
		if (multipartFormMeta == null) {
			multipartFormMeta = new MultipartFormMeta(request);
		}
		multipartFormMeta.add(name, bytes, contentType);
		return this;
	}

	/**
	 * 添加带文件名和内容类型的字节数组多部分表单参数
	 *
	 * @param name        参数名
	 * @param fileName    文件名
	 * @param bytes       字节数组数据
	 * @param contentType 内容类型
	 */
	public ParamMeta addMultipartForm(String name, String fileName, byte[] bytes, String contentType) {
		if (multipartFormMeta == null) {
			multipartFormMeta = new MultipartFormMeta(request);
		}
		multipartFormMeta.add(name, fileName, bytes, contentType);
		return this;
	}

	/**
	 * 添加文件类型多部分表单参数（默认二进制流）
	 *
	 * @param name 参数名
	 * @param file 文件对象
	 */
	public ParamMeta addMultipartForm(String name, File file) {
		addMultipartForm(name, file, ContentType.OCTET_STREAM);
		return this;
	}

	/**
	 * 添加带文件名的文件多部分表单参数（默认二进制流）
	 *
	 * @param name     参数名
	 * @param fileName 文件名
	 * @param file     文件对象
	 */
	public ParamMeta addMultipartForm(String name, String fileName, File file) {
		addMultipartForm(name, fileName, file, ContentType.OCTET_STREAM);
		return this;
	}

	/**
	 * 添加带内容类型的文件多部分表单参数
	 *
	 * @param name        参数名
	 * @param file        文件对象
	 * @param contentType 内容类型
	 */
	public ParamMeta addMultipartForm(String name, File file, String contentType) {
		if (multipartFormMeta == null) {
			multipartFormMeta = new MultipartFormMeta(request);
		}
		multipartFormMeta.add(name, file, contentType);
		return this;
	}

	/**
	 * 添加带文件名和内容类型的文件多部分表单参数
	 *
	 * @param name        参数名
	 * @param fileName    文件名
	 * @param file        文件对象
	 * @param contentType 内容类型
	 */
	public ParamMeta addMultipartForm(String name, String fileName, File file, String contentType) {
		if (multipartFormMeta == null) {
			multipartFormMeta = new MultipartFormMeta(request);
		}
		multipartFormMeta.add(name, fileName, file, contentType);
		return this;
	}

	/**
	 * 添加输入流类型多部分表单参数（默认二进制流）
	 *
	 * @param name        参数名
	 * @param inputStream 输入流对象
	 */
	public ParamMeta addMultipartForm(String name, InputStream inputStream) {
		addMultipartForm(name, inputStream, ContentType.OCTET_STREAM);
		return this;
	}

	/**
	 * 添加带文件名的输入流多部分表单参数（默认二进制流）
	 *
	 * @param name        参数名
	 * @param fileName    文件名
	 * @param inputStream 输入流对象
	 */
	public ParamMeta addMultipartForm(String name, String fileName, InputStream inputStream) {
		addMultipartForm(name, fileName, inputStream, ContentType.OCTET_STREAM);
		return this;
	}

	/**
	 * 添加带内容类型的输入流多部分表单参数
	 *
	 * @param name        参数名
	 * @param inputStream 输入流对象
	 * @param contentType 内容类型
	 */
	public ParamMeta addMultipartForm(String name, InputStream inputStream, String contentType) {
		if (multipartFormMeta == null) {
			multipartFormMeta = new MultipartFormMeta(request);
		}
		multipartFormMeta.add(name, inputStream, contentType);
		return this;
	}

	/**
	 * 添加带文件名和内容类型的输入流多部分表单参数
	 *
	 * @param name        参数名
	 * @param fileName    文件名
	 * @param inputStream 输入流对象
	 * @param contentType 内容类型
	 */
	public ParamMeta addMultipartForm(String name, String fileName, InputStream inputStream, String contentType) {
		if (multipartFormMeta == null) {
			multipartFormMeta = new MultipartFormMeta(request);
		}
		multipartFormMeta.add(name, fileName, inputStream, contentType);
		return this;
	}

	/**
	 * 判断多部分表单参数是否为空
	 *
	 * @return 空返回true，非空返回false
	 */
	public boolean isMultipartFormEmpty() {
		return multipartFormMeta == null || multipartFormMeta.isEmpty();
	}

	/**
	 * 获取多部分表单参数元数据
	 *
	 * @return MultipartFormMeta实例
	 */
	public MultipartFormMeta multipartFormMeta() {
		if (multipartFormMeta == null) {
			multipartFormMeta = new MultipartFormMeta(request);
		}
		return multipartFormMeta;
	}

	/**
	 * 获取不可修改的多部分表单参数列表
	 *
	 * @return MultipartForm参数列表
	 */
	public List<MultipartFormMeta.Param> multipartFormParams() {
		if (multipartFormMeta == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(multipartFormMeta.getParams());
	}

	// ===================== BinaryData参数相关方法（集成BinaryDataParamMeta） =====================

	/**
	 * 添加二进制参数（默认文件名为null，默认内容类型为文本类型）
	 *
	 * @param name  参数名称
	 * @param value 二进制数据值（支持File、byte[]、InputStream等类型）
	 * @return 当前ParamMeta实例（链式调用）
	 */
	public ParamMeta addBinaryData(String name, Object value) {
		if (binaryDataParamMeta == null) {
			binaryDataParamMeta = new BinaryDataParamMeta(request);
		}
		binaryDataParamMeta.set(name, value);
		return this;
	}

	/**
	 * 判断二进制参数是否为空
	 *
	 * @return true=参数为空（参数名称或数据值为null），false=参数非空
	 */
	public boolean isBinaryDataEmpty() {
		return binaryDataParamMeta == null || binaryDataParamMeta.isEmpty();
	}

	/**
	 * 获取二进制数据参数元数据
	 *
	 * @return BinaryDataParamMeta实例
	 */
	public BinaryDataParamMeta binaryDataParamMeta() {
		if (binaryDataParamMeta == null) {
			binaryDataParamMeta = new BinaryDataParamMeta(request);
		}
		return binaryDataParamMeta;
	}

	// ===================== 构造方法与请求关联方法 =====================

	/**
	 * 构造参数元数据实例
	 *
	 * @param request 关联的HTTP请求实例
	 */
	public ParamMeta(HttpRequest request) {
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
		StringBuilder sb = new StringBuilder();
		// 适配对象类型的参数块添加（自动处理对象空值、嵌套格式）
		addObjectParamBlock(sb, "查询参数", queryParamMeta, this::isQueryEmpty);
		addObjectParamBlock(sb, "普通表单参数", formParamMeta, this::isFormEmpty);
		addObjectParamBlock(sb, "多部分表单参数", multipartFormMeta, this::isMultipartFormEmpty);
		addObjectParamBlock(sb, "请求体参数", bodyParamMeta, () -> !request.isBody() && !request.isBodyMap());
		addObjectParamBlock(sb, "二进制数据参数", binaryDataParamMeta, this::isBinaryDataEmpty);

		// 无任何参数时返回友好提示
		return sb.isEmpty() ? "无请求参数" : sb.toString();
	}

	/**
	 * 通用方法：添加对象类型的参数块（适配自定义元数据对象，优化嵌套输出）
	 *
	 * @param sb        字符串构建器
	 * @param paramDesc 参数描述（中文，更易读，如「查询参数」）
	 * @param paramObj  自定义元数据对象（如 QueryParamMeta）
	 * @param isEmpty   判断参数是否为空的逻辑（避免触发懒加载）
	 */
	private void addObjectParamBlock(StringBuilder sb, String paramDesc, Object paramObj, Supplier<Boolean> isEmpty) {
		// 1. 空判断：优先用业务isEmpty方法（避免触发懒加载），再判断对象是否为null
		if (isEmpty.get() || paramObj == null) {
			return;
		}

		// 2. 非第一个参数块，添加分隔符+换行（提升可读性）
		if (!sb.isEmpty()) {
			sb.append(System.lineSeparator())
				.append("----------------------------------------")
				.append(System.lineSeparator());
		}

		// 3. 拼接参数块：适配对象类型的嵌套输出（缩进+换行）
		sb.append(paramDesc).append(": ")
			.append(System.lineSeparator())
			// 对对象toString结果做缩进，嵌套更清晰
			.append(indentString(paramObj.toString(), 2));
	}

	/**
	 * 辅助方法：给字符串添加缩进（适配对象嵌套输出）
	 *
	 * @param content 原字符串
	 * @param spaces  缩进空格数
	 * @return 缩进后的字符串
	 */
	private String indentString(String content, int spaces) {
		if (content == null || content.isBlank()) {
			return "无数据";
		}
		// 生成缩进符（如2个空格）
		String indent = " ".repeat(spaces);
		// 按行缩进，适配多行字符串
		return content.lines()
			.map(line -> indent + line)
			.collect(Collectors.joining(System.lineSeparator()));
	}

}
