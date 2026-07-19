package org.bedrock.common.boot.base;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bedrock.common.code.api.R;
import org.bedrock.common.code.util.IoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class BaseController {

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private HttpServletResponse response;

	/**
	 * 返回成功，或者失败
	 */
	public <T> R<T> status(boolean status) {
		return R.status(status);
	}

	/**
	 * 默认成功
	 */
	public <T> R<T> success() {
		return R.success();
	}

	/**
	 * 默认成功
	 */
	public <T> R<T> success(T data) {
		return R.success(data);
	}

	/**
	 * 默认成功
	 */
	public <T> R<T> success(String msg, T data) {
		return R.success(msg, data);
	}

	/**
	 * 默认失败
	 */

	public <T> R<T> failure() {
		return R.failure();
	}

	/**
	 * 默认失败
	 */
	public <T> R<T> failure(int code) {
		return R.failure(code);
	}

	/**
	 * 默认失败
	 */
	public <T> R<T> failure(String msg) {
		return R.failure(msg);
	}

	/**
	 * 默认失败
	 */
	public <T> R<T> failure(int code, String msg) {
		return R.failure(code, msg);
	}

	/**
	 * 服务器异常
	 */
	public <T> R<T> error() {
		return R.error();
	}

	/**
	 * 服务器异常
	 */
	public <T> R<T> error(int code) {
		return R.error(code);
	}

	/**
	 * 服务器异常
	 */
	public <T> R<T> error(String msg) {
		return R.error(msg);
	}

	/**
	 * 服务器异常
	 */
	public <T> R<T> error(int code, String msg) {
		return R.error(code, msg);
	}

	/**
	 * 直接下载文件，通过字节数组读取内容并写入响应流
	 *
	 * @param inputStream 要下载的文件流
	 * @param fileName    下载的文件名（包含扩展名）
	 */
	public void downloadFile(InputStream inputStream, String fileName) throws IOException {
		downloadFile(inputStream, fileName, false);
	}

	/**
	 * 直接下载文件，通过字节数组读取内容并写入响应流
	 *
	 * @param inputStream 要下载的文件流
	 * @param fileName    下载的文件名（包含扩展名）
	 * @param inline      是否在线打开（true）或附件下载（false）
	 */
	public void downloadFile(InputStream inputStream, String fileName, boolean inline) throws IOException {
		downloadFile(inputStream,
			fileName,
			MediaTypeFactory
				.getMediaType(fileName)
				.orElse(MediaType.APPLICATION_OCTET_STREAM).toString(),
			inline);
	}

	/**
	 * 直接下载文件，通过字节数组读取内容并写入响应流
	 *
	 * @param fileBytes 要下载的文件字节数组
	 * @param fileName  下载的文件名（包含扩展名）
	 */
	public void downloadFile(byte[] fileBytes, String fileName) throws IOException {
		downloadFile(fileBytes, fileName, false);
	}

	/**
	 * 直接下载文件，通过字节数组读取内容并写入响应流
	 *
	 * @param fileBytes 要下载的文件字节数组
	 * @param fileName  下载的文件名（包含扩展名）
	 * @param inline    是否在线打开（true）或附件下载（false）
	 */
	public void downloadFile(byte[] fileBytes, String fileName, boolean inline) throws IOException {
		downloadFile(fileBytes,
			fileName,
			MediaTypeFactory
				.getMediaType(fileName)
				.orElse(MediaType.APPLICATION_OCTET_STREAM).toString(),
			inline);
	}

	/**
	 * 直接下载文件，通过字节数组读取内容并写入响应流
	 *
	 * @param fileBytes   要下载的文件字节数组
	 * @param fileName    下载的文件名（包含扩展名）
	 * @param contentType 文件MIME类型，如application/pdf、image/jpeg等
	 * @param inline      是否在线打开（true）或附件下载（false）
	 */
	public void downloadFile(byte[] fileBytes, String fileName, String contentType, boolean inline) throws IOException {
		// 创建一个输入流对象
		InputStream inputStream = new java.io.ByteArrayInputStream(fileBytes);
		// 调用下载文件方法
		downloadFile(inputStream, fileName, contentType, inline);
	}

	/**
	 * 直接下载文件，通过InputStream读取内容并写入响应流
	 *
	 * @param inputStream 要下载的文件输入流
	 * @param fileName    下载的文件名（包含扩展名）
	 * @param contentType 文件MIME类型，如application/pdf、image/jpeg等
	 * @param inline      是否在线打开（true）或附件下载（false）
	 */
	public void downloadFile(InputStream inputStream, String fileName, String contentType, boolean inline) throws IOException {
		// 设置响应内容类型
		if (contentType == null || contentType.trim().isEmpty()) {
			contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 默认二进制流
		}
		response.setContentType(contentType);
		// 处理文件名编码（解决中文乱码问题）
		String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
			.replaceAll("\\+", "%20");
		// 设置下载方式：附件下载或在线打开
		String dispositionType = inline ? "inline" : "attachment";
		response.setHeader("Content-Disposition", dispositionType + "; filename*=UTF-8''" + encodedFileName);
		try (inputStream; ServletOutputStream out = response.getOutputStream();) {
			IoUtil.copy(inputStream, out);
		}
	}
}
