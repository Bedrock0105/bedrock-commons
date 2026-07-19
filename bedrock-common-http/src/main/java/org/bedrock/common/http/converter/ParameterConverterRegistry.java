package org.bedrock.common.http.converter;

import lombok.Getter;
import org.bedrock.common.code.util.SpringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;

import java.util.concurrent.ConcurrentHashMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.ServiceLoader;

/**
 * 参数转换器注册中心
 * 管理系统默认转换器列表，支持SPI自动加载和手动维护，保证线程安全
 */
@Getter
public class ParameterConverterRegistry {

    /**
     * 系统默认参数转换器集合，线程安全，初始化时通过SPI加载填充
     */
    private final Set<ParameterConverter> defaultConverters = new HashSet<>();

    /**
     * 转换器类型与实例的映射表，并发安全，用于快速获取指定类型转换器
     */
    private final Map<Class<? extends ParameterConverter>, ParameterConverter> converterMap = new ConcurrentHashMap<>();

    /**
     * 初始化注册中心，自动加载ClassPath下所有ParameterConverter的SPI实现类
     */
    public ParameterConverterRegistry() {
        ServiceLoader.load(ParameterConverter.class)
                .forEach(defaultConverters::add);
    }

    /**
     * 手动添加默认转换器，补充SPI加载的列表，线程安全
     *
     * @param converter 待添加的参数转换器（不可为null）
     */
    public void addDefaultConverter(ParameterConverter converter) {
        defaultConverters.add(converter);
    }

    /**
     * 从默认列表移除指定转换器，线程安全
     *
     * @param converter 待移除的参数转换器
     */
    public void removeDefaultConverter(ParameterConverter converter) {
        defaultConverters.remove(converter);
    }

    /**
     * 获取指定类型的转换器实例，不存在则创建
     *
     * @param clazz 转换器类型
     * @return 对应类型的转换器实例
     */
    public ParameterConverter getConverter(Class<? extends ParameterConverter> clazz) {
        return converterMap.computeIfAbsent(clazz, this::createConverter);
    }

    /**
     * 创建转换器实例，优先从Spring容器获取，兜底反射实例化
     *
     * @param clazz 转换器类型
     * @return 转换器实例
     */
    private ParameterConverter createConverter(Class<? extends ParameterConverter> clazz) {
        if (SpringUtil.getApplicationContext() != null) {
            ObjectProvider<? extends ParameterConverter> beanProvider = SpringUtil.getApplicationContext().getBeanProvider(clazz);
            ParameterConverter springBean = beanProvider.getIfAvailable();
            if (springBean != null) {
                return springBean;
            }
        }
        return BeanUtils.instantiateClass(clazz);
    }
}
