package org.bedrock.common.code.factory;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.lang.Nullable;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * @Classname DefaultPropertySourceFactory
 * @Description 描述

 */
public class DefaultPropertySourceFactory extends org.springframework.core.io.support.DefaultPropertySourceFactory {
    @Override
    public PropertySource<?> createPropertySource(@Nullable String name, EncodedResource resource) throws IOException {
        String filename = resource.getResource().getFilename();
        if (filename != null && filename.endsWith(".properties")) {
            return super.createPropertySource(name, resource);
        } else if (filename != null && (filename.endsWith(".yml") || filename.endsWith(".yaml"))) {
            // 加载 Yaml 格式的属性文件
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource.getResource());
            Properties properties = factory.getObject();
            return new PropertiesPropertySource(StringUtils.hasText(name) ? name : filename, properties);
        } else if (filename != null && filename.endsWith(".xml")) {
            // 加载 XML 格式的属性文件
            Properties properties = new Properties();
            DefaultPropertiesPersister persister = new DefaultPropertiesPersister();
            persister.loadFromXml(properties, resource.getInputStream());
            return new PropertiesPropertySource(StringUtils.hasText(name) ? name : filename, properties);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + filename);
        }
    }
}
