package org.bedrock.common.http.model;

import lombok.Getter;
import lombok.Setter;
import org.bedrock.common.code.constant.StringPool;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Cookie 数据模型
 * 封装HTTP标准Cookie的所有属性，提供属性合并、对象克隆、标准格式序列化能力
 */
@Getter
@Setter
public class CookieMeta {

	private String name;    // Cookie名称，对应Cookie键值对的键，为必填项

	private String value;   // Cookie值，对应Cookie键值对的值

	private String comment; // Cookie注释信息，描述Cookie的业务用途

	private String domain;  // Cookie生效域名，仅该域名下的请求可携带此Cookie

	private int maxAge = -1;// Cookie最大存活时长，单位：秒；-1表示会话级Cookie，浏览器关闭则失效

	private String path;    // Cookie生效路径，仅该路径下的请求可携带此Cookie

	private boolean secure; // 是否仅在HTTPS安全协议下传输该Cookie

	private int version = 0;// Cookie版本号，遵循RFC 2109协议规范，默认值为0即可满足通用场景

	private boolean isHttpOnly = false; // 是否禁止前端JS脚本读取Cookie，有效防范XSS攻击

	/**
	 * 构造方法：初始化Cookie核心的名称与值
	 *
	 * @param name  Cookie名称
	 * @param value Cookie值
	 */
	public CookieMeta(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * 属性合并方法：将源Cookie对象的所有属性值覆盖至当前对象
	 *
	 * @param original 源Cookie元数据对象
	 * @return 当前CookieMeta对象，支持链式调用
	 */
	public CookieMeta mergeFrom(CookieMeta original) {
		this.value = original.value;
		this.comment = original.comment;
		this.domain = original.domain;
		this.maxAge = original.maxAge;
		this.path = original.path;
		this.secure = original.secure;
		this.version = original.version;
		this.isHttpOnly = original.isHttpOnly;
		return this;
	}

	/**
	 * 拷贝构造方法：通过源Cookie对象，创建一个属性完全一致的新Cookie对象
	 *
	 * @param original 源Cookie元数据对象
	 */
	public CookieMeta(CookieMeta original) {
		this.name = original.name;
		this.value = original.value;
		this.comment = original.comment;
		this.domain = original.domain;
		this.maxAge = original.maxAge;
		this.path = original.path;
		this.secure = original.secure;
		this.version = original.version;
		this.isHttpOnly = original.isHttpOnly;
	}

	/**
	 * 克隆当前Cookie对象，返回属性完全一致的新对象
	 *
	 * @return 新的CookieMeta对象，与当前对象属性完全相同
	 */
	@Override
	public CookieMeta clone() {
		return new CookieMeta(this);
	}

	// ======================== 对外静态解析方法 ========================

	/**
	 * 解析单个Cookie字符串 转为 CookieMeta对象
	 * 支持两种格式：极简格式(name=value)、完整带属性格式(name=value; Path=/; Domain=.xxx.com; Max-Age=3600; Secure; HttpOnly)
	 *
	 * @param cookieStr 单个Cookie的完整字符串
	 * @return CookieMeta对象，解析失败/入参无效时返回null
	 */
	public static CookieMeta parse(String cookieStr) {
		// 空值容错：入参为空直接返回null
		if (cookieStr == null || cookieStr.isBlank()) {
			return null;
		}
		CookieMeta cookieMeta = new CookieMeta("", "");
		// 按分号分割所有片段，兼容 分号后有空格/无空格的格式
		String[] parts = cookieStr.split(StringPool.SEMICOLON);
		// 遍历解析每一个Cookie片段
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i].trim();
			if (part.isBlank()) {
				continue;
			}
			if (i == 0) {
				// 第一个片段固定为Cookie的name=value核心键值对
				parseNameAndValue(part, cookieMeta);
			} else {
				// 后续片段均为Cookie的附加属性
				parseCookieAttribute(part, cookieMeta);
			}
		}
		// 最终容错：解析后的Cookie名称为空则返回null
		return cookieMeta.getName() == null || cookieMeta.getName().isBlank() ? null : cookieMeta;
	}

	// ======================== 私有解析工具方法 (归类合并，放一起，供内部调用) ========================

	/**
	 * 私有工具方法：解析Cookie的核心键值对 name=value 并赋值到对象
	 * 自动对内容做URL解码，兼容中文、空格、=、;等特殊字符
	 */
	private static void parseNameAndValue(String nameValueStr, CookieMeta cookieMeta) {
		int eqIndex = nameValueStr.indexOf("=");
		if (eqIndex > 0) {
			// 正常场景：包含等号，分割名称和值并解码
			String name = URLDecoder.decode(nameValueStr.substring(0, eqIndex).trim(), StandardCharsets.UTF_8);
			String value = URLDecoder.decode(nameValueStr.substring(eqIndex + 1).trim(), StandardCharsets.UTF_8);
			cookieMeta.setName(name);
			cookieMeta.setValue(value);
		} else if (eqIndex == 0) {
			// 特殊场景：等号在首位，名称为空，仅解析值
			cookieMeta.setValue(URLDecoder.decode(nameValueStr.substring(1).trim(), StandardCharsets.UTF_8));
		} else {
			// 特殊场景：无等号，只有名称，值为空字符串
			cookieMeta.setName(URLDecoder.decode(nameValueStr.trim(), StandardCharsets.UTF_8));
			cookieMeta.setValue("");
		}

	}

	/**
	 * 私有工具方法：解析Cookie的单个附加属性，并赋值到对象
	 * 忽略属性大小写，兼容 Path/path/PATH 等多种写法，自动处理数字类型转换异常
	 */
	private static void parseCookieAttribute(String attrStr, CookieMeta cookieMeta) {
		int eqIndex = attrStr.indexOf("=");
		String attrKey;
		String attrValue = null;

		// 分割属性的键和值
		if (eqIndex > 0) {
			attrKey = attrStr.substring(0, eqIndex).trim();
			attrValue = attrStr.substring(eqIndex + 1).trim();
		} else {
			attrKey = attrStr.trim();
		}

		// 忽略大小写解析所有Cookie标准属性
		switch (attrKey.toLowerCase()) {
			case "path":
				cookieMeta.setPath(attrValue);
				break;
			case "domain":
				cookieMeta.setDomain(attrValue);
				break;
			case "max-age":
				try {
					cookieMeta.setMaxAge(Integer.parseInt(attrValue));
				} catch (NumberFormatException e) {
					cookieMeta.setMaxAge(-1);
				}
				break;
			case "secure":
				cookieMeta.setSecure(true);
				break;
			case "httponly":
				cookieMeta.setHttpOnly(true);
				break;
			case "comment":
				cookieMeta.setComment(attrValue);
				break;
			case "version":
				try {
					cookieMeta.setVersion(Integer.parseInt(attrValue));
				} catch (NumberFormatException e) {
					cookieMeta.setVersion(0);
				}
				break;
			default:
				// 兼容未知属性，不做处理，避免解析失败
				break;
		}
	}

	/**
	 * 转换为符合HTTP标准的Cookie请求头字符串
	 * 自动对name/value做UTF-8 URL编码，兼容中文、空格、等号、分号等特殊字符
	 * 自动过滤空值和默认值属性，仅拼接有实际配置的内容，无冗余字段
	 *
	 * @return 标准Cookie格式字符串，名称为空时返回空字符串
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		// 空值容错：Cookie名称为空则返回空串，避免生成无效Cookie
		if (name == null || name.isBlank()) {
			return "";
		}
		// 对name/value做URL编码，兼容特殊字符，防止Cookie格式异常失效
		String encodeName = java.net.URLEncoder.encode(name, StandardCharsets.UTF_8);
		String encodeValue = value == null ? "" : java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
		sb.append(encodeName).append("=").append(encodeValue);

		// 拼接Cookie附加属性，仅拼接非默认值/非空的属性
		if (path != null && !path.isBlank()) {
			sb.append("; Path=").append(path);
		}
		if (domain != null && !domain.isBlank()) {
			sb.append("; Domain=").append(domain);
		}
		if (maxAge > -1) {
			sb.append("; Max-Age=").append(maxAge);
		}
		if (secure) {
			sb.append("; Secure");
		}
		if (isHttpOnly) {
			sb.append("; HttpOnly");
		}
		if (comment != null && !comment.isBlank()) {
			sb.append("; Comment=").append(comment);
		}
		if (version > 0) {
			sb.append("; Version=").append(version);
		}
		return sb.toString();
	}
}
