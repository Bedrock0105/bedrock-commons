package org.bedrock.common.resource.service.oss.impl;

import com.obs.services.ObsClient;
import com.obs.services.model.CopyObjectRequest;
import com.obs.services.model.ObsObject;
import com.obs.services.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.util.FileUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.resource.model.oss.OssProperties;
import org.bedrock.common.resource.model.oss.OssResultFile;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 华为云 OBS 实现
 * <p>
 * 依赖：com.huaweicloud:esdk-obs-java
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * endpoint: https://obs.cn-north-4.myhuaweicloud.com
 * accessKey: your-access-key
 * secretKey: your-secret-key
 * bucketName: your-bucket
 * publicUrl: https://your-bucket.obs.cn-north-4.myhuaweicloud.com
 * </pre>
 * </p>
 */
@Slf4j
public class HuaweiObsOssTemplate extends AbstractOssTemplate {

	private final ObsClient obsClient;

	/**
	 * 通过工厂注入 OBS 客户端
	 */
	public HuaweiObsOssTemplate(ObsClient obsClient, OssProperties ossProperties) {
		super(ossProperties);
		this.obsClient = obsClient;
	}

	@Override
	public boolean mkdirBucket(String bucketName) {
		if (existsBucket(bucketName)) {
			return true;
		}
		try {
			obsClient.createBucket(bucketName);
			return true;
		} catch (Exception e) {
			log.error("华为云OBS创建Bucket失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean existsBucket(String bucketName) {
		return obsClient.headBucket(bucketName);
	}

	@Override
	public boolean deleteBucket(String bucketName) {
		try {
			obsClient.deleteBucket(bucketName);
			return true;
		} catch (Exception e) {
			log.error("华为云OBS删除Bucket失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean copyFile(String sourceBucketName, String sourceFileName, String targetBucketName, String targetFileName) {
		try {
			CopyObjectRequest request = new CopyObjectRequest();
			request.setSourceBucketName(sourceBucketName);
			request.setSourceObjectKey(sourceFileName);
			request.setDestinationBucketName(targetBucketName);
			request.setDestinationObjectKey(targetFileName);
			obsClient.copyObject(request);
			return true;
		} catch (Exception e) {
			log.error("华为云OBS复制文件失败：{}", e.getMessage(), e);
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
			ObjectMetadata metadata = new ObjectMetadata();
			if (StringUtil.isNotBlank(contentType)) {
				metadata.setContentType(contentType);
			}
			obsClient.putObject(bucketName, objectKey, file, metadata);
		} catch (Exception e) {
			log.error("华为云OBS上传文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return buildResult(bucketName, dirPath, fileName, objectName);
	}

	@Override
	public void downloadFile(String bucketName, String filePath, OutputStream outputStream) {
		try (InputStream is = downloadFile(bucketName, filePath)) {
			FileUtil.copy(is, outputStream);
		} catch (Exception e) {
			log.error("华为云OBS下载文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public InputStream downloadFile(String bucketName, String filePath) {
		try {
			ObsObject obsObject = obsClient.getObject(bucketName, filePath);
			return obsObject.getObjectContent();
		} catch (Exception e) {
			log.error("华为云OBS下载文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean deleteFile(String bucketName, String filePath) {
		try {
			obsClient.deleteObject(bucketName, filePath);
			return true;
		} catch (Exception e) {
			log.error("华为云OBS删除文件失败：{}", e.getMessage(), e);
			return false;
		}
	}

}
