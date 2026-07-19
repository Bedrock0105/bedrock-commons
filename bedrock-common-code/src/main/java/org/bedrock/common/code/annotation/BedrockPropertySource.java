/*
 * Copyright
 * Author: RA
 */

package org.bedrock.common.code.annotation;

import org.bedrock.common.code.factory.DefaultPropertySourceFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 自定义资源文件读取
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PropertySource(value = {}, factory = DefaultPropertySourceFactory.class)
public @interface BedrockPropertySource {

    /**
     * Indicate the resource location(s) of the properties file to be loaded.
     * for example, {@code "classpath:/com/example/app.yml"}
     *
     * @return location(s)
     */
    @AliasFor(annotation = PropertySource.class)
    String[] value();


	/**
	 * A specific character encoding for the given resources, e.g. "UTF-8".
	 * @since 4.3
	 */
	@AliasFor(annotation = PropertySource.class)
	String encoding() default "";



	/**
	 * Indicate if a failure to find a {@link #value property resource} should be
	 * ignored.
	 * <p>{@code true} is appropriate if the properties file is completely optional.
	 * <p>Default is {@code false}.
	 * @since 4.0
	 */
	@AliasFor(annotation = PropertySource.class)
	boolean ignoreResourceNotFound() default false;
}
