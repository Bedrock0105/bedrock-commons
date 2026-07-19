package org.bedrock.common.resource.factory;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.resource.enums.OssEnum;
import org.bedrock.common.resource.model.oss.OssProperties;
import org.bedrock.common.resource.service.oss.OssTemplate;
import org.bedrock.common.resource.support.oss.OssFactoryBean;
import org.springframework.beans.factory.ObjectProvider;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 生成资源上传模板
 */
@Slf4j
public class OssBeanFactory {

	private final Map<OssEnum, OssFactoryBean> ossFactoryBeanMap;

	/**
	 * 获取资源上传模板
	 */
	@Nullable
	public OssTemplate getOssTemplate(OssEnum ossEnum, OssProperties properties) {
		OssFactoryBean ossFactoryBean = ossFactoryBeanMap.get(ossEnum);
		if (ossFactoryBean == null) {
			log.error("未找到对应的资源上传模板{},请检测是否引用了相关依赖 OssEnum 枚举类型上面有标注", ossEnum);
			throw new IllegalArgumentException("未找到对应的资源上传模板");
		}
		return ossFactoryBean.createOssTemplate(properties);
	}

	/**
	 * 构造函数
	 * 生成bean 工厂
	 */
	public OssBeanFactory(ObjectProvider<List<OssFactoryBean>> ossFactoryBeans) {
		ossFactoryBeanMap = new EnumMap<>(OssEnum.class);
		ossFactoryBeans.ifAvailable(list -> list
			.forEach(bean -> ossFactoryBeanMap
				.put(bean.getOssEnum(), bean)));

	}
}
