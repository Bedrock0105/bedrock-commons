package org.bedrock.common.log.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bedrock.common.code.util.JsonUtil;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class LogUtil {

	/**
	 * 解析参数
	 */
	public static void logParameters(Object[] args, String[] parameterNames, Map<String, Object> params, List<FileInfo> list) {

		// 分析方法参数，分离文件和普通参数
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			String parameterName = parameterNames[i];
			if (arg instanceof MultipartFile) {
				list.add(FileInfo.from((MultipartFile) arg));
			} else if (arg instanceof MultipartFile[] files) {
				for (MultipartFile file : files) {
					if (file != null && !file.isEmpty()) {
						list.add(FileInfo.from(file));
					}
				}
			} else if (arg instanceof Collection<?> listFile) {
				// 可能是List<MultipartFile>
				List<Object> listOrg = new ArrayList<>();
				for (Object item : listFile) {
					if (item instanceof MultipartFile) {
						list.add(FileInfo.from((MultipartFile) item));
					} else {
						listOrg.add(item);
					}
				}
				if (!listOrg.isEmpty()) {
					params.put(parameterName, formatArgument(listOrg));
				}
			} else if (arg != null && !isExcludedRequestObject(arg)) {
				// 记录其他非文件参数
				params.put(parameterName, formatArgument(arg));
			}
		}
	}

	/**
	 * 判断是否可以序列化
	 */
	private static Object formatArgument(Object arg) {
		if (JsonUtil.canConvertToJson(arg)) {
			return arg;
		}
		return "此对象不可以序列化";
	}

	/**
	 * 检查是否为需要排除的请求对象
	 */
	private static boolean isExcludedRequestObject(Object arg) {
		if (arg == null) return false;

		return arg instanceof HttpServletRequest ||
			arg instanceof HttpServletResponse ||
			arg.getClass().getName().contains("jakarta.servlet");
	}

	/**
	 * 文件信息内部类
	 */
//	@Getter
//	public static class FileInfo {
//
//		private final String originalFilename;
//
//		private final long size;
//
//		private final String contentType;
//
//		private final boolean empty;
//
//		public FileInfo(String originalFilename, long size, String contentType, boolean empty) {
//			this.originalFilename = originalFilename;
//			this.size = size;
//			this.contentType = contentType;
//			this.empty = empty;
//		}
//
//		public static FileInfo extractFileInfo(MultipartFile file) {
//			return new FileInfo(file.getOriginalFilename(), file.getSize(), file.getContentType(), file.isEmpty());
//		}
//
//		public FileInfo(MultipartFile file) {
//			this(file.getOriginalFilename(), file.getSize(), file.getContentType(), file.isEmpty());
//		}
//
//	}

	/**
	 * 文件信息内部类
	 */
	public record FileInfo(String originalFilename, long size, String contentType, boolean empty) {

		public static FileInfo from(MultipartFile file) {
			return new FileInfo(file.getOriginalFilename(), file.getSize(), file.getContentType(), file.isEmpty());
		}
	}
}
