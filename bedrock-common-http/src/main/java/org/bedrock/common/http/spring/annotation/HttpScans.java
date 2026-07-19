package org.bedrock.common.http.spring.annotation;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * HTTP客户端扫描注解
 * 用于扫描指定包下的HTTP客户端接口，自动注册为Spring Bean，支持自定义扫描规则、Bean创建逻辑、作用域等配置
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ClientScannerRegistrar.RepeatingRegistrar.class)
public @interface HttpScans {

	HttpScan[] value();
}
