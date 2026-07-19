package org.bedrock.common.resource.service.oss.impl;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.DownloadUrl;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.util.FileUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.resource.model.oss.OssProperties;
import org.bedrock.common.resource.model.oss.OssResultFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * 七牛云 Kodo 对象存储实现
 * <p>
 * 依赖：com.qiniu:qiniu-java-sdk
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * accessKey: your-access-key
 * secretKey: your-secret-key
 * bucketName: your-bucket
 * publicUrl: https://your-cdn-domain.com
 * </pre>
 * </p>
 * <p>
 * 注意：七牛云建议配置 CDN 加速域名作为 publicUrl，用于生成文件访问地址。
 * </p>
 */
@Slf4j
public class QiniuOssTemplate extends AbstractOssTemplate {

	private final Auth auth;

	private final UploadManager uploadManager;

	private final BucketManager bucketManager;

	/**
	 * 通过工厂注入 Auth 和 Configuration，内部创建 UploadManager、BucketManager
	 */
	public QiniuOssTemplate(Auth auth, Configuration configuration, OssProperties ossProperties) {
		super(ossProperties);
		this.auth = auth;
		this.uploadManager = new UploadManager(configuration);
		this.bucketManager = new BucketManager(auth, configuration);
	}

	@Override
	public boolean mkdirBucket(String bucketName) {
		if (existsBucket(bucketName)) {
			return true;
		}
		try {
			bucketManager.createBucket(bucketName, ossProperties.getBucketRegion());
			return true;
		} catch (QiniuException e) {
			log.error("七牛云创建Bucket失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean existsBucket(String bucketName) {
		try {
			bucketManager.getBucketInfo(bucketName);
			return true;
		} catch (QiniuException e) {
			return false;
		}
	}

	@Override
	public boolean deleteBucket(String bucketName) {
		try {
			bucketManager.deleteBucket(bucketName);
			return true;
		} catch (QiniuException e) {
			log.error("七牛云删除Bucket失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean copyFile(String sourceBucketName, String sourceFileName, String targetBucketName, String targetFileName) {
		try {
			bucketManager.copy(sourceBucketName, sourceFileName, targetBucketName, targetFileName);
			return true;
		} catch (QiniuException e) {
			log.error("七牛云复制文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public OssResultFile uploadFile(String bucketName, String fileName, InputStream file, String contentType) {
		mkdirBucket(bucketName);
		String dirPath = getFilePath();
		String objectName = generateObjectName(fileName);
		String objectKey = dirPath + StringPool.SLASH + objectName;
		try (file) {
			String uploadToken = auth.uploadToken(bucketName);
			Response response = uploadManager.put(file, objectKey, uploadToken, null, contentType);
			if (!response.isOK()) {
				throw new RuntimeException("七牛云上传文件失败：" + response.bodyString());
			}
		} catch (Exception e) {
			log.error("七牛云上传文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return buildResult(bucketName, dirPath, fileName, objectName);
	}

	@Override
	public void downloadFile(String bucketName, String filePath, OutputStream outputStream) {
		try (InputStream is = downloadFile(bucketName, filePath)) {
			FileUtil.copy(is, outputStream);
		} catch (Exception e) {
			log.error("七牛云下载文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public InputStream downloadFile(String bucketName, String filePath) {
		try {
			String downloadUrl = buildDownloadUrl(bucketName, filePath);
			return new URL(downloadUrl).openStream();
		} catch (Exception e) {
			log.error("七牛云下载文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 构建七牛云文件下载地址（支持私有空间签名，默认有效期 1 小时）
	 */
	private String buildDownloadUrl(String bucketName, String filePath) {
		String domain = StringUtil.isBlank(ossProperties.getPublicUrl()) ? ossProperties.getEndpoint() : ossProperties.getPublicUrl();
		boolean useHttps = domain.startsWith("https");
		domain = domain.replaceFirst("^https?://", "");
		DownloadUrl downloadUrl = new DownloadUrl(domain, useHttps, filePath);
		try {
			return downloadUrl.buildURL(auth, 3600L);
		} catch (QiniuException e) {
			log.error("七牛云构建下载地址失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean deleteFile(String bucketName, String filePath) {
		try {
			bucketManager.delete(bucketName, filePath);
			return true;
		} catch (QiniuException e) {
			log.error("七牛云删除文件失败：{}", e.getMessage(), e);
			return false;
		}
	}

}
