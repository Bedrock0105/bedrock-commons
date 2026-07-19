package org.bedrock.common.log.annotation;

import java.lang.annotation.*;

/**
 * 排除日志打印的接口
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcludeRequestLog {

}
