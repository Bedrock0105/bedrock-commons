package org.bedrock.common.resource.service.oss.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.CopyObjectRequest;
import com.qcloud.cos.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.util.FileUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.resource.model.oss.OssProperties;
import org.bedrock.common.resource.model.oss.OssResultFile;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 腾讯云 COS 实现
 * <p>
 * 依赖：com.qcloud:cos_api
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * bucketRegion: ap-guangzhou
 * accessKey: your-secret-id
 * secretKey: your-secret-key
 * bucketName: your-bucket-1250000000
 * publicUrl: https://your-bucket-1250000000.cos.ap-guangzhou.myqcloud.com
 * </pre>
 * </p>
 * <p>注意：bucketRegion 为必填项</p>
 */
@Slf4j
public class TencentCosOssTemplate extends AbstractOssTemplate {

	private final COSClient cosClient;

	/**
	 * 通过工厂注入 COS 客户端
	 */
	public TencentCosOssTemplate(COSClient cosClient, OssProperties ossProperties) {
		super(ossProperties);
		this.cosClient = cosClient;
	}

	@Override
	public boolean mkdirBucket(String bucketName) {
		if (existsBucket(bucketName)) {
			return true;
		}
		try {
			cosClient.createBucket(bucketName);
			return true;
		} catch (Exception e) {
			log.error("腾讯云COS创建Bucket失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean existsBucket(String bucketName) {
		return cosClient.doesBucketExist(bucketName);
	}

	@Override
	public boolean deleteBucket(String bucketName) {
		try {
			cosClient.deleteBucket(bucketName);
			return true;
		} catch (Exception e) {
			log.error("腾讯云COS删除Bucket失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean copyFile(String sourceBucketName, String sourceFileName, String targetBucketName, String targetFileName) {
		try {
			cosClient.copyObject(new CopyObjectRequest(sourceBucketName, sourceFileName, targetBucketName, targetFileName));
			return true;
		} catch (Exception e) {
			log.error("腾讯云COS复制文件失败：{}", e.getMessage(), e);
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
			cosClient.putObject(bucketName, objectKey, file, metadata);
		} catch (Exception e) {
			log.error("腾讯云COS上传文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return buildResult(bucketName, dirPath, fileName, objectName);
	}

	@Override
	public void downloadFile(String bucketName, String filePath, OutputStream outputStream) {
		try (InputStream is = downloadFile(bucketName, filePath)) {
			FileUtil.copy(is, outputStream);
		} catch (Exception e) {
			log.error("腾讯云COS下载文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public InputStream downloadFile(String bucketName, String filePath) {
		try {
			COSObject cosObject = cosClient.getObject(bucketName, filePath);
			return cosObject.getObjectContent();
		} catch (Exception e) {
			log.error("腾讯云COS下载文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean deleteFile(String bucketName, String filePath) {
		try {
			cosClient.deleteObject(bucketName, filePath);
			return true;
		} catch (Exception e) {
			log.error("腾讯云COS删除文件失败：{}", e.getMessage(), e);
			return false;
		}
	}

}
