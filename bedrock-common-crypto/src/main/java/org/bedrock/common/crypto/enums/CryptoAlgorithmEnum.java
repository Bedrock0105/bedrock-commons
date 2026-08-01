package org.bedrock.common.crypto.enums;

import lombok.Getter;
import org.bedrock.common.crypto.algorithm.CryptoAlgorithm;
import org.bedrock.common.crypto.algorithm.impl.AesCryptoAlgorithm;
import org.bedrock.common.crypto.algorithm.impl.DesCryptoAlgorithm;
import org.bedrock.common.crypto.algorithm.impl.RsaCryptoAlgorithm;
import org.bedrock.common.crypto.algorithm.impl.Sm2CryptoAlgorithm;
import org.bedrock.common.crypto.algorithm.impl.Sm4CryptoAlgorithm;

/**
 * 内置加解密算法枚举。
 * <p>
 * 每项仅挂接对应 {@link CryptoAlgorithm} 实现 Class，实际实例由
 * {@link org.bedrock.common.crypto.algorithm.CryptoAlgorithmRegistry} 通过 SPI 加载。
 * </p>
 */
@Getter
public enum CryptoAlgorithmEnum {

	/**
	 * AES 对称加密
	 */
	AES(AesCryptoAlgorithm.class),

	/**
	 * DES 对称加密
	 */
	DES(DesCryptoAlgorithm.class),

	/**
	 * RSA 非对称加密
	 */
	RSA(RsaCryptoAlgorithm.class),

	/**
	 * 国密 SM4 对称加密
	 */
	SM4(Sm4CryptoAlgorithm.class),

	/**
	 * 国密 SM2 非对称加密
	 */
	SM2(Sm2CryptoAlgorithm.class),

	/**
	 * 空实现，用于标识无加密算法
	 */
	NULL(CryptoAlgorithm.class);

	/**
	 * 对应的策略实现类型
	 */
	private final Class<? extends CryptoAlgorithm> algorithmClass;

	CryptoAlgorithmEnum(Class<? extends CryptoAlgorithm> algorithmClass) {
		this.algorithmClass = algorithmClass;
	}
}
