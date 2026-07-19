package org.bedrock.common.http.executor.impl.okhttp;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.HttpResponse;
import org.bedrock.common.http.exception.HttpException;
import org.bedrock.common.http.model.ContentType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * HttpResponse的OkHttp实现类
 * 封装OkHttp客户端的响应信息，实现核心数据（字节数组/输入流）的获取逻辑
 */
public class OkHttpResponse extends HttpResponse {

	/**
	 * OkHttp原始响应对象
	 */
	private Response response;

	private final Object closeLock = new Object();

	/**
	 * OkHttp响应体（默认空响应体，避免空指针异常）
	 */
	private ResponseBody body = Util.EMPTY_RESPONSE;

	/**
	 * 响应字节数组缓存（懒加载，避免重复读取响应体）
	 */
	private byte[] bytes;

	/**
	 * 构造方法（正常响应场景）
	 *
	 * @param request      请求对象
	 * @param response     OkHttp原始响应实例
	 * @param requestTime  请求发起时间
	 * @param responseTime 响应返回时间
	 */
	public OkHttpResponse(HttpRequest request, Response response, LocalDateTime requestTime, LocalDateTime responseTime) {
		this.httpStatus = response.code();
		this.request = request;
		this.requestTime = requestTime;
		this.responseTime = responseTime;
		this.response = response;
		ResponseBody body = response.body();
		if (body != null) {
			this.body = body;
			// 设置响应编码与内容类型
			MediaType mediaType = body.contentType();
			if (mediaType != null) {
				String mediaTypeString = mediaType.toString();
				this.contentType = ContentType.create(mediaTypeString);
				Charset charset = mediaType.charset();
				if (charset != null) {
					this.contentType.setCharset(charset);
					this.charset = charset;
				}
			}
		}

		// 设置响应头信息
		Map<String, List<String>> multimap = response.headers().toMultimap();
		multimap.forEach((k, v) -> v.forEach(value -> headersMeta.addHeader(k, value)));

	}

	/**
	 * 构造方法（异常响应场景）
	 *
	 * @param request      请求对象
	 * @param throwable    异常实例
	 * @param requestTime  请求发起时间
	 * @param responseTime 响应返回时间
	 */
	public OkHttpResponse(HttpRequest request, Throwable throwable, LocalDateTime requestTime, LocalDateTime responseTime) {
		this.httpStatus = 500; // 默认标记为服务端异常
		this.request = request;
		this.requestTime = requestTime;
		this.responseTime = responseTime;
		this.exception = throwable;
	}

	/**
	 * 重写：懒加载获取响应字节数组
	 * 缓存结果到bytes属性，避免重复读取OkHttp响应体
	 *
	 * @return 响应体字节数组
	 * @throws HttpException 读取响应体字节数组失败时抛出
	 */
	@Override
	public byte[] getBytes() {
		byte[] bytes = this.bytes;
		if (bytes == null) {
			try {
				// 读取字节数组并缓存
				bytes = this.bytes = body.bytes();
			} catch (IOException e) {
				throw new HttpException("读取OkHttp响应体字节数组失败", e);
			} finally {
				close();
			}
		}
		return bytes;
	}

	/**
	 * 重写：获取响应体输入流
	 *
	 * @return OkHttp响应体对应的输入流
	 */
	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(getBytes());
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}
		synchronized (closeLock) {
			if (closed) {
				return;
			}
			closed = true;
		}
		if (body != null) {
			try {
				Util.closeQuietly(this.body);
			} catch (Throwable th) {
				throw new HttpException("关闭响应体输入流失败", th);
			} finally {
				closed = true;
			}
		}
	}
}
