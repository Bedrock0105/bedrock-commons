package org.bedrock.common.code.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * Spring 资源加载工具类
 * <p>
 * 继承 Spring {@link org.springframework.util.ResourceUtils}，提供根据路径自动识别协议并加载资源的能力。
 * 支持的资源类型包括：classpath、本地文件、HTTP/HTTPS、FTP 及其他标准 URL 协议。
 */
public abstract class ResourceUtil extends org.springframework.util.ResourceUtils {

	/**
	 * 根据路径获取对应的 Spring {@link Resource} 对象。
	 * <p>
	 * 路径识别规则（按优先级）：
	 * <ol>
	 *   <li>{@code classpath:} 前缀 —— 类路径资源，如 {@code classpath:config/app.yml}</li>
	 *   <li>带协议的资源 URL —— 按 URL 协议加载：
	 *     <ul>
	 *       <li>{@code file:} —— 本地文件（绝对/相对路径均可）</li>
	 *       <li>{@code http://} / {@code https://} —— 远程 HTTP 资源</li>
	 *       <li>{@code ftp://} —— FTP 远程资源</li>
	 *       <li>{@code jar:} —— JAR 包内资源</li>
	 *       <li>其他合法 URL 协议 —— 通过 {@link UrlResource} 加载</li>
	 *     </ul>
	 *   </li>
	 *   <li>无协议前缀 —— 视为本地文件系统路径，如 {@code /data/file.txt} 或 {@code D:/docs/readme.md}</li>
	 * </ol>
	 *
	 * @param location 资源路径，不可为空
	 * @return 对应的 Resource 实例
	 * @throws IllegalArgumentException 路径为空，或 URL 格式无效时抛出
	 */
	public static Resource getResource(String location) throws IOException {
		Assert.hasText(location, "资源路径不能为空");
		String path = location.trim();

		// classpath 类路径资源
		if (path.startsWith(CLASSPATH_URL_PREFIX)) {
			return new ClassPathResource(path.substring(CLASSPATH_URL_PREFIX.length()));
		}

		// 带协议的 URL 资源（file / http / https / ftp / jar 等）
		if (isUrl(path)) {
			return new UrlResource(path);
		}

		// 本地文件系统路径（无协议前缀）
		return new FileSystemResource(path);
	}

}
