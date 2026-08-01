package org.bedrock.common.crypto.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 排除 API 加解密。
 * <p>
 * 优先级最高：标注后即使类/方法存在 {@link ApiCrypto} / {@link ApiEncrypt} / {@link ApiDecrypt}，
 * 也不再对该目标执行加解密。
 * </p>
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD, PARAMETER})
public @interface ApiExcludeCrypto {

}
