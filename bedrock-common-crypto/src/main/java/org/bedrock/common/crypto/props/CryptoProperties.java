package org.bedrock.common.crypto.props;

import lombok.Getter;
import lombok.Setter;
import org.bedrock.common.crypto.algorithm.CryptoAlgorithm;
import org.bedrock.common.crypto.enums.CryptoAlgorithmEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * API 加解密配置属性。
 * <p>配置前缀：{@code bedrock.crypto}</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = CryptoProperties.PREFIX)
public class CryptoProperties {

	/**
	 * 配置前缀
	 */
	public static final String PREFIX = "bedrock.crypto";

	/**
	 * 是否启用 API 加解密拦截
	 */
	private boolean enabled = true;

	/**
	 * 默认内置算法枚举（注解未指定 handler / algorithm 时使用）
	 */
	private CryptoAlgorithmEnum algorithm = CryptoAlgorithmEnum.AES;

	/**
	 * 自定义 {@link CryptoAlgorithm} 实现 Class。
	 * <p>
	 * 优先级高于 {@link #algorithm} 枚举，但低于注解上的 {@code handler}。
	 * 默认 {@link CryptoAlgorithm} 表示未指定，此时回退到 {@link #algorithm}。
	 * </p>
	 */
	private Class<? extends CryptoAlgorithm> algorithmClass = CryptoAlgorithm.class;

	/**
	 * 统一加密请求参数名，默认为 {@code params}
	 */
	private String paramName = "params";

	/**
	 * 对称密钥（Hex），用于 AES / DES / SM4
	 */
	private String key = "";

	/**
	 * 非对称公钥（Hex），用于 RSA / SM2 加密
	 */
	private String publicKey = "";

	/**
	 * 非对称私钥（Hex），用于 RSA / SM2 解密
	 */
	private String privateKey = "";

	/**
	 * Advice 执行顺序，默认 0，越小越先执行
	 */
	private Integer order = 0;

	/**
	 * 解析最终使用的算法 Class：若 {@link #algorithmClass} 未指定具体实现，则使用枚举对应 Class。
	 *
	 * @return 算法实现类型
	 */
	public Class<? extends CryptoAlgorithm> getAlgorithmClass() {
		if (algorithmClass == CryptoAlgorithm.class) {
			return algorithm.getAlgorithmClass();
		}
		return algorithmClass;
	}
}
