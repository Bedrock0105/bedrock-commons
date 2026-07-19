package org.bedrock.common.resource.service.oss.impl;

import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.util.FileUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.resource.model.oss.OssProperties;
import org.bedrock.common.resource.model.oss.OssResultFile;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * MinIO 对象存储实现
 * <p>
 * 依赖：io.minio:minio
 * </p>
 * <p>
 * MinIO 兼容 S3 协议，默认使用 PathStyle 访问方式。
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * endpoint: http://127.0.0.1:9000
 * accessKey: minioadmin
 * secretKey: minioadmin
 * bucketName: bedrock
 * publicUrl: http://127.0.0.1:9000
 * </pre>
 * </p>
 */
@Slf4j
public class MinioOssTemplate extends AbstractOssTemplate {

	private static final int PART_SIZE = 10 * 1024 * 1024;

	private final MinioClient minioClient;

	/**
	 * 通过工厂注入 MinIO 客户端
	 */
	public MinioOssTemplate(MinioClient minioClient, OssProperties ossProperties) {
		super(ossProperties);
		this.minioClient = minioClient;
	}

	@Override
	public boolean mkdirBucket(String bucketName) {
		if (existsBucket(bucketName)) {
			return true;
		}
		try {
			minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
			return true;
		} catch (Exception e) {
			log.error("MinIO创建Bucket失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean existsBucket(String bucketName) {
		try {
			return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
		} catch (Exception e) {
			log.error("MinIO检查Bucket失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean deleteBucket(String bucketName) {
		try {
			minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
			return true;
		} catch (Exception e) {
			log.error("MinIO删除Bucket失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean copyFile(String sourceBucketName, String sourceFileName, String targetBucketName, String targetFileName) {
		try {
			minioClient.copyObject(CopyObjectArgs.builder()
				.bucket(targetBucketName)
				.object(targetFileName)
				.source(CopySource.builder().bucket(sourceBucketName).object(sourceFileName).build())
				.build());
			return true;
		} catch (Exception e) {
			log.error("MinIO复制文件失败：{}", e.getMessage(), e);
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
			PutObjectArgs.Builder builder = PutObjectArgs.builder()
				.bucket(bucketName)
				.object(objectKey)
				.stream(file, -1, PART_SIZE);
			if (StringUtil.isNotBlank(contentType)) {
				builder.contentType(contentType);
			}
			minioClient.putObject(builder.build());
		} catch (Exception e) {
			log.error("MinIO上传文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return buildResult(bucketName, dirPath, fileName, objectName);
	}

	@Override
	public void downloadFile(String bucketName, String filePath, OutputStream outputStream) {
		try (InputStream is = downloadFile(bucketName, filePath)) {
			FileUtil.copy(is, outputStream);
		} catch (Exception e) {
			log.error("MinIO下载文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public InputStream downloadFile(String bucketName, String filePath) {
		try {
			return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(filePath).build());
		} catch (Exception e) {
			log.error("MinIO下载文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean deleteFile(String bucketName, String filePath) {
		try {
			minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(filePath).build());
			return true;
		} catch (Exception e) {
			log.error("MinIO删除文件失败：{}", e.getMessage(), e);
			return false;
		}
	}

}
