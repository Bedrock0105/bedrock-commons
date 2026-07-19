package org.bedrock.common.http.support;

import org.bedrock.common.code.reflects.MethodMeta;
import org.bedrock.common.http.HttpConfiguration;
import org.bedrock.common.http.HttpRequest;
import org.bedrock.common.http.annotation.Request;
import org.bedrock.common.http.interceptor.Interceptor;
import org.bedrock.common.http.model.RequestMeta;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * 接口普通业务方法的执行载体（核心业务逻辑封装类）
 * 职责：
 * 1. 封装方法元数据（MethodMeta）、全局配置（Configuration）、所属接口信息，聚合方法执行的核心依赖
 * 2. 提供统一的 execute 方法入口，承载普通业务方法的核心执行逻辑（如参数解析、远程调用、结果封装等）
 * 3. 解耦方法执行逻辑与代理调用器（PlainMethodInvoker），符合单一职责原则
 */
public class ClientMethod {

	/**
	 * 方法元数据对象（核心）
	 * 封装Method的静态信息：方法名、参数类型/个数、返回值类型、注解信息等，避免重复解析Method对象提升性能
	 */
	private final MethodMeta methodMeta;

	/**
	 * 全局配置对象
	 * 提供方法执行所需的配置项：如接口调用超时、远程服务地址、序列化规则、拦截器链等
	 */
	private final HttpConfiguration config;

	/**
	 * 方法所属的接口Class对象
	 * 用于关联接口级配置（如接口专属服务地址）、标识方法归属，或扩展接口级拦截逻辑
	 */
	private final RequestMeta requestMeta;

	/**
	 * 请求拦截器集合
	 * <p>存储请求生命周期的拦截器（如请求前参数校验、请求后响应处理），默认初始化空 HashSet，保证非空且去重</p>
	 *
	 * @see Interceptor
	 */
	private final Set<Interceptor> interceptors = new HashSet<>();

	/**
	 * 构造器：初始化业务方法执行的核心依赖
	 *
	 * @param requestMeta 方法所属的接口Class对象（非空，标识方法归属）
	 * @param method      目标业务方法对象（非空，用于解析方法元数据）
	 * @param config      全局配置对象（非空，提供方法执行的配置支撑）
	 */
	public ClientMethod(RequestMeta requestMeta, Method method, HttpConfiguration config) {
		// 解析Method为元数据对象，封装静态信息避免重复解析
		this.methodMeta = MethodMeta.forMethod(method);
		this.config = config;
		this.requestMeta = requestMeta;
		initInterceptors();
	}

	/**
	 * 核心方法：执行普通业务方法的逻辑
	 * 扩展方向（待实现）：
	 * 1. 参数校验与转换：基于methodMeta校验参数个数/类型，适配远程调用参数格式
	 * 2. 配置读取：从config获取接口/方法级配置（如超时、服务地址）
	 * 3. 业务逻辑执行：如HTTP/RPC远程调用、本地逻辑处理、数据持久化等
	 * 4. 结果封装与转换：将执行结果适配为方法返回值类型
	 * 5. 异常处理：统一捕获执行异常，转换为业务异常抛出
	 *
	 * @param args 方法调用的参数数组（无参时为null，需与methodMeta的参数定义匹配）
	 * @return 方法执行结果（需与目标方法的返回值类型一致，当前占位返回null）
	 */
	public Object execute(Object[] args) {
		HttpRequest httpRequest = new HttpRequest(config, methodMeta, args);
		httpRequest.setRequestMeta(requestMeta);
		/**
		 * 添加请求拦截器
		 */
		httpRequest.interceptors(interceptors);
		return httpRequest.execute();
	}

	/**
	 * 初始化请求拦截器
	 */
	public void initInterceptors() {
		interceptors.addAll(requestMeta.getInterceptors());
		Request request = this.methodMeta.getMergedAnnotation(Request.class);
		if (request != null) {
			for (Class<? extends Interceptor> interceptor : request.interceptors()) {
				interceptors.add(config.getInterceptor(interceptor));
			}
		}

	}

	/**
	 * 默认业务方法执行器接口
	 */
	public interface DefaultClient {

		HttpRequest createHttpRequest();
	}
}
