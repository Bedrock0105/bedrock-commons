package org.bedrock.common.resource.model.oss;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * 文件上传配置模型
 */
@Getter
@Setter
public class OssProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 上传端点
	 */
	private String endpoint;

	/**
	 * 访问密钥ID
	 */
	private String accessKey;

	/**
	 * 访问密钥
	 */
	private String secretKey;

	/**
	 * 存储桶名称
	 */
	private String bucketName;

	/**
	 * 存储桶地域（如：cn-beijing/cn-shanghai/ap-hongkong）
	 */
	private String bucketRegion;

	/**
	 * 文件路径前缀
	 */
	@Nullable
	private String prefixPath = "upload";

	/**
	 * 公开访问URL
	 * 默认是 endpoint
	 */
	@Nullable
	private String publicUrl;

	/**
	 * 配置项
	 */
	@Nullable
	private Map<String, Objects> properties;
}
