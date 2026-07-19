package org.bedrock.common.excel.support;

import cn.idev.excel.metadata.data.RichTextStringData;
import cn.idev.excel.write.metadata.style.WriteFont;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WriteFontParser {

	private static final Pattern FONT_TAG_PATTERN = Pattern.compile("<WriteFont\\s+(.*?)>(.*?)</WriteFont>", Pattern.DOTALL);

	/**
	 * 解析带WriteFont标签的字符串
	 *
	 * @param originalStr 原始字符串（含WriteFont标签）
	 * @return 结果封装：key=纯文本（去标签），value=字体样式+位置列表
	 */
	public static RichTextStringData parse(String originalStr) {
		RichTextStringData result = new RichTextStringData();
		StringBuilder pureText = new StringBuilder(); // 去标签后的纯文本

		// 先替换所有换行符为统一格式（避免下标计算错误）
		String normalizedStr = originalStr.replaceAll("\\r\\n|\\r|\\n", "\n");
		int currentOffset = 0; // 记录纯文本的当前长度（用于计算下标）

		// 正则匹配所有WriteFont标签
		Matcher matcher = FONT_TAG_PATTERN.matcher(normalizedStr);
		int lastEnd = 0; // 上一个匹配结束的位置（用于拼接非标签文本）

		while (matcher.find()) {
			// 1. 拼接匹配标签前的普通文本
			String prefix = normalizedStr.substring(lastEnd, matcher.start());
			pureText.append(prefix);
			currentOffset += prefix.length();

			// 2. 提取标签属性和包裹的文本
			String attrStr = matcher.group(1); // 如：fontName=sds color=1
			String targetText = matcher.group(2); // 如：姓名

			// 3. 解析属性为WriteFont对象
			WriteFont writeFont = parseWriteFontAttr(attrStr);

			// 4. 记录文本在纯文本中的位置
			int start = currentOffset;
			int end = currentOffset + targetText.length();
			result.applyFont(start, end, writeFont);
			// 5. 拼接标签内的文本到纯文本，更新偏移
			pureText.append(targetText);
			currentOffset = end;
			// 6. 更新最后匹配位置
			lastEnd = matcher.end();
		}
		if (lastEnd == 0) {
			result.setTextString(originalStr);
			return result;
		}

		// 拼接最后一个标签后的剩余文本
		pureText.append(normalizedStr.substring(lastEnd));
		// 封装结果
		result.setTextString(pureText.toString());
		return result;
	}

	/**
	 * 解析WriteFont标签的属性字符串为WriteFont对象
	 *
	 * @param attrStr 属性字符串（如fontName=sds color=1 bold=true）
	 * @return WriteFont对象
	 */
	private static WriteFont parseWriteFontAttr(String attrStr) {
		WriteFont writeFont = new WriteFont();
		if (attrStr == null || attrStr.trim().isEmpty()) {
			return writeFont;
		}

		// 拆分属性（空格分隔，兼容多空格）
		String[] attrs = attrStr.trim().split("\\s+");
		for (String attr : attrs) {
			String[] keyValue = attr.split("=", 2); // 按第一个=拆分（避免值含=）
			if (keyValue.length != 2) {
				continue; // 无效属性，跳过
			}
			String key = keyValue[0].trim();
			String value = keyValue[1].trim();

			// 解析各个属性（按WriteFont字段匹配）
			switch (key) {
				case "fontName":
					writeFont.setFontName(value);
					break;
				case "fontHeightInPoints":
					try {
						writeFont.setFontHeightInPoints(Short.parseShort(value));
					} catch (NumberFormatException e) {
						// 格式错误，使用默认值
					}
					break;
				case "italic":
					writeFont.setItalic(Boolean.parseBoolean(value));
					break;
				case "strikeout":
					writeFont.setStrikeout(Boolean.parseBoolean(value));
					break;
				case "color":
					try {
						writeFont.setColor(Short.parseShort(value));
					} catch (NumberFormatException e) {
						// 格式错误，使用默认值（黑色）
						writeFont.setColor((short) 8); // IndexedColors.BLACK.getIndex()
					}
					break;
				case "typeOffset":
					try {
						writeFont.setTypeOffset(Short.parseShort(value));
					} catch (NumberFormatException e) {
						// 格式错误，使用默认值
					}
					break;
				case "underline":
					try {
						writeFont.setUnderline(Byte.parseByte(value));
					} catch (NumberFormatException e) {
						// 格式错误，使用默认值
					}
					break;
				case "charset":
					try {
						writeFont.setCharset(Integer.parseInt(value));
					} catch (NumberFormatException e) {
						// 格式错误，使用默认值
					}
					break;
				case "bold":
					writeFont.setBold(Boolean.parseBoolean(value));
					break;
				default:
					// 未知属性，跳过
					break;
			}
		}
		return writeFont;
	}
}
