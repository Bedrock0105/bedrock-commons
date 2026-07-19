package org.bedrock.common.excel.support;

import cn.idev.excel.read.listener.ReadListener;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.util.BeanUtil;
import org.bedrock.common.excel.annotation.RequestExcel;
import org.bedrock.common.excel.listener.ExcelListener;
import org.bedrock.common.excel.listener.IReadListener;
import org.bedrock.common.excel.util.ExcelUtil;
import org.bedrock.common.log.exception.ServiceException;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;

import java.io.InputStream;
import java.util.List;

/**
 * 参数解析器
 */
@Slf4j
public class RequestExcelArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(RequestExcel.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
								  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		RequestExcel requestExcel = parameter.getParameterAnnotation(RequestExcel.class);
		Assert.notNull(requestExcel, "@RequestExcel annotation cannot be null");

		if (!List.class.isAssignableFrom(parameter.getParameterType()) &&
			!ReadListener.class.isAssignableFrom(parameter.getParameterType())) {
			throw new ServiceException("The @RequestExcel annotation can only be applied to parameters of type List<T> or IReadListener<T>");
		}
		MultipartFile file = extractMultipartFile(webRequest, requestExcel);
		IReadListener listener = instantiateReadListener(requestExcel);
		validateRequestExcelParams(requestExcel);

		Class<?> parameterType = parameter.getParameterType();
		if (List.class.isAssignableFrom(parameterType)) {
			Class<?> head = getHeadClass(parameter, requestExcel);
			setListenerSkipRowNum(listener, requestExcel);
			readExcelData(file.getInputStream(), head, listener, requestExcel.sheetNo(), requestExcel.headRowNumber());
			return listener.getData();
		} else if (ReadListener.class.isAssignableFrom(parameterType)) {
			Class<?> head = requestExcel.head();
			if (head == Void.class) {
				throw new ServiceException("Head class must be specified when parameter type is IReadListener<T>");
			}
			setListenerSkipRowNum(listener, requestExcel);
			readExcelData(file.getInputStream(), head, listener, requestExcel.sheetNo(), requestExcel.headRowNumber());
			return listener;
		}

		throw new ServiceException("Unsupported parameter type for @RequestExcel");
	}

	/**
	 * 提取Multipart文件
	 */
	private MultipartFile extractMultipartFile(NativeWebRequest webRequest, RequestExcel requestExcel) {
		HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		Assert.state(servletRequest != null, "No HttpServletRequest");

		MultipartHttpServletRequest multipartRequest =
			WebUtils.getNativeRequest(servletRequest, MultipartHttpServletRequest.class);
		if (multipartRequest == null) {
			multipartRequest = new StandardMultipartHttpServletRequest(servletRequest);
		}

		MultipartFile file = multipartRequest.getFile(requestExcel.value());
		if (file == null) {
			String message = String.format("Multipart file with name '%s' not found", requestExcel.value());
			log.error(message);
			throw new MultipartException(message);
		}
		return file;
	}

	/**
	 * 实例化监听器（支持Spring Bean）
	 */
	private IReadListener instantiateReadListener(RequestExcel requestExcel) {
		return BeanUtil.instantiateClass(requestExcel.listener());
	}

	/**
	 * 校验sheetNo/headRowNumber合法性
	 */
	private void validateRequestExcelParams(RequestExcel requestExcel) {
		if (requestExcel.sheetNo() < 0 || requestExcel.headRowNumber() < 0) {
			throw new ServiceException("sheetNo and headRowNumber must be non-negative");
		}
	}

	/**
	 * 获取Excel头类（处理List泛型）
	 */
	private Class<?> getHeadClass(MethodParameter parameter, RequestExcel requestExcel) {
		Class<?> head = requestExcel.head();
		if (head != Void.class) {
			return head;
		}

		ResolvableType genericType = ResolvableType.forMethodParameter(parameter).getGeneric(0);
		head = genericType.resolve();
		if (head == null) {
			String message = "List<T> must specify a concrete generic type (cannot be List<?>)";
			log.error(message);
			throw new ServiceException(message);
		}
		return head;
	}

	/**
	 * 设置监听器跳过行数
	 */
	private void setListenerSkipRowNum(IReadListener listener, RequestExcel requestExcel) {
		if (listener instanceof ExcelListener excelListener) {
			excelListener.setSkipRowNum(requestExcel.skipRowNum());
		}
	}

	/**
	 * 统一读取Excel数据
	 */
	private void readExcelData(InputStream inputStream, Class<?> head, IReadListener listener,
							   int sheetNo, int headRowNumber) {
		try (inputStream) { // 自动关闭输入流
			ExcelUtil.readExcel(inputStream, head, listener, sheetNo, headRowNumber);
		} catch (Exception e) {
			log.error("Excel read failed for head class: {}", head.getName(), e);
			throw new ServiceException("Excel parsing failed", e);
		}
	}
}
