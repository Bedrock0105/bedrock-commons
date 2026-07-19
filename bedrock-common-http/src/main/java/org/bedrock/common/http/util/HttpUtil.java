package org.bedrock.common.http.util;

import org.bedrock.common.http.HttpConfiguration;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.enums.HttpMethod;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * HTTP请求工具类
 * 基于自定义HttpRequest封装的同步请求工具，提供GET/POST/PUT/PATCH/DELETE/HEAD/OPTIONS/TRACE全请求方式
 * 所有方法均提供多级重载，适配不同传参场景，底层自动加载全局默认HttpConfiguration配置
 * 支持URL查询参数、自定义请求头、请求体传递，泛型自动解析响应结果为指定数据类型
 */
public abstract class HttpUtil {

	/**
	 * GET请求 - 极简重载：仅传请求地址+返回值类型
	 *
	 * @param url  请求完整地址（协议+域名+路径）
	 * @param type 响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T get(String url, Type type) {
		return get(url, Map.of(), Map.of(), type);
	}

	/**
	 * GET请求 - 全参重载：请求地址+查询参数+请求头+返回值类型
	 *
	 * @param url      请求完整地址（协议+域名+路径）
	 * @param queryMap URL拼接的查询参数集合
	 * @param headers  请求头配置集合
	 * @param type     响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T get(String url, Map<String, Object> queryMap, Map<String, String> headers, Type type) {
		return createRequest(url, HttpMethod.GET)
			.addQuery(queryMap)
			.addHeaders(headers)
			.execute(type);
	}

	/**
	 * POST请求 - 极简重载：仅传请求地址+返回值类型
	 *
	 * @param url  请求完整地址（协议+域名+路径）
	 * @param type 响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T post(String url, Type type) {
		return post(url, Map.of(), type);
	}

	/**
	 * POST请求 - 重载：请求地址+请求头+返回值类型（无请求体）
	 *
	 * @param url     请求完整地址（协议+域名+路径）
	 * @param headers 请求头配置集合
	 * @param type    响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T post(String url, Map<String, String> headers, Type type) {
		return createRequest(url, HttpMethod.POST)
			.addHeaders(headers)
			.execute(type);
	}

	/**
	 * POST请求 - 重载：请求地址+请求体+返回值类型（默认空请求头）
	 *
	 * @param url  请求完整地址（协议+域名+路径）
	 * @param body 请求体参数对象（支持JSON/表单等格式，内部自动序列化）
	 * @param type 响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T post(String url, Object body, Type type) {
		return post(url, Map.of(), body, type);
	}

	/**
	 * POST请求 - 全参重载：请求地址+请求头+请求体+返回值类型
	 *
	 * @param url     请求完整地址（协议+域名+路径）
	 * @param headers 请求头配置集合
	 * @param body    请求体参数对象（支持JSON/表单等格式，内部自动序列化）
	 * @param type    响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T post(String url, Map<String, String> headers, Object body, Type type) {
		return createRequest(url, HttpMethod.POST)
			.addHeaders(headers)
			.addBody(body)
			.execute(type);
	}

	/**
	 * HEAD请求 - 极简重载：仅传请求地址+返回值类型
	 * <p>特性：与GET一致，仅返回响应头，无响应体，常用于检测资源是否存在</p>
	 *
	 * @param url  请求完整地址（协议+域名+路径）
	 * @param type 响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T head(String url, Type type) {
		return head(url, Map.of(), Map.of(), type);
	}

	/**
	 * HEAD请求 - 全参重载：请求地址+查询参数+请求头+返回值类型
	 * <p>特性：与GET一致，仅返回响应头，无响应体，常用于检测资源是否存在</p>
	 *
	 * @param url      请求完整地址（协议+域名+路径）
	 * @param queryMap URL拼接的查询参数集合
	 * @param headers  请求头配置集合
	 * @param type     响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T head(String url, Map<String, Object> queryMap, Map<String, String> headers, Type type) {
		return createRequest(url, HttpMethod.HEAD)
			.addQuery(queryMap)
			.addHeaders(headers)
			.execute(type);
	}

	/**
	 * PUT请求 - 极简重载：仅传请求地址+返回值类型
	 * <p>特性：幂等请求，常用于全量更新资源，需传递完整资源对象</p>
	 *
	 * @param url  请求完整地址（协议+域名+路径）
	 * @param type 响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T put(String url, Type type) {
		return put(url, Map.of(), type);
	}

	/**
	 * PUT请求 - 重载：请求地址+请求头+返回值类型（无请求体）
	 * <p>特性：幂等请求，常用于全量更新资源，需传递完整资源对象</p>
	 *
	 * @param url     请求完整地址（协议+域名+路径）
	 * @param headers 请求头配置集合
	 * @param type    响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T put(String url, Map<String, String> headers, Type type) {
		return createRequest(url, HttpMethod.PUT)
			.addHeaders(headers)
			.execute(type);
	}

	/**
	 * PUT请求 - 重载：请求地址+请求体+返回值类型（默认空请求头）
	 * <p>特性：幂等请求，常用于全量更新资源，需传递完整资源对象</p>
	 *
	 * @param url  请求完整地址（协议+域名+路径）
	 * @param body 请求体参数对象（支持JSON/表单等格式，内部自动序列化）
	 * @param type 响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T put(String url, Object body, Type type) {
		return put(url, Map.of(), body, type);
	}

	/**
	 * PUT请求 - 全参重载：请求地址+请求头+请求体+返回值类型
	 * <p>特性：幂等请求，常用于全量更新资源，需传递完整资源对象</p>
	 *
	 * @param url     请求完整地址（协议+域名+路径）
	 * @param headers 请求头配置集合
	 * @param body    请求体参数对象（支持JSON/表单等格式，内部自动序列化）
	 * @param type    响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T put(String url, Map<String, String> headers, Object body, Type type) {
		return createRequest(url, HttpMethod.PUT)
			.addHeaders(headers)
			.addBody(body)
			.execute(type);
	}

	/**
	 * PATCH请求 - 极简重载：仅传请求地址+返回值类型
	 * <p>特性：非幂等请求，常用于增量更新资源，仅传递需要修改的字段</p>
	 *
	 * @param url  请求完整地址（协议+域名+路径）
	 * @param type 响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T patch(String url, Type type) {
		return patch(url, Map.of(), type);
	}

	/**
	 * PATCH请求 - 重载：请求地址+请求头+返回值类型（无请求体）
	 * <p>特性：非幂等请求，常用于增量更新资源，仅传递需要修改的字段</p>
	 *
	 * @param url     请求完整地址（协议+域名+路径）
	 * @param headers 请求头配置集合
	 * @param type    响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T patch(String url, Map<String, String> headers, Type type) {
		return createRequest(url, HttpMethod.PATCH)
			.addHeaders(headers)
			.execute(type);
	}

	/**
	 * PATCH请求 - 重载：请求地址+请求体+返回值类型（默认空请求头）
	 * <p>特性：非幂等请求，常用于增量更新资源，仅传递需要修改的字段</p>
	 *
	 * @param url  请求完整地址（协议+域名+路径）
	 * @param body 请求体参数对象（支持JSON/表单等格式，内部自动序列化）
	 * @param type 响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T patch(String url, Object body, Type type) {
		return patch(url, Map.of(), body, type);
	}

	/**
	 * PATCH请求 - 全参重载：请求地址+请求头+请求体+返回值类型
	 * <p>特性：非幂等请求，常用于增量更新资源，仅传递需要修改的字段</p>
	 *
	 * @param url     请求完整地址（协议+域名+路径）
	 * @param headers 请求头配置集合
	 * @param body    请求体参数对象（支持JSON/表单等格式，内部自动序列化）
	 * @param type    响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T patch(String url, Map<String, String> headers, Object body, Type type) {
		return createRequest(url, HttpMethod.PATCH)
			.addHeaders(headers)
			.addBody(body)
			.execute(type);
	}

	/**
	 * DELETE请求 - 极简重载：仅传请求地址+返回值类型
	 * <p>特性：幂等请求，常用于删除指定资源，支持传查询参数/请求体两种方式</p>
	 *
	 * @param url  请求完整地址（协议+域名+路径）
	 * @param type 响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T delete(String url, Type type) {
		return createRequest(url, HttpMethod.DELETE)
			.execute(type);
	}

	/**
	 * DELETE请求 - 重载：请求地址+查询参数+请求头+返回值类型（无请求体）
	 * <p>特性：幂等请求，常用于删除指定资源，支持传查询参数/请求体两种方式</p>
	 *
	 * @param url      请求完整地址（协议+域名+路径）
	 * @param queryMap URL拼接的查询参数集合
	 * @param headers  请求头配置集合
	 * @param type     响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T delete(String url, Map<String, Object> queryMap, Map<String, String> headers, Type type) {
		return createRequest(url, HttpMethod.DELETE)
			.addQuery(queryMap)
			.addHeaders(headers)
			.execute(type);
	}

	/**
	 * DELETE请求 - 重载：请求地址+请求体+返回值类型（默认空请求头）
	 * <p>特性：幂等请求，常用于删除指定资源，支持传查询参数/请求体两种方式</p>
	 *
	 * @param url  请求完整地址（协议+域名+路径）
	 * @param body 请求体参数对象（支持JSON/表单等格式，内部自动序列化）
	 * @param type 响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T delete(String url, Object body, Type type) {
		return delete(url, Map.of(), body, type);
	}

	/**
	 * DELETE请求 - 全参重载：请求地址+请求头+请求体+返回值类型
	 * <p>特性：幂等请求，常用于删除指定资源，支持传查询参数/请求体两种方式</p>
	 *
	 * @param url     请求完整地址（协议+域名+路径）
	 * @param headers 请求头配置集合
	 * @param body    请求体参数对象（支持JSON/表单等格式，内部自动序列化）
	 * @param type    响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T delete(String url, Map<String, String> headers, Object body, Type type) {
		return createRequest(url, HttpMethod.DELETE)
			.addHeaders(headers)
			.addBody(body)
			.execute(type);
	}

	/**
	 * OPTIONS请求 - 极简重载：仅传请求地址+返回值类型
	 * <p>特性：获取服务端支持的所有HTTP请求方法，预检请求，无请求体</p>
	 *
	 * @param url  请求完整地址（协议+域名+路径）
	 * @param type 响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T options(String url, Type type) {
		return options(url, Map.of(), type);
	}

	/**
	 * OPTIONS请求 - 全参重载：请求地址+请求头+返回值类型
	 * <p>特性：获取服务端支持的所有HTTP请求方法，预检请求，无请求体</p>
	 *
	 * @param url     请求完整地址（协议+域名+路径）
	 * @param headers 请求头配置集合
	 * @param type    响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T options(String url, Map<String, String> headers, Type type) {
		return createRequest(url, HttpMethod.OPTIONS)
			.addHeaders(headers)
			.execute(type);
	}

	/**
	 * TRACE请求 - 极简重载：仅传请求地址+返回值类型
	 * <p>特性：回显服务器收到的请求，用于测试/诊断，无请求体，生产环境极少使用</p>
	 *
	 * @param url  请求完整地址（协议+域名+路径）
	 * @param type 响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T trace(String url, Type type) {
		return trace(url, Map.of(), type);
	}

	/**
	 * TRACE请求 - 全参重载：请求地址+请求头+返回值类型
	 * <p>特性：回显服务器收到的请求，用于测试/诊断，无请求体，生产环境极少使用</p>
	 *
	 * @param url     请求完整地址（协议+域名+路径）
	 * @param headers 请求头配置集合
	 * @param type    响应结果的目标数据类型
	 * @return <T> 解析后的目标类型返回值
	 */
	public static <T> T trace(String url, Map<String, String> headers, Type type) {
		return createRequest(url, HttpMethod.TRACE)
			.addHeaders(headers)
			.execute(type);
	}

	/**
	 * 创建基础HTTP请求对象，初始化全局默认配置
	 *
	 * @param url    请求完整地址（协议+域名+路径）
	 * @param method HTTP请求方式枚举
	 * @return 初始化完成的HttpRequest实例，支持链式调用配置参数
	 */
	public static HttpRequest createRequest(String url, HttpMethod method) {
		return new HttpRequest(HttpConfiguration.getDefault())
			.url(url)
			.method(method);
	}
}
