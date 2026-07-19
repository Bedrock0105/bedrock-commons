package org.bedrock.common.resource.model.oss;

/**
 * 文件上传返回对象
 *
 * @param filePath     文件路径 ： upload/2023/07/05/file.png
 * @param originalName 文件原始名称 : fileName.png
 * @param url          文件访问地址 ： http://localhost:8080/bucket/upload/2023/07/05/file.png
 * @param objectName   文件对象名称 ： file.png
 * @param bucketName   文件存储桶名称:  bucket
 * @param halfPath     文件存储桶路径:  bucket/upload/2023/07/05/file.png
 */
public record OssResultFile(String filePath,
							String originalName,
							String url,
							String objectName,
							String bucketName,
							String halfPath) {

}
