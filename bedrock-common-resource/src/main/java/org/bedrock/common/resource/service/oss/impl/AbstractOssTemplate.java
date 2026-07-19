package org.bedrock.common.resource.service.oss.impl;

import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.util.DateUtil;
import org.bedrock.common.code.util.FileUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.resource.model.oss.OssProperties;
import org.bedrock.common.resource.model.oss.OssResultFile;
import org.bedrock.common.resource.service.oss.OssTemplate;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * OSS 模板抽象基类
 * <p>
 * 封装各云厂商实现中通用的文件路径生成、唯一文件名生成、访问 URL 构建等逻辑，
 * 子类只需关注各自 SDK 的差异化操作。
 * </p>
 */
public abstract class AbstractOssTemplate implements OssTemplate {

	protected final OssProperties ossProperties;

	protected AbstractOssTemplate(OssProperties ossProperties) {
		this.ossProperties = ossProperties;
	}

	/**
	 * 根据日期和前缀生成文件存储目录路径
	 * <p>格式：{prefixPath}/{yyyyMMdd}，默认 prefixPath 为 upload</p>
	 */
	protected String getFilePath() {
		String format = DateUtil.DATE_FORMATTER_NO_SEPARATION.format(LocalDateTime.now());
		if (StringUtil.isNotBlank(ossProperties.getPrefixPath())) {
			return ossProperties.getPrefixPath() + StringPool.SLASH + format;
		}
		return format;
	}

	/**
	 * 根据原始文件名生成唯一对象名（UUID + 原始扩展名）
	 */
	protected String generateObjectName(String fileName) {
		String extension = FileUtil.getExtension(fileName);
		return UUID.randomUUID().toString().replaceAll(StringPool.DASH, StringPool.EMPTY) + StringPool.DOT + extension;
	}

	/**
	 * 构建文件公开访问 URL
	 * <p>优先使用 publicUrl，未配置时回退至 endpoint</p>
	 */
	protected String getUrl(String bucketName, String filePath) {
		String prefix = StringUtil.isBlank(ossProperties.getPublicUrl()) ? ossProperties.getEndpoint() : ossProperties.getPublicUrl();
		if (prefix.endsWith(StringPool.SLASH)) {
			return prefix + bucketName + StringPool.SLASH + filePath;
		}
		return prefix + StringPool.SLASH + bucketName + StringPool.SLASH + filePath;
	}

	/**
	 * 构建上传结果对象
	 */
	protected OssResultFile buildResult(String bucketName, String dirPath, String originalName, String objectName) {
		String filePath = dirPath + StringPool.SLASH + objectName;
		return new OssResultFile(filePath, originalName, getUrl(bucketName, filePath), objectName, bucketName,
			bucketName + StringPool.SLASH + filePath);
	}

	@Override
	public OssResultFile uploadFile(String fileName, InputStream file, String contentType) {
		return uploadFile(ossProperties.getBucketName(), fileName, file, contentType);
	}

	@Override
	public void downloadFile(String filePath, OutputStream outputStream) {
		downloadFile(ossProperties.getBucketName(), filePath, outputStream);
	}

	@Override
	public InputStream downloadFile(String filePath) {
		return downloadFile(ossProperties.getBucketName(), filePath);
	}

	@Override
	public boolean deleteFile(String filePath) {
		return deleteFile(ossProperties.getBucketName(), filePath);
	}

	@Override
	public boolean copyFile(String sourceFileName, String targetFileName) {
		return copyFile(ossProperties.getBucketName(), sourceFileName, ossProperties.getBucketName(), targetFileName);
	}

}
