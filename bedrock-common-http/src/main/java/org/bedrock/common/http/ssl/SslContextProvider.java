package org.bedrock.common.http.ssl;

import org.bedrock.common.http.HttpRequest;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/**
 * SSL上下文能力提供接口
 * 定义获取HTTPS请求核心SSL组件的标准方法，提供统一能力规范，支持多实现策略
 * 核心能力：提供SSL上下文、证书信任管理器、域名校验器
 */
public interface SslContextProvider {

	/**
	 * 获取SSL上下文对象
	 *
	 * @return SSLContext 初始化完成的SSL上下文
	 */
	SSLContext getSslContext(HttpRequest request);

	/**
	 * 获取X509证书信任管理器
	 *
	 * @return X509TrustManager 证书信任管理器
	 */
	X509TrustManager getX509TrustManager(HttpRequest request);

	/**
	 * 获取域名校验器
	 *
	 * @return HostnameVerifier 域名合法性校验器
	 */
	HostnameVerifier getHostnameVerifier(HttpRequest request);
}
