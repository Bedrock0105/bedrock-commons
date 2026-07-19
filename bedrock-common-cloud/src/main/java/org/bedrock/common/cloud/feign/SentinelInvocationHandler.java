/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bedrock.common.cloud.feign;

import com.alibaba.cloud.sentinel.feign.SentinelContractHolder;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import feign.Feign;
import feign.InvocationHandlerFactory.MethodHandler;
import feign.MethodMetadata;
import feign.Target;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.api.R;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.lang.reflect.*;
import java.util.*;

import static feign.Util.checkNotNull;

/**
 * {@link InvocationHandler} handle invocation that protected by Sentinel.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Slf4j
public class SentinelInvocationHandler implements InvocationHandler {

	private final Target<?> target;

	private final Map<Method, MethodHandler> dispatch;

	private FallbackFactory fallbackFactory;

	private Map<Method, Method> fallbackMethodMap;

	SentinelInvocationHandler(Target<?> target, Map<Method, MethodHandler> dispatch,
							  FallbackFactory fallbackFactory) {
		this.target = checkNotNull(target, "target");
		this.dispatch = checkNotNull(dispatch, "dispatch");
		this.fallbackFactory = fallbackFactory;
		this.fallbackMethodMap = toFallbackMethod(dispatch);
	}

	SentinelInvocationHandler(Target<?> target, Map<Method, MethodHandler> dispatch) {
		this.target = checkNotNull(target, "target");
		this.dispatch = checkNotNull(dispatch, "dispatch");
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args)
		throws Throwable {
		if ("equals".equals(method.getName())) {
			try {
				Object otherHandler = args.length > 0 && args[0] != null
					? Proxy.getInvocationHandler(args[0])
					: null;
				return equals(otherHandler);
			} catch (IllegalArgumentException e) {
				return false;
			}
		} else if ("hashCode".equals(method.getName())) {
			return hashCode();
		} else if ("toString".equals(method.getName())) {
			return toString();
		}

		Object result;
		MethodHandler methodHandler = this.dispatch.get(method);
		// only handle by HardCodedTarget
		if (target instanceof Target.HardCodedTarget hardCodedTarget) {
			MethodMetadata methodMetadata = SentinelContractHolder.METADATA_MAP
				.get(hardCodedTarget.type().getName()
					+ Feign.configKey(hardCodedTarget.type(), method));
			// resource default is HttpMethod:protocol://url
			if (methodMetadata == null) {
				result = methodHandler.invoke(args);
			} else {
				String resourceName = methodMetadata.template().method().toUpperCase()
					+ ":" + hardCodedTarget.url() + methodMetadata.template().path();
				Entry entry = null;
				try {
					ContextUtil.enter(resourceName);
					entry = SphU.entry(resourceName, EntryType.OUT, 1, args);
					result = methodHandler.invoke(args);
				} catch (Throwable ex) {
					// fallback handle
					if (!BlockException.isBlockException(ex)) {
						Tracer.traceEntry(ex, entry);
					}
					if (fallbackFactory != null) {
						try {
							Object fallbackResult = fallbackMethodMap.get(method)
								.invoke(fallbackFactory.create(ex), args);
							return fallbackResult;
						} catch (IllegalAccessException e) {
							// shouldn't happen as method is public due to being an
							// interface
							throw new AssertionError(e);
						} catch (InvocationTargetException e) {
							throw e.getCause();
						}
					} else {
						return defaultFallbackFactory(method, ex);
						// throw exception if fallbackFactory is null
//						throw ex;
					}
				} finally {
					if (entry != null) {
						entry.exit(1, args);
					}
					ContextUtil.exit();
				}
			}
		} else {
			// other target type using default strategy
			result = methodHandler.invoke(args);
		}

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SentinelInvocationHandler sentinelInvocationHandler) {
			return target.equals(sentinelInvocationHandler.target);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return target.hashCode();
	}

	@Override
	public String toString() {
		return target.toString();
	}

	static Map<Method, Method> toFallbackMethod(Map<Method, MethodHandler> dispatch) {
		Map<Method, Method> result = new LinkedHashMap<>();
		for (Method method : dispatch.keySet()) {
			method.setAccessible(true);
			result.put(method, method);
		}
		return result;
	}

	private Object defaultFallbackFactory(Method method, Throwable cause) {
		log.error("Sentinel fallback error: {}", cause.getMessage(), cause);
		Class<?> returnType = method.getReturnType();
		// 1. 处理void类型
		if (returnType == void.class) {
			return null;
		}
		// 2. 处理自定义响应类R
		if (returnType == R.class) {
			return R.error(cause.getMessage());
		}
		// 3. 处理基本类型（int、long、boolean等）
		if (returnType.isPrimitive()) {
			return getPrimitiveDefaultValue(returnType);
		}
		// 4. 处理基本类型包装类（Integer、Long、Boolean等）
		if (isWrapperType(returnType)) {
			return getWrapperDefaultValue(returnType);
		}
		// 5. 处理数组类型（如int[]、String[]、Object[]）
		if (returnType.isArray()) {
			return Array.newInstance(returnType.getComponentType(), 0); // 返回空数组
		}
		// 6. 处理集合类型（List、Map、Set）
		if (List.class.isAssignableFrom(returnType)) {
			return Collections.emptyList(); // 空List（不可变，如需可变可返回new ArrayList<>()）
		}
		if (Map.class.isAssignableFrom(returnType)) {
			return Collections.emptyMap(); // 空Map
		}
		if (Set.class.isAssignableFrom(returnType)) {
			return Collections.emptySet(); // 空Set
		}
		if (Collection.class.isAssignableFrom(returnType)) {
			return Collections.emptyList(); // 空Collection
		}
		// 7. 处理字符串
		if (returnType == String.class) {
			return ""; // 返回空字符串（而非null）
		}
		// 8. 其他引用类型（返回null，或根据业务返回默认实例）
		// 注意：如果是自定义POJO，可根据需求实例化（需无参构造），这里默认返回null
		return null;
	}

	// 辅助方法：获取基本类型的默认值
	private Object getPrimitiveDefaultValue(Class<?> primitiveType) {
		if (primitiveType == int.class) return 0;
		if (primitiveType == long.class) return 0L;
		if (primitiveType == short.class) return (short) 0;
		if (primitiveType == byte.class) return (byte) 0;
		if (primitiveType == float.class) return 0.0f;
		if (primitiveType == double.class) return 0.0d;
		if (primitiveType == boolean.class) return false;
		if (primitiveType == char.class) return '\0';
		return null; // 理论上不会走到这里
	}

	// 辅助方法：判断是否为基本类型包装类
	private boolean isWrapperType(Class<?> clazz) {
		return clazz == Integer.class || clazz == Long.class || clazz == Short.class
			|| clazz == Byte.class || clazz == Float.class || clazz == Double.class
			|| clazz == Boolean.class || clazz == Character.class;
	}

	private Object getWrapperDefaultValue(Class<?> wrapperType) {
		// 包装类默认值通常返回null，但也可返回对应基本类型的包装（如0→Integer.valueOf(0)）
		// 根据业务需求选择，这里返回null（更符合包装类的默认语义）
		return null;
		// 若需要返回非null值，可改为：
		// if (wrapperType == Integer.class) return 0;
		// ... 其他类型类似
	}
}
