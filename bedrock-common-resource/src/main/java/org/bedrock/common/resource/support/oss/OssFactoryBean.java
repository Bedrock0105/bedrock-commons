package org.bedrock.common.resource.support.oss;

import org.bedrock.common.resource.enums.OssEnum;
import org.bedrock.common.resource.model.oss.OssProperties;
import org.bedrock.common.resource.service.oss.OssTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface OssFactoryBean {

	/**
	 * 获取OSS枚举
	 */
	@NonNull
	OssEnum getOssEnum();

	/**
	 * 获取OSS模板
	 */
	@Nullable
	OssTemplate createOssTemplate(OssProperties properties);

	default boolean isSupport(@NonNull OssEnum ossEnum) {
		return ossEnum == getOssEnum();
	}
}
