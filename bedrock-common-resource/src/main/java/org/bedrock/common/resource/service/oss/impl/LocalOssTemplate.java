package org.bedrock.common.resource.service.oss.impl;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.util.DateUtil;
import org.bedrock.common.code.util.FileUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.resource.model.oss.OssProperties;
import org.bedrock.common.resource.model.oss.OssResultFile;
import org.bedrock.common.resource.service.oss.OssTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
public class LocalOssTemplate extends AbstractOssTemplate {

	private final String path;

	public LocalOssTemplate(OssProperties ossProperties) {
		super(ossProperties);
		this.path = ossProperties.getEndpoint();
	}

	@Override
	public boolean mkdirBucket(String bucketName) {
		if (existsBucket(bucketName)) {
			return true;
		}
		try {
			Files.createDirectories(Paths.get(path, bucketName));
		} catch (Exception e) {
			log.error("创建目录失败：{}", e.getMessage());
			throw new RuntimeException(e);
		}
		return false;
	}

	@Override
	public boolean existsBucket(String bucketName) {
		return Files.exists(Paths.get(path, bucketName));
	}

	@Override
	public boolean deleteBucket(String bucketName) {
		try {
			return Files.deleteIfExists(Paths.get(path, bucketName));
		} catch (Exception e) {
			log.error("删除目录失败：{}", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean copyFile(String sourceBucketName, String sourceFileName, String targetBucketName, String targetFileName) {
		try {
			Files.copy(Paths.get(path, sourceBucketName, sourceFileName),
				Paths.get(path, targetBucketName, targetFileName));
			return true;
		} catch (Exception e) {
			log.error("复制文件失败：{}", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	@Override
	public OssResultFile uploadFile(String bucketName, String fileName, InputStream file, String contentType) {
		mkdirBucket(bucketName);
		String filePath = getFilePath();
		String extension = FileUtil.getExtension(fileName);
		String newFileName = UUID.randomUUID().toString().replaceAll(StringPool.DASH, StringPool.EMPTY) + StringPool.DOT + extension;
		OutputStream outputStream = null;
		try (file) {
			Files.createDirectories(Paths.get(path, bucketName, filePath));
			outputStream = Files.newOutputStream(Files.createFile(Paths.get(path, bucketName, filePath, newFileName)));
			FileUtil.copy(file, outputStream);
		} catch (Exception e) {
			log.error("上传文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					log.error("关闭输出流失败：{}", e.getMessage(), e);
				}
			}
		}
		filePath = filePath + StringPool.SLASH + newFileName;
		return new OssResultFile(filePath, fileName, getUrl(bucketName, filePath), newFileName, bucketName, bucketName + StringPool.SLASH + filePath);
	}

	@Override
	public void downloadFile(String bucketName, String filePath, OutputStream outputStream) {
		try (InputStream is = downloadFile(bucketName, filePath)) {
			FileUtil.copy(is, outputStream);
		} catch (Exception e) {
			log.error("下载文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public InputStream downloadFile(String bucketName, String filePath) {
		try {
			return Files.newInputStream(Paths.get(path, bucketName, filePath));
		} catch (Exception e) {
			log.error("下载文件失败：{}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean deleteFile(String bucketName, String filePath) {
		try {
			return Files.deleteIfExists(Paths.get(path, bucketName, filePath));
		} catch (IOException e) {
			log.error("删除文件失败：{}", e.getMessage(), e);
			return false;
		}
	}
}
