package org.bedrock.common.crypto.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 组合注解：同时表示需要加密与解密（元注解 {@link ApiEncrypt} + {@link ApiDecrypt}）。
 * <p>
 * 标注在类上时，表示该类所有方法的入参解密 + 返回加密。
 * 算法与密钥请在 {@link ApiEncrypt} / {@link ApiDecrypt} 上分别配置，或依赖 {@code bedrock.crypto} 配置。
 * </p>
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD, PARAMETER})
@ApiEncrypt
@ApiDecrypt
@Inherited
public @interface ApiCrypto {

}
