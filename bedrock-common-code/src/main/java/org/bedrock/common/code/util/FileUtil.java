package org.bedrock.common.code.util;

import org.bedrock.common.code.constant.StringPool;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件操作工具类
 * 继承Spring {@link FileCopyUtils}，提供文件名处理、目录操作、流文件转换等常用功能
 */
public abstract class FileUtil extends org.springframework.util.FileCopyUtils {

	/**
	 * 获取文件完整后缀名（支持多后缀，如 tar.gz）
	 *
	 * @param fileName 文件名（示例："image.tar.gz"、"file.txt"、"file"）
	 * @return 完整后缀名（示例："tar.gz"、"txt"），无后缀返回空字符串
	 */
	public static String getExtension(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return StringPool.EMPTY;
		}
		Path path = Paths.get(fileName);
		fileName = path.getFileName().toString();
		// 找到第一个点的位置
		int firstDotIndex = fileName.indexOf(StringPool.DOT);
		// 无点、点在最后一位（如 "file."），均视为无后缀
		if (firstDotIndex == -1 || firstDotIndex == fileName.length() - 1) {
			return StringPool.EMPTY;
		}
		// 从第一个点之后的位置截取到结尾（包含后续所有点）
		return fileName.substring(firstDotIndex + 1);
	}

	/**
	 * 提取纯文件名（去除路径前缀）
	 *
	 * @param fileName 带路径的文件名（示例："/home/image.tar.gz"、"D:/bak/file.txt"、".gitignore"）
	 * @return 纯文件名（示例："image.tar.gz"、"file.txt"、".gitignore"），空参数返回空字符串
	 */
	public static String getFileName(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return StringPool.EMPTY;
		}
		Path path = Paths.get(fileName);
		fileName = path.getFileName().toString();
		return fileName;
	}

	/**
	 * 获取去除后缀的文件名主体（按第一个点截取，忽略后续所有后缀）
	 *
	 * @param fileName 文件名（示例："/home/image.tar.gz"、"file.txt"、".gitignore"）
	 * @return 文件名主体（示例："image"、"file"、""），空参数返回空字符串
	 */
	public static String getFileNameWithoutExtension(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return StringPool.EMPTY;
		}
		Path path = Paths.get(fileName);
		fileName = path.getFileName().toString();
		// 找到第一个点的位置
		int firstDotIndex = fileName.indexOf(StringPool.DOT);

		// 无点 → 返回完整文件名
		if (firstDotIndex == -1) {
			return fileName;
		}
		// 有点 → 截取从开头到第一个点的部分（不包含点）
		return fileName.substring(0, firstDotIndex);
	}

	/**
	 * 获取系统临时目录路径
	 *
	 * @return 系统临时目录路径字符串（Windows：C:\Users\XXX\AppData\Local\Temp\；Linux：/tmp/）
	 */
	public static String getTempDirPath() {
		return System.getProperty("java.io.tmpdir");
	}

	/**
	 * 获取系统临时目录的File实例
	 *
	 * @return 系统临时目录File对象
	 */
	public static File getTempDir() {
		return new File(getTempDirPath());
	}

	/**
	 * 将输入流写入指定文件（自动关闭流，避免资源泄露）
	 *
	 * @param is   待写入的输入流
	 * @param file 目标文件
	 */
	public static void toFile(InputStream is, File file) {
		try (InputStream inputStream = is; OutputStream outputStream = new FileOutputStream(file)) {
			IoUtil.copy(inputStream, outputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将文件转换为字节数组
	 *
	 * @param file 目标文件
	 * @return 文件对应的字节数组，异常时返回空字节数组
	 */
	public static byte[] fileToByte(File file) {
		try {
			return copyToByteArray(file);
		} catch (Exception e) {
			return IoUtil.EMPTY_CONTENT;
		}
	}

	/**
	 * 创建多级目录（兼容单级目录）
	 *
	 * @param path 文件夹路径
	 * @return 目录已存在/创建成功返回true，创建失败返回false
	 */
	public static boolean mkdirs(String path) {
		File file = new File(path);
		if (file.exists()) {
			return true;
		}
		return file.mkdirs();
	}

	/**
	 * 递归删除文件夹及所有子内容（支持删除单个文件）
	 *
	 * @param path 文件/文件夹路径
	 * @return 路径不存在/删除成功返回true，删除失败返回false
	 */
	public static boolean deleteDir(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return true;
		}
		if (file.isDirectory()) {
			String[] list = file.list();
			if (ObjectUtil.isNotEmpty(list)) {
				for (String s : list) {
					deleteDir(path + File.separator + s);
				}
			}
		}
		return file.delete();
	}
}
