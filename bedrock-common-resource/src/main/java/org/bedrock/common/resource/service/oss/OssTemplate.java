package org.bedrock.common.resource.service.oss;

import org.bedrock.common.resource.model.oss.OssResultFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 资源上传模板
 */
public interface OssTemplate {

	/**
	 * 创建bucket
	 *
	 * @param bucketName bucket名称
	 */
	boolean mkdirBucket(String bucketName);

	/**
	 * 判断bucket是否存在
	 *
	 * @param bucketName bucket名称
	 * @return true：存在 false：不存在
	 */
	boolean existsBucket(String bucketName);

	/**
	 * 删除bucket
	 *
	 * @param bucketName bucket名称
	 */
	boolean deleteBucket(String bucketName);

	/**
	 * 拷贝文件
	 *
	 * @param sourceFileName 源文件名称
	 * @param targetFileName 目标文件名称
	 * @return true：成功 false：失败
	 */
	boolean copyFile(String sourceFileName, String targetFileName);

	/**
	 * 拷贝文件
	 *
	 * @param sourceBucketName 源bucket名称
	 * @param sourceFileName   源文件名称
	 * @param targetBucketName 目标bucket名称
	 * @param targetFileName   目标文件名称
	 * @return true：成功 false：失败
	 */
	boolean copyFile(String sourceBucketName, String sourceFileName, String targetBucketName, String targetFileName);

	/**
	 * 上传文件
	 *
	 * @param fileName 文件名称
	 * @param file     文件
	 * @return 文件信息
	 */
	default OssResultFile uploadFile(String fileName, InputStream file) {
		return uploadFile(fileName, file, null);
	}

	/**
	 * 上传文件
	 *
	 * @param fileName    文件名称
	 * @param file        文件
	 * @param contentType 文件类型
	 * @return 文件信息
	 */
	OssResultFile uploadFile(String fileName, InputStream file, String contentType);

	/**
	 * 上传文件
	 *
	 * @param bucketName bucket名称
	 * @param fileName   文件名称
	 * @param file       文件
	 * @return 文件信息
	 */
	default OssResultFile uploadFile(String bucketName, String fileName, InputStream file) {
		return uploadFile(bucketName, fileName, file, null);
	}

	/**
	 * 上传文件
	 *
	 * @param bucketName  bucket名称
	 * @param fileName    文件名称
	 * @param file        文件
	 * @param contentType 文件类型
	 * @return 文件信息
	 */
	OssResultFile uploadFile(String bucketName, String fileName, InputStream file, String contentType);

	/**
	 * 上传文件
	 *
	 * @param fileName 文件名称
	 * @param bytes    文件字节数组
	 * @return 文件信息
	 */
	default OssResultFile uploadFile(String fileName, byte[] bytes) {
		return uploadFile(fileName, bytes, null);
	}

	/**
	 * 上传文件
	 *
	 * @param fileName    文件名称
	 * @param bytes       文件字节数组
	 * @param contentType 文件类型
	 * @return 文件信息
	 */
	default OssResultFile uploadFile(String fileName, byte[] bytes, String contentType) {
		return uploadFile(fileName, new ByteArrayInputStream(bytes), contentType);
	}

	/**
	 * 上传文件
	 *
	 * @param bucketName bucket名称
	 * @param fileName   文件名称
	 * @param bytes      文件字节数组
	 * @return 文件信息
	 */
	default OssResultFile uploadFile(String bucketName, String fileName, byte[] bytes) {
		return uploadFile(bucketName, fileName, bytes, null);
	}

	/**
	 * 上传文件
	 *
	 * @param bucketName  bucket名称
	 * @param fileName    文件名称
	 * @param bytes       文件字节数组
	 * @param contentType 文件类型
	 * @return 文件信息
	 */
	default OssResultFile uploadFile(String bucketName, String fileName, byte[] bytes, String contentType) {
		return uploadFile(bucketName, fileName, new ByteArrayInputStream(bytes), contentType);
	}

	/**
	 * 下载文件
	 *
	 * @param filePath     文件名称(路径加名称)
	 * @param outputStream 输出流
	 */
	void downloadFile(String filePath, OutputStream outputStream);

	/**
	 * 下载文件
	 *
	 * @param bucketName   bucket名称
	 * @param filePath     文件名称(路径加名称)
	 * @param outputStream 输出流
	 */
	void downloadFile(String bucketName, String filePath, OutputStream outputStream);

	/**
	 * 下载文件
	 *
	 * @param bucketName bucket名称
	 * @param filePath   文件名称(路径加名称)
	 * @return 文件流
	 */
	InputStream downloadFile(String bucketName, String filePath);

	/**
	 * 下载文件
	 *
	 * @param filePath 文件名称(路径加名称)
	 * @return 文件流
	 */
	InputStream downloadFile(String filePath);

	/**
	 * 删除文件
	 *
	 * @param filePath 文件名称(路径加名称)
	 */
	boolean deleteFile(String filePath);

	/**
	 * 删除文件
	 *
	 * @param bucketName bucket名称
	 * @param filePath   文件名称(路径加名称)
	 */
	boolean deleteFile(String bucketName, String filePath);
}
