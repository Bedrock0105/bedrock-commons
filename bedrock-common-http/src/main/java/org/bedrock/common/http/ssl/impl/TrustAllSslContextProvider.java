package org.bedrock.common.http.ssl.impl;

import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.ssl.SslContextProvider;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * 信任所有证书的SSL上下文提供者实现类
 * <p>核心特性：无条件信任服务端所有SSL证书、忽略域名与证书的匹配校验，跳过所有HTTPS安全校验逻辑
 * <p>适用场景：仅限【内网测试环境、开发环境】对接自签名SSL证书的服务端使用
 * <p>⚠️ 重要警示：生产环境严禁使用该实现，会存在SSL中间人攻击、证书伪造等严重安全风险！
 * <p>实现说明：采用饿汉式单例设计，静态代码块初始化核心SSL组件，全局复用唯一实例，避免重复初始化性能损耗
 */
public class TrustAllSslContextProvider implements SslContextProvider {

	/**
	 * 当前实现类的全局单例实例，推荐统一使用该实例，禁止重复创建
	 */
	public static final SslContextProvider INSTANCE = new TrustAllSslContextProvider();

	/**
	 * 私有化构造方法，禁止外部实例化，保障单例唯一性
	 */
	private TrustAllSslContextProvider() {
	}

	/**
	 * 全局复用的SSL上下文对象，已初始化完成信任所有证书的TLS协议环境
	 */
	private static final SSLContext SSL_CONTEXT;

	/**
	 * 信任所有X509证书的信任管理器，空实现所有校验方法，无条件信任任意证书
	 */
	private static final X509TrustManager TRUST_MANAGER;

	/**
	 * 忽略域名校验的域名验证器，固定返回true，跳过域名与证书绑定关系的校验
	 */
	private static final HostnameVerifier HOSTNAME_VERIFIER = (hostname, session) -> true;

	/**
	 * 静态代码块初始化SSL核心组件，项目启动时执行且仅执行一次
	 * 初始化TLS协议的SSL上下文，注入自定义的信任管理器，完成全局SSL环境初始化
	 * 初始化失败时抛出运行时异常，终止程序启动，避免使用无效的SSL环境
	 */
	static {
		try {
			SSL_CONTEXT = SSLContext.getInstance("TLS");
			TRUST_MANAGER = new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) {
					// 空实现：无条件信任客户端证书
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) {
					// 空实现：无条件信任服务端证书，跳过所有证书合法性校验
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					// 返回空数组：表示无指定信任的证书颁发机构
					return new X509Certificate[0];
				}
			};
			SSL_CONTEXT.init(null, new TrustManager[]{TRUST_MANAGER}, new SecureRandom());
		} catch (Exception e) {
			throw new RuntimeException("初始化信任所有证书的SSL上下文失败，无法完成HTTPS请求初始化", e);
		}
	}

	/**
	 * 获取初始化完成的SSL上下文对象
	 *
	 * @param request 当前请求对象，本实现暂未做请求级别的差异化配置，入参仅做兼容预留
	 * @return 全局复用的信任所有证书的SSLContext实例
	 */
	@Override
	public SSLContext getSslContext(HttpRequest request) {
		return SSL_CONTEXT;
	}

	/**
	 * 获取信任所有证书的X509信任管理器
	 *
	 * @param request 当前请求对象，本实现暂未做请求级别的差异化配置，入参仅做兼容预留
	 * @return 全局复用的无条件信任管理器实例
	 */
	@Override
	public X509TrustManager getX509TrustManager(HttpRequest request) {
		return TRUST_MANAGER;
	}

	/**
	 * 获取忽略域名校验的域名验证器
	 *
	 * @param request 当前请求对象，本实现暂未做请求级别的差异化配置，入参仅做兼容预留
	 * @return 全局复用的跳过域名校验的验证器实例
	 */
	@Override
	public HostnameVerifier getHostnameVerifier(HttpRequest request) {
		return HOSTNAME_VERIFIER;
	}
}
