package org.bedrock.common.http.annotation;

import java.lang.annotation.*;

/**
 * 下载文件注解（标注用于文件下载的方法，配置下载相关属性）
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DownloadFile {

	/**
	 * 下载文件存储目录
	 * 支持：Spring Boot环境占位符(${xxx})、方法参数变量({xxx})
	 *
	 * @return 存储目录路径
	 */
	String dir() default "";

	/**
	 * 下载文件名（默认空字符串）
	 * 支持：Spring Boot环境占位符(${xxx})、方法参数变量({xxx})
	 *
	 * @return 下载文件名称
	 */
	String fileName() default "";
}
