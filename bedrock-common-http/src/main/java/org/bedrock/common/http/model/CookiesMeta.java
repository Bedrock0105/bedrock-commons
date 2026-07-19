package org.bedrock.common.http.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP Cookie 集合管理类
 * 封装多个Cookie元数据对象的集合，提供Cookie的新增、查询、属性合并、格式解析、标准序列化能力
 * 核心特性：同名Cookie自动合并属性、自动兼容Cookie标准协议格式、完善的空值与异常容错处理
 */
public class CookiesMeta {

	/**
	 * Cookie元数据对象集合，统一存储和管理所有Cookie信息
	 */
	@Getter
	private final List<CookieMeta> cookieMetas = new ArrayList<>();

	// ======================== 对外核心查询方法 ========================

	/**
	 * 根据Cookie名称精准匹配获取对应的Cookie对象
	 *
	 * @param name Cookie名称
	 * @return 匹配到的CookieMeta对象，无匹配项时返回null
	 */
	public CookieMeta getCookie(String name) {
		for (CookieMeta cookie : cookieMetas) {
			if (cookie.getName().equals(name)) {
				return cookie;
			}
		}
		return null;
	}

	// ======================== 对外核心添加方法 ========================

	/**
	 * 添加Cookie元数据对象至集合
	 * 存在同名Cookie时，自动将新属性合并至原有Cookie；不存在则直接新增
	 *
	 * @param cookieMeta Cookie元数据对象
	 * @return 当前CookiesMeta对象本身，支持链式调用
	 */
	public CookiesMeta addCookie(CookieMeta cookieMeta) {
		if (cookieMeta == null || cookieMeta.getName() == null || cookieMeta.getName().isBlank()) {
			return this;
		}
		for (CookieMeta meta : cookieMetas) {
			if (meta.getName().equals(cookieMeta.getName())) {
				meta.mergeFrom(cookieMeta);
				return this;
			}
		}
		cookieMetas.add(cookieMeta);
		return this;
	}

	/**
	 * 快捷添加Cookie，通过名称和值构建Cookie对象并加入集合
	 * 存在同名Cookie时自动合并属性，不存在则新增
	 *
	 * @param name  Cookie名称
	 * @param value Cookie值
	 * @return 当前CookiesMeta对象本身，支持链式调用
	 */
	public CookiesMeta addCookie(String name, String value) {
		this.addCookie(new CookieMeta(name, value));
		return this;
	}

	/**
	 * 解析单个Cookie格式字符串，转换为Cookie对象后加入集合
	 * 存在同名Cookie时自动合并属性，解析失败则忽略本次添加
	 *
	 * @param cookieStr 单个Cookie的标准格式字符串，如：token=123; Path=/; HttpOnly
	 * @return 当前CookiesMeta对象本身，支持链式调用
	 */
	public CookiesMeta addCookie(String cookieStr) {
		this.addCookie(CookieMeta.parse(cookieStr));
		return this;
	}

	/**
	 * 批量解析HTTP请求头中的完整Cookie字符串，转换后批量加入集合
	 * 自动按标准分割多个Cookie，支持带特殊字符的Cookie值，解析失败的项自动忽略
	 *
	 * @param cookieHeaderStr 请求头Cookie字段的完整内容，如：token=123; userId=456; userName=张三
	 * @return 当前CookiesMeta对象本身，支持链式调用
	 */
	public CookiesMeta addCookies(String cookieHeaderStr) {
		if (cookieHeaderStr == null || cookieHeaderStr.isBlank()) {
			return this;
		}
		String[] singleCookieArr = cookieHeaderStr.split(";(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
		for (String singleCookie : singleCookieArr) {
			CookieMeta cookieMeta = CookieMeta.parse(singleCookie);
			if (cookieMeta != null) {
				addCookie(cookieMeta);
			}
		}
		return this;
	}

	/**
	 * 批量添加Cookie元数据对象集合至当前容器
	 * 存在同名Cookie时自动合并属性，不存在则直接新增
	 *
	 * @param cookieMetas 待添加的Cookie元数据对象集合
	 * @return 当前CookiesMeta对象本身，支持链式调用
	 */
	public CookiesMeta addCookies(List<CookieMeta> cookieMetas) {
		for (CookieMeta cookieMeta : cookieMetas) {
			addCookie(cookieMeta);
		}
		return this;
	}

	// ======================== 序列化方法 ========================

	/**
	 * 转换为符合HTTP协议标准的Cookie请求头字符串
	 * 多个Cookie之间使用「分号 + 空格」分隔，过滤无效空Cookie，无冗余字符
	 * 生成的字符串可直接放入请求头的 Cookie 字段中使用
	 *
	 * @return 标准格式的Cookie拼接字符串，无数据时返回空串
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (CookieMeta cookie : cookieMetas) {
			String cookieStr = cookie.toString();
			if (!cookieStr.isBlank()) {
				if (!sb.isEmpty()) {
					sb.append("; ");
				}
				sb.append(cookieStr);
			}
		}
		return sb.toString();
	}
}
