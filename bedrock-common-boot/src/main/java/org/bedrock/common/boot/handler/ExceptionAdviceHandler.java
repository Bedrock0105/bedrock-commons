package org.bedrock.common.boot.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.log.exception.ServiceException;
import org.bedrock.common.code.api.R;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 统一异常拦截处理
 */
@Slf4j
@RestControllerAdvice
public class ExceptionAdviceHandler {

	@ExceptionHandler(ServiceException.class)
	public R<Void> serverException(ServiceException e) {
		log.error("ServerException: {}", e.getMessage(), e);
		return R.error(e.getCode(), e.getMessage());
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<R<Void>> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
		return new ResponseEntity<>(R.error(HttpServletResponse.SC_METHOD_NOT_ALLOWED, e.getMessage()), HttpStatus.METHOD_NOT_ALLOWED);
	}

	/**
	 * 请求路径中缺少必需的路径变量
	 */
	@ExceptionHandler(MissingPathVariableException.class)
	public R<Void> handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.error("请求路径中缺少必需的路径变量'{}',发生系统异常.", requestURI, e);
		return R.error(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
	}

	/**
	 * 请求参数类型不匹配
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<R<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
		log.error("请求参数类型不匹配,发生系统异常.{}", e.getMessage(), e);
		return new ResponseEntity<>(R.error(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
	}

	/**
	 * 自定义验证异常
	 */
	@ExceptionHandler(BindException.class)
	public R<Void> handleBindException(BindException e) {
		log.error("发送参数校验失败异常 {}", e.getMessage(), e);
		String message = e.getAllErrors().get(0).getDefaultMessage();
		return R.error(HttpServletResponse.SC_BAD_REQUEST, message);
	}

	/**
	 * 自定义验证异常
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		log.error("参数校验失败{}", e.getMessage(), e);
		// 提取详细的错误信息
		List<String> errors = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.toList();
		return R.error(HttpServletResponse.SC_BAD_REQUEST, String.join("; ", errors));
	}

	/**
	 * 约束校验异常（如@NotBlank等）
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	public R<Void> handleConstraintViolationException(ConstraintViolationException e) {
		log.error("约束校验失败: {}", e.getMessage(), e);
		List<String> errors = e.getConstraintViolations()
			.stream()
			.map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
			.collect(Collectors.toList());

		return R.error(HttpServletResponse.SC_BAD_REQUEST, String.join("; ", errors));
	}

	/**
	 * 媒体类型不支持
	 */
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<R<Void>> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
		log.error("媒体类型不支持: {}", e.getMessage(), e);
		return new ResponseEntity<>(R.error(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, e.getMessage()), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
	}

	/**
	 * 缺少请求参数
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<R<Void>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
		log.error("缺少请求参数: {}", e.getMessage(), e);
		return new ResponseEntity<>(R.error(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
	}

	/**
	 * 请求体缺失
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<R<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
		log.error("请求体解析失败: {}", e.getMessage(), e);
		return new ResponseEntity<>(R.error(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
	}

	/**
	 * 404处理 - 需要配置spring.mvc.throw-exception-if-no-handler-found=true
	 * spring.mvc.throw-exception-if-no-handler-found 当前版本默认是true 后面版本将会删除改属性
	 */
	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<R<Void>> handleNoHandlerFoundException(NoHandlerFoundException e) {
		log.error("接口不存在: {} {}", e.getHttpMethod(), e.getRequestURL());
		return new ResponseEntity<>(R.error(HttpServletResponse.SC_NOT_FOUND, e.getMessage()), HttpStatus.NOT_FOUND);
	}

	// 处理静态资源未找到的情况
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<R<Void>> handleNoResourceFound(NoResourceFoundException e) {
		log.error("资源不存在: {} {}", e.getHttpMethod(), e.getResourcePath());
		return new ResponseEntity<>(R.error(HttpServletResponse.SC_NOT_FOUND, e.getMessage()), HttpStatus.NOT_FOUND);
	}

	/**
	 * 文件大小超限
	 */
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<R<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
		log.error("文件大小超限: {}", e.getMessage(), e);
		return new ResponseEntity<>(R.error(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Throwable.class)
	public R<Void> handleException(Throwable e) {
		log.error("系统异常: {}", e.getMessage(), e);
		return R.error(e.getMessage());
	}

}
