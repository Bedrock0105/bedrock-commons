package org.bedrock.common.tenant.annotation;

import java.lang.annotation.*;

/**
 * 忽略租户注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantIgnore {

}
