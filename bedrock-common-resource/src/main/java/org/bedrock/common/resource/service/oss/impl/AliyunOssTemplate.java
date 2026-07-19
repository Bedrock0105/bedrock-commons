package org.bedrock.common.resource.service.oss.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.CopyObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.util.FileUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.resource.model.oss.OssProperties;
import org.bedrock.common.resource.model.oss.OssResultFile;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 阿里云 OSS 实现
 * <p>
 * 依赖：com.aliyun.oss:aliyun-sdk-oss
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * endpoint: https://oss-cn-beijing.aliyuncs.com
 * accessKey: your-access-key-id
 * secretKey: your-access-key-secret
 * bucketName: your-bucket
 * publicUrl: https://your-bucket.oss-cn-beijing.aliyuncs.com
 * </pre>
 * </p>
 */
@Slf4j
public class AliyunOssTemplate extends AbstractOssTemplate {

	private final OSS ossClient;

	/**
	 * 通过工厂注入 OSS 客户端
	 */
	public AliyunOssTemplate(OSS ossClient, OssProperties ossProperties) {
		super(ossProperties);
		this.ossClient = ossClient;
	}

	@Override
	public boolean mkdirBucket(String bucketName) {
		if (existsBucket(bucketName)) {
			return true;
		}
		try {
			ossClient.createBucket(bucketName);
			return true;
		} catch (Exception e) {
			log.error("阿里云OSS创建Bucket失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean existsBucket(String bucketName) {
		return ossClient.doesBucketExist(bucketName);
	}

	@Override
	public boolean deleteBucket(String bucketName) {
		try {
			ossClient.deleteBucket(bucketName);
			return true;
		} catch (Exception e) {
			log.error("阿里云OSS删除Bucket失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean copyFile(String sourceBucketName, String sourceFileName, String targetBucketName, String targetFileName) {
		try {
			ossClient.copyObject(new CopyObjectRequest(sourceBucketName, sourceFileName, targetBucketName, targetFileName));
			return true;
		} catch (Exception e) {
			log.error("阿里云OSS复制文件失败：{}", e.getMessage(), e);
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
			ossClient.putObject(bucketName, objectKey, file, metadata);
		} catch (Exception e) {
			log.error("阿里云OSS上传文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return buildResult(bucketName, dirPath, fileName, objectName);
	}

	@Override
	public void downloadFile(String bucketName, String filePath, OutputStream outputStream) {
		try (InputStream is = downloadFile(bucketName, filePath)) {
			FileUtil.copy(is, outputStream);
		} catch (Exception e) {
			log.error("阿里云OSS下载文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public InputStream downloadFile(String bucketName, String filePath) {
		try {
			OSSObject ossObject = ossClient.getObject(bucketName, filePath);
			return ossObject.getObjectContent();
		} catch (Exception e) {
			log.error("阿里云OSS下载文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean deleteFile(String bucketName, String filePath) {
		try {
			ossClient.deleteObject(bucketName, filePath);
			return true;
		} catch (Exception e) {
			log.error("阿里云OSS删除文件失败：{}", e.getMessage(), e);
			return false;
		}
	}

}
