package org.bedrock.common.crypto.annotation;

import org.bedrock.common.crypto.algorithm.CryptoAlgorithm;
import org.bedrock.common.crypto.enums.CryptoAlgorithmEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标记需要对请求入参进行解密。
 * <p>
 * 可标注在类、方法、参数上。类上标注表示该类所有方法的入参需要解密
 * （除非方法/参数被 {@link ApiExcludeCrypto} 排除）。
 * </p>
 */
@Inherited
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD, PARAMETER})
public @interface ApiDecrypt {

	/**
	 * 自定义加解密策略实现类，算法解析优先级最高。
	 * <p>默认 {@link CryptoAlgorithm} 表示未指定，继续按配置 bean / 枚举解析。</p>
	 *
	 * @return 策略实现 Class
	 */
	Class<? extends CryptoAlgorithm> handler() default CryptoAlgorithm.class;

	/**
	 * 内置算法枚举（{@link #handler} 未指定时使用）
	 *
	 * @return 算法枚举
	 */
	CryptoAlgorithmEnum algorithm() default CryptoAlgorithmEnum.NULL;

	/**
	 * 对称密钥（Hex），为空则使用配置文件中的 key
	 *
	 * @return 对称密钥
	 */
	String key() default "";

	/**
	 * 非对称公钥（Hex），为空则使用配置
	 *
	 * @return 公钥
	 */
	String publicKey() default "";

	/**
	 * 非对称私钥（Hex），为空则使用配置
	 *
	 * @return 私钥
	 */
	String privateKey() default "";
}
