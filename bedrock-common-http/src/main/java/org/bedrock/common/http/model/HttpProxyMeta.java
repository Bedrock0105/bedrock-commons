package org.bedrock.common.http.model;

import lombok.Getter;
import lombok.Setter;

import java.net.Proxy;
import java.util.Collection;

@Setter
@Getter
public class HttpProxyMeta {

	/**
	 * 代理类型 (HTTP/SOCKS等)
	 */
	private final Proxy.Type type;

	/**
	 * 代理服务器地址
	 */
	private final String host;

	/**
	 * 代理服务器端口
	 */
	private final int port;

	/**
	 * 代理认证用户名
	 */
	private String username;

	/**
	 * 代理认证密码
	 */
	private String password;

	/**
	 * 代理请求头元数据，默认初始化空实例
	 */
	private HeadersMeta headerMetas = new HeadersMeta();

	/**
	 * 私有化构造器，禁止外部直接实例化
	 *
	 * @param type 代理类型
	 * @param host 代理服务器地址
	 * @param port 代理服务器端口
	 */
	private HttpProxyMeta(Proxy.Type type, String host, int port) {
		this.type = type;
		this.host = host;
		this.port = port;
	}

	/**
	 * 链式添加单个请求头
	 *
	 * @param name  请求头名称
	 * @param value 请求头值
	 * @return 当前HttpProxyMeta实例
	 */
	public HttpProxyMeta addHeader(String name, String value) {
		this.headerMetas.addHeader(new HeaderMeta(name, value));
		return this;
	}

	/**
	 * 链式添加单个请求头
	 *
	 * @param headerMeta 请求头元数据
	 * @return 当前HttpProxyMeta实例
	 */
	public HttpProxyMeta addHeader(HeaderMeta headerMeta) {
		this.headerMetas.addHeader(headerMeta);
		return this;
	}

	/**
	 * 链式设置/覆盖单个请求头，存在则覆盖，不存在则新增
	 *
	 * @param name  请求头名称
	 * @param value 请求头新值
	 * @return 当前HttpProxyMeta实例
	 */
	public HttpProxyMeta setHeader(String name, String value) {
		this.headerMetas.setHeader(name, value);
		return this;
	}

	/**
	 * 链式设置/覆盖单个请求头，存在则覆盖，不存在则新增
	 *
	 * @param headerMeta 请求头元数据
	 * @return 当前HttpProxyMeta实例
	 */
	public HttpProxyMeta setHeader(HeaderMeta headerMeta) {
		this.headerMetas.setHeader(headerMeta);
		return this;
	}

	/**
	 * 链式批量添加请求头
	 *
	 * @param headerMetas 请求头元数据集合，传null不处理
	 * @return 当前HttpProxyMeta实例
	 */
	public HttpProxyMeta addHeaders(Collection<HeaderMeta> headerMetas) {
		if (headerMetas == null) {
			return this;
		}
		this.headerMetas.addHeaders(headerMetas);
		return this;
	}

	/**
	 * 创建默认HTTP类型的代理配置元数据
	 *
	 * @param host 代理服务器地址
	 * @param port 代理服务器端口
	 * @return HttpProxyMeta 实例
	 */
	public static HttpProxyMeta create(String host, int port) {
		return new HttpProxyMeta(Proxy.Type.HTTP, host, port);
	}

	/**
	 * 创建自定义代理类型的代理配置元数据
	 *
	 * @param type 代理类型
	 * @param host 代理服务器地址
	 * @param port 代理服务器端口
	 * @return HttpProxyMeta 实例
	 */
	public static HttpProxyMeta create(Proxy.Type type, String host, int port) {
		return new HttpProxyMeta(type, host, port);
	}

}
