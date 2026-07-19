package org.bedrock.common.resource.service.oss.impl;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.util.FileUtil;
import org.bedrock.common.code.util.IoUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.resource.model.oss.OssProperties;
import org.bedrock.common.resource.model.oss.OssResultFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Amazon S3 实现（标准 S3 协议）
 * <p>
 * 依赖：software.amazon.awssdk:s3
 * </p>
 * <p>
 * 使用 AWS 官方 SDK，默认 VirtualHost 风格访问。
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * bucketRegion: us-east-1
 * accessKey: your-access-key-id
 * secretKey: your-secret-access-key
 * bucketName: your-bucket
 * publicUrl: https://your-bucket.s3.amazonaws.com
 * </pre>
 * </p>
 * <p>注意：bucketRegion 为必填项</p>
 */
@Slf4j
public class S3OssTemplate extends AbstractOssTemplate {

	private final S3Client s3Client;

	/**
	 * 通过工厂注入 S3 客户端
	 */
	public S3OssTemplate(S3Client s3Client, OssProperties ossProperties) {
		super(ossProperties);
		this.s3Client = s3Client;
	}

	@Override
	public boolean mkdirBucket(String bucketName) {
		if (existsBucket(bucketName)) {
			return true;
		}
		try {
			s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
			return true;
		} catch (Exception e) {
			log.error("AWS S3创建Bucket失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean existsBucket(String bucketName) {
		try {
			s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
			return true;
		} catch (NoSuchBucketException e) {
			return false;
		} catch (S3Exception e) {
			if (e.statusCode() == 404) {
				return false;
			}
			log.error("AWS S3检查Bucket失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean deleteBucket(String bucketName) {
		try {
			s3Client.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build());
			return true;
		} catch (Exception e) {
			log.error("AWS S3删除Bucket失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean copyFile(String sourceBucketName, String sourceFileName, String targetBucketName, String targetFileName) {
		try {
			s3Client.copyObject(CopyObjectRequest.builder()
				.sourceBucket(sourceBucketName)
				.sourceKey(sourceFileName)
				.destinationBucket(targetBucketName)
				.destinationKey(targetFileName)
				.build());
			return true;
		} catch (Exception e) {
			log.error("AWS S3复制文件失败：{}", e.getMessage(), e);
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
			byte[] bytes = IoUtil.copyToByteArray(file);
			PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(objectKey);
			if (StringUtil.isNotBlank(contentType)) {
				requestBuilder.contentType(contentType);
			}
			s3Client.putObject(requestBuilder.build(), RequestBody.fromBytes(bytes));
		} catch (Exception e) {
			log.error("AWS S3上传文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return buildResult(bucketName, dirPath, fileName, objectName);
	}

	@Override
	public void downloadFile(String bucketName, String filePath, OutputStream outputStream) {
		try (InputStream is = downloadFile(bucketName, filePath)) {
			FileUtil.copy(is, outputStream);
		} catch (Exception e) {
			log.error("AWS S3下载文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public InputStream downloadFile(String bucketName, String filePath) {
		try {
			return s3Client.getObject(GetObjectRequest.builder()
				.bucket(bucketName)
				.key(filePath)
				.build());
		} catch (Exception e) {
			log.error("AWS S3下载文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean deleteFile(String bucketName, String filePath) {
		try {
			s3Client.deleteObject(DeleteObjectRequest.builder()
				.bucket(bucketName)
				.key(filePath)
				.build());
			return true;
		} catch (Exception e) {
			log.error("AWS S3删除文件失败：{}", e.getMessage(), e);
			return false;
		}
	}

}
