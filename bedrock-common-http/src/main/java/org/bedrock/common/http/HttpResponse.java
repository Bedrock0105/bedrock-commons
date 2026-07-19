package org.bedrock.common.http;

import lombok.Getter;
import lombok.Setter;
import org.bedrock.common.code.util.FileUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.http.annotation.DownloadFile;
import org.bedrock.common.http.converter.Converter;
import org.bedrock.common.http.enums.HeaderEnum;
import org.bedrock.common.http.model.ContentType;
import org.bedrock.common.http.model.CookieMeta;
import org.bedrock.common.http.model.HeaderMeta;
import org.bedrock.common.http.model.HeadersMeta;
import org.bedrock.common.http.support.HttpSupport;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP响应抽象基类
 * 统一封装HTTP响应公共属性与通用方法，强制子类实现核心数据（字节数组/输入流）获取逻辑
 * 支持响应数据类型转换、文件下载、响应状态判断、耗时统计等核心功能
 */
@Getter
@Setter
public abstract class HttpResponse implements AutoCloseable {

	/**
	 * 字符集编码（默认 UTF-8）
	 */
	protected Charset charset = StandardCharsets.UTF_8;

	/**
	 * 响应头集合（默认初始化空实例）
	 */
	protected HeadersMeta headersMeta = new HeadersMeta();

	/**
	 * 响应状态码（默认 200）
	 */
	protected int httpStatus = 200;

	/**
	 * 响应数据是否已关闭（默认false）
	 */
	protected boolean closed = false;

	/**
	 * 响应内容类型
	 */
	protected ContentType contentType;

	/**
	 * 关联的请求对象
	 */
	protected HttpRequest request;

	/**
	 * 请求发起时间
	 */
	protected LocalDateTime requestTime;

	/**
	 * 响应返回时间
	 */
	protected LocalDateTime responseTime;

	/**
	 * 异常信息记录
	 */
	protected Throwable exception;

	/**
	 * 获取关联请求对象（与getRequest()功能一致）
	 *
	 * @return HttpRequest实例（可为null）
	 */
	public HttpRequest request() {
		return request;
	}

	/**
	 * 获取响应字节数组（子类实现）
	 *
	 * @return 响应体字节数组
	 */
	public abstract byte[] getBytes();

	/**
	 * 获取响应输入流（子类实现）
	 *
	 * @return 响应体输入流
	 */
	public abstract InputStream getInputStream();

	/**
	 * 将响应数据转换为指定类型对象
	 * 支持转换类型：HttpResponse、String、InputStream、byte[]、File
	 * 其他类型通过转换器 {@link Converter#convertToResponseObject} 转换
	 *
	 * @param type 目标类型
	 * @param <T>  泛型类型
	 * @return 转换后的业务对象
	 */
	@SuppressWarnings("unchecked")
	public <T> T result(Type type) {
		if (type instanceof Class<?> classType) {
			if (classType == void.class || classType == Void.class) {
				return null;
			}
			if (HttpResponse.class.isAssignableFrom(classType)) {
				return (T) this;
			}
			if (String.class.isAssignableFrom(classType)) {
				return (T) new String(getBytes(), charset);
			}
			if (classType == InputStream.class) {
				return (T) getInputStream();
			}
			if (classType == byte[].class) {
				return (T) getBytes();
			}
			if (classType == File.class) {
				// 调用抽离后的下载逻辑方法
				return (T) resolveDownloadFile();
			}
		}
		return request.getConverter().convertToResponseObject(getBytes(), this, type);
	}

	/**
	 * 抽离：解析文件下载配置并执行下载（供result()方法调用）
	 * 处理DownloadFile注解、占位符替换、默认文件名兜底
	 *
	 * @return 下载后的File对象（下载失败返回null）
	 */
	private File resolveDownloadFile() {
		DownloadFile annotation = request.getMethodMeta().getAnnotation(DownloadFile.class);
		// 无注解时，使用默认目录+自动解析文件名
		if (annotation == null) {
			return downloadFile(null, getFileName());
		}

		// 解析存储目录（替换占位符）
		String dir = annotation.dir();
		if (StringUtil.isNotBlank(dir)) {
			dir = HttpSupport.resolvePlaceholders(annotation.dir(), request);
		}

		// 解析文件名（替换占位符，兜底自动解析文件名）
		String fileName = annotation.fileName();
		if (StringUtil.isNotBlank(fileName)) {
			fileName = HttpSupport.resolvePlaceholders(fileName, request);
		} else {
			fileName = getFileName();
		}

		return downloadFile(dir, fileName);
	}

	/**
	 * 获取响应内容类型（与getContentType()功能一致）
	 *
	 * @return ContentType实例（可为null）
	 */
	public ContentType contentType() {
		return this.contentType;
	}

	/**
	 * 下载响应内容为本地文件
	 *
	 * @param dir      存储目录（可为null/空，为空时默认使用系统临时目录）
	 * @param fileName 文件名（可为null/空，为空时默认生成UUID文件名）
	 * @return 下载成功返回目标File对象，下载失败返回null（失败时自动删除无效文件）
	 */
	public File downloadFile(String dir, String fileName) {
		// 文件名兜底：为空则生成UUID
		if (StringUtil.isBlank(fileName)) {
			fileName = StringUtil.randomUUID();
		}

		// 处理存储目录：非空则创建多级目录，为空则使用系统临时目录
		if (StringUtil.isNotBlank(dir)) {
			FileUtil.mkdirs(dir);
		} else {
			dir = FileUtil.getTempDirPath();
		}

		// 构建目标文件对象
		File targetFile = new File(dir, fileName);

		try {
			// 输入流转本地文件（自动关闭流，避免资源泄露）
			FileUtil.toFile(getInputStream(), targetFile);
			return targetFile;
		} catch (Exception e) {
			// 下载失败：删除生成的无效/损坏文件
			if (targetFile.exists()) {
				targetFile.delete();
			}
			return null;
		}
	}

	/**
	 * 获取下载文件名（优先响应头Content-Disposition，兜底从URL提取）
	 *
	 * @return 下载文件名（无法解析返回null）
	 */
	public String getFileName() {
		String headerFileName = getFileNameFromContentDisposition();
		if (headerFileName != null && !headerFileName.trim().isEmpty()) {
			return headerFileName;
		}
		// 解析失败则从URL提取
		return getFileNameFromUrl();
	}

	/**
	 * 从Content-Disposition响应头解析文件名（内部辅助方法）
	 */
	private String getFileNameFromContentDisposition() {
		String contentDisposition = getHeaderValue(HeaderEnum.CONTENT_DISPOSITION.getValue());
		if (contentDisposition == null || contentDisposition.trim().isEmpty()) {
			return null;
		}
		Pattern pattern = Pattern.compile("filename(\\*?)=(?:\"?)([^\";]+)(?:\"?)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(contentDisposition);
		if (!matcher.find()) {
			return null;
		}
		try {
			String isEncodedStar = matcher.group(1);
			String rawFileName = matcher.group(2).trim();
			String decodedFileName = rawFileName;

			if ("*".equals(isEncodedStar)) {
				String[] parts = rawFileName.split("''");
				if (parts.length >= 2) {
					String charset = parts[0];
					String encodedFileName = parts[1];
					decodedFileName = URLDecoder.decode(encodedFileName, getValidCharset(charset));
				}
			} else {
				decodedFileName = URLDecoder.decode(rawFileName, StandardCharsets.UTF_8);
			}

			String result = decodedFileName.replace("\"", "").replace("'", "").trim();
			return result.isEmpty() ? null : result;
		} catch (Exception e) {
			try {
				String rawResult = matcher.group(2).trim().replace("\"", "").replace("'", "");
				return rawResult.isEmpty() ? null : rawResult;
			} catch (Exception ex) {
				return null;
			}
		}
	}

	/**
	 * 从URL中提取文件名（内部辅助方法，兜底逻辑）
	 */
	private String getFileNameFromUrl() {
		String requestUrl = this.request != null ? this.request.url() : null;
		if (requestUrl == null || requestUrl.trim().isEmpty()) {
			return null;
		}
		// 去除查询参数
		String urlWithoutQuery = requestUrl.contains("?") ? requestUrl.substring(0, requestUrl.indexOf("?")) : requestUrl;
		// 取最后一个斜杠后的字符串
		int lastSlashIndex = urlWithoutQuery.lastIndexOf("/");
		if (lastSlashIndex == -1 || lastSlashIndex == urlWithoutQuery.length() - 1) {
			return null;
		}
		// 解码后返回
		String fileName = urlWithoutQuery.substring(lastSlashIndex + 1);
		return URLDecoder.decode(fileName, StandardCharsets.UTF_8);
	}

	/**
	 * 获取有效字符编码（兜底UTF-8）
	 *
	 * @param charset 待校验的编码字符串
	 * @return 有效Charset实例
	 */
	private Charset getValidCharset(String charset) {
		if (charset == null || charset.trim().isEmpty()) {
			return StandardCharsets.UTF_8;
		}
		if (charset.equalsIgnoreCase("UTF-8") || charset.equalsIgnoreCase("GBK")
			|| charset.equalsIgnoreCase("GB2312") || charset.equalsIgnoreCase("ISO-8859-1")) {
			return Charset.forName(charset.trim());
		}
		return StandardCharsets.UTF_8;
	}

	/**
	 * 获取响应字节数组（与getBytes()功能一致）
	 *
	 * @return 响应体字节数组
	 */
	public byte[] bytes() {
		return getBytes();
	}

	/**
	 * 获取响应输入流（与getInputStream()功能一致）
	 *
	 * @return 响应体输入流
	 */
	public InputStream inputStream() {
		return getInputStream();
	}

	/**
	 * 获取匹配名称的首个响应头
	 *
	 * @param name 响应头名称
	 * @return HeaderMeta实例（无匹配返回null）
	 */
	public HeaderMeta getHeader(String name) {
		return this.headersMeta.getHeader(name);
	}

	/**
	 * 获取匹配名称的首个响应头值
	 *
	 * @param name 响应头名称
	 * @return 响应头值（无匹配返回null）
	 */
	public String getHeaderValue(String name) {
		return this.headersMeta.getHeaderValue(name);
	}

	/**
	 * 获取匹配名称的所有响应头值
	 *
	 * @param name 响应头名称
	 * @return 响应头值列表（无匹配返回空列表）
	 */
	public List<String> getHeaderValues(String name) {
		return this.headersMeta.getHeaderValues(name);
	}

	/**
	 * 根据Cookie名称精准匹配获取对应的SetCookie对象
	 *
	 * @param name Cookie名称
	 * @return 匹配到的CookieMeta对象，无匹配项时返回null
	 */
	public CookieMeta getSetCookie(String name) {
		return this.headersMeta.getSetCookie(name);
	}

	/**
	 * 获取所有的Cookie对象
	 *
	 * @return 匹配到的CookieMeta对象，无匹配项时返回[]
	 */
	public List<CookieMeta> getSetCookie() {
		return this.headersMeta.getSetCookie();
	}

	/**
	 * 获取请求发起时间（与getRequestTime()功能一致）
	 *
	 * @return LocalDateTime实例（可为null）
	 */
	public LocalDateTime requestDate() {
		return this.requestTime;
	}

	/**
	 * 获取响应返回时间（与getResponseTime()功能一致）
	 *
	 * @return LocalDateTime实例（可为null）
	 */
	public LocalDateTime responseDate() {
		return this.responseTime;
	}

	/**
	 * 获取请求响应耗时（请求发起至响应返回的时间间隔）
	 *
	 * @return 耗时Duration对象
	 */
	public Duration between() {
		return Duration.between(requestTime, responseTime);
	}

	/**
	 * 获取请求响应耗时（分钟）
	 *
	 * @return 耗时分钟数（负数表示响应时间早于请求时间）
	 */
	public long betweenMinutes() {
		Duration between = between();
		return between.toMinutes();
	}

	/**
	 * 获取请求响应耗时（毫秒）
	 *
	 * @return 耗时毫秒数（负数表示响应时间早于请求时间）
	 */
	public long betweenMillis() {
		Duration between = between();
		return between.toMillis();
	}

	/**
	 * 获取请求响应耗时（秒）
	 *
	 * @return 耗时秒数（负数表示响应时间早于请求时间）
	 */
	public long betweenSeconds() {
		Duration between = between();
		return between.toSeconds();
	}

	/**
	 * 获取响应状态码（与getHttpStatus()功能一致）
	 *
	 * @return HTTP状态码
	 */
	public int status() {
		return this.httpStatus;
	}

	/**
	 * 判断响应是否成功（状态码200-299）
	 *
	 * @return true=响应成功，false=响应失败
	 */
	public boolean isSuccess() {
		return isStatusInRange(200, 299);
	}

	/**
	 * 判断响应是否为重定向（状态码300-399）
	 *
	 * @return true=重定向，false=非重定向
	 */
	public boolean isRedirect() {
		return isStatusInRange(300, 399);
	}

	/**
	 * 判断响应是否为客户端异常（状态码400-499）
	 *
	 * @return true=客户端异常，false=非客户端异常
	 */
	public boolean isClientError() {
		return isStatusInRange(400, 499);
	}

	/**
	 * 判断响应是否为服务端异常（状态码500-599）
	 *
	 * @return true=服务端异常，false=非服务端异常
	 */
	public boolean isServerError() {
		return isStatusInRange(500, 599);
	}

	/**
	 * 判断状态码是否在指定闭区间内
	 *
	 * @param min 最小状态码
	 * @param max 最大状态码
	 * @return true=状态码在区间内，false=状态码不在区间内
	 */
	public boolean isStatusInRange(int min, int max) {
		int status = this.status();
		return status >= min && status <= max;
	}

	/**
	 * 关闭响应对象
	 */
	public abstract void close();
}
