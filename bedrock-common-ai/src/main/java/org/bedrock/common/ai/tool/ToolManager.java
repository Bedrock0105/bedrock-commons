package org.bedrock.common.ai.tool;

import org.bedrock.common.ai.annotation.ToolProvider;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.code.util.StringUtil;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.boot.CommandLineRunner;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 工具注册中心。
 * <p>
 * 应用启动后扫描 {@link ToolProvider} 标注的 Bean，
 * 将元数据与 {@link ToolCallback} 一并注册，避免多 Map 同步维护。
 * </p>
 */
public class ToolManager implements CommandLineRunner {

	private final ToolCallbackResolver toolCallbackResolver;

	/**
	 * key: {@link ToolProvider#name()}，value: 工具组注册信息（含回调列表）。
	 */
	private final Map<String, ToolRegistration> registrations = new LinkedHashMap<>();

	public ToolManager(ToolCallbackResolver toolCallbackResolver) {
		this.toolCallbackResolver = toolCallbackResolver;
	}

	/**
	 * 按工具组名称获取回调列表。
	 *
	 * @param name 工具组标识，对应 {@link ToolProvider#name()}
	 */
	public List<ToolCallback> getCallbacks(String name) {
		ToolRegistration registration = registrations.get(name);
		return registration != null ? registration.callbacks() : Collections.emptyList();
	}

	/**
	 * 获取已注册工具的下拉选项。
	 *
	 * @return label 为展示名，value 为工具组 name
	 */
	public List<ToolOption> listOptions() {
		return registrations.values()
			.stream()
			.map(registration -> new ToolOption(registration.label(), registration.name()))
			.toList();
	}

	/**
	 * 注册工具组。
	 *
	 * @param name      工具组标识
	 * @param label     工具组展示名
	 * @param callbacks 回调列表
	 */
	public void register(String name, String label, List<ToolCallback> callbacks) {
		registrations.put(name, new ToolRegistration(name, label, callbacks));
	}

	/**
	 * 注册工具组。
	 *
	 * @param registration 工具组注册信息
	 */
	public void register(ToolRegistration registration) {
		registrations.put(registration.name(), new ToolRegistration(registration.name(), registration.label(), registration.callbacks()));
	}

	/**
	 * 工具组下拉项。
	 */
	public record ToolOption(String label, String value) {

	}

	public record ToolRegistration(String name, String label, List<ToolCallback> callbacks) {

	}

	@Override
	public void run(String... args) {
		SpringUtil.getApplicationContext()
			.getBeansWithAnnotation(ToolProvider.class)
			.forEach(this::register);
	}

	private void register(String beanName, Object bean) {
		ToolProvider provider = bean.getClass().getAnnotation(ToolProvider.class);
		if (provider == null) {
			return;
		}
		String name = StringUtil.isNotBlank(provider.name()) ? provider.name() : beanName;
		String label = StringUtil.isNotBlank(provider.label()) ? provider.label() : name;
		List<ToolCallback> callbacks = resolveCallbacks(beanName, bean, provider.kind());
		registrations.put(name, new ToolRegistration(name, label, callbacks));
	}

	private List<ToolCallback> resolveCallbacks(String beanName, Object bean, ToolProvider.Kind kind) {
		return switch (kind) {
			case FUNCTION -> {
				ToolCallback callback = toolCallbackResolver.resolve(beanName);
				yield callback != null ? List.of(callback) : List.of();
			}
			case METHOD -> List.of(ToolCallbacks.from(bean));
		};
	}

}
