package org.bedrock.common.http.interceptor.impl;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.reflects.MethodMeta;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.HttpResponse;
import org.bedrock.common.http.annotation.Body;
import org.bedrock.common.http.enums.MediaTypeCategory;
import org.bedrock.common.http.interceptor.Interceptor;
import org.bedrock.common.http.support.HttpSupport;
import org.bedrock.common.http.support.InterceptorChain;
import org.bedrock.common.spi.autoservice.AutoService;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;

import java.io.File;
import java.io.InputStream;

/**
 * 处理以下注解
 *
 * @see Body
 */
@Slf4j
@AutoService(Interceptor.class)
public class BodyInterceptor implements Interceptor {

	@Override
	public HttpResponse interceptor(HttpRequest request, MethodMeta methodMeta, InterceptorChain chain) {
		// 获取请求级Content-Type，兜底为普通表单格式
		MediaTypeCategory mediaTypeCategory = request.getMediaTypeCategory();
		/**
		 * 解析方法参数上的@Body注解，按Content-Type分发参数（兼容image/png等所有文件类型）
		 * 核心优化：
		 * 1.  区分「请求级Content-Type」和「文件级Content-Type」，兼容多文件/单文件上传场景
		 * 2.  支持image/png、application/pdf等具体文件类型的处理
		 * 3.  优化逻辑结构，增强空值防护和可读性
		 */
		for (MethodParameter parameter : methodMeta.getParameters()) {
			Body bodyAnnotation = parameter.getParameterAnnotation(Body.class);
			if (bodyAnnotation == null) {
				continue;
			}

			int paramIndex = parameter.getParameterIndex();
			String bodyName = getName(bodyAnnotation);
			String bodyFileName = bodyAnnotation.fileName();
			String bodyContentType = bodyAnnotation.contentType();
			boolean isEncoded = bodyAnnotation.encoded();
			Object bodyValue = request.args(paramIndex);

			// 1. 处理普通表单场景（application/x-www-form-urlencoded）
			if (MediaTypeCategory.FORM_URLENCODED.equals(mediaTypeCategory)) {
				handleFormContentType(request, bodyName, bodyValue, isEncoded);
				continue;
			}

			// 2. 处理结构化/纯文本请求体场景（JSON/XML/TEXT_PLAIN/TEXT_HTML等）
			if (MediaTypeCategory.STRING_REQUEST_BODY.equals(mediaTypeCategory)) {
				handleStructuredBodyContentType(request, bodyName, bodyValue);
				continue;
			}
			// 3. 处理文件上传场景（兼容所有二进制文件类型：multipart/form-data等）
			if (MediaTypeCategory.MULTIPART_FORM_DATA.equals(mediaTypeCategory)) {
				handleFileUploadContentType(request, bodyName, bodyFileName, bodyValue, bodyContentType);
				continue;
			}
			// 4. 处理二进制数据场景（如application/octet-stream、image/png）
			request.addBinaryData(bodyName, bodyValue);

		}
		return chain.doInterceptor(request, methodMeta);
	}

	/**
	 * 处理普通表单（application/x-www-form-urlencoded）场景
	 *
	 * @param request   HTTP请求实例
	 * @param bodyName  注解配置的参数名
	 * @param bodyValue 参数值
	 * @param isEncoded 是否启用URL编码
	 */
	private void handleFormContentType(HttpRequest request, String bodyName, Object bodyValue, boolean isEncoded) {
		if (isEncoded) {
			request.addForm(bodyName, bodyValue);
		} else {
			request.addFormEncoded(bodyName, bodyValue);
		}
	}

	/**
	 * 处理结构化/纯文本请求体场景
	 *
	 * @param request   HTTP请求实例
	 * @param bodyName  注解配置的参数名
	 * @param bodyValue 参数值
	 */
	private void handleStructuredBodyContentType(HttpRequest request, String bodyName, Object bodyValue) {
		if (StringUtil.isBlank(bodyName)) {
			request.addBody(bodyValue);
		} else {
			request.addBody(bodyName, bodyValue);
		}
	}

	/**
	 * 处理文件上传场景（兼容所有二进制文件类型：multipart/form-data、application/octet-stream、image/png等）
	 *
	 * @param request         HTTP请求实例
	 * @param bodyName        注解配置的参数名
	 * @param bodyFileName    注解配置的文件名
	 * @param bodyValue       参数值
	 * @param bodyContentType 注解配置的文件级Content-Type
	 */
	private void handleFileUploadContentType(HttpRequest request, String bodyName, String bodyFileName, Object bodyValue, String bodyContentType) {
		// 解析文件名占位符，兜底为空字符串
		String resolvedFileName = HttpSupport.resolvePlaceholdersParam(bodyFileName, request);
		resolvedFileName = StringUtil.toStr(resolvedFileName, "");

		// 优先使用注解配置的Content-Type（支持image/png、application/pdf等具体类型），兜底为空字符串
		String resolvedContentType = StringUtil.toStr(bodyContentType, "");

		// 1. 若注解指定了文件级Content-Type（如image/png），直接使用该类型添加多部分表单
		if (StringUtil.isNotBlank(resolvedContentType)) {
			request.addMultipartForm(bodyName, resolvedFileName, bodyValue, resolvedContentType);
			return;
		}

		// 2. 按参数类型自动适配文件上传（byte[]/File/InputStream优先，其他类型兜底）
		if (bodyValue instanceof byte[] bytes) {
			request.addMultipartForm(bodyName, resolvedFileName, bytes);
		} else if (bodyValue instanceof File file) {
			request.addMultipartForm(bodyName, resolvedFileName, file);
		} else if (bodyValue instanceof InputStream inputStream) {
			request.addMultipartForm(bodyName, resolvedFileName, inputStream);
		} else {
			// 兜底处理：传入普通对象，使用注解配置的ContentType（已兜底为空）
			request.addMultipartForm(bodyName, resolvedFileName, bodyValue, resolvedContentType);
		}
	}

	public String getName(Body body) {
		return StringUtil.isNotBlank(body.name()) ? body.name() : body.value();
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 100000;
	}
}
