package org.bedrock.common.http.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

import org.bedrock.common.http.enums.HeaderEnum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * HTTP 请求头元数据容器
 * <p>管理一组 {@link HeaderMeta} 类型的请求头信息，提供请求头的增、删、改、查操作，核心特性：</p>
 * <ul>
 *     <li>头名称匹配采用 <b>大小写不敏感</b> 规则（如 "Content-Type" 和 "content-type" 视为同一个头）；</li>
 *     <li>支持单值头覆盖、多值头查询，兼容 HTTP 头的多值特性；</li>
 *     <li>所有操作均基于内部维护的 {@link #headerMetas} 列表，保证操作的原子性（单线程场景）。</li>
 * </ul>
 */
@Getter
@Setter
@ToString
public class HeadersMeta {

	/**
	 * 请求头元数据列表，初始化为空列表，避免空指针
	 */
	private List<HeaderMeta> headerMetas = new ArrayList<>();

	/**
	 * Cookie 列表
	 */
	private CookiesMeta cookiesMeta;

	/**
	 * 添加单个请求头（快速构建）
	 * <p>简化单个请求头的配置流程，无需手动创建 {@link HeaderMeta} 对象，直接通过名称和值构建并添加</p>
	 *
	 * @param name  请求头名称（非空，若传入 null 会导致 {@link HeaderMeta} 构造异常，需提前校验）
	 * @param value 请求头值（可为 null 或空字符串，适配 HTTP 空值头场景）
	 * @see HeaderMeta#HeaderMeta(String, String)
	 */
	public HeadersMeta addHeader(String name, String value) {
		return addHeader(new HeaderMeta(name, value));
	}

	/**
	 * 添加单个请求头（直接传入元数据对象）
	 * <p>适用于已构建好 {@link HeaderMeta} 对象的场景，保留头元数据的完整属性</p>
	 *
	 * @param headerMeta 请求头元数据对象（若传入 null，会向列表中添加 null 元素，建议提前校验）
	 */
	public HeadersMeta addHeader(HeaderMeta headerMeta) {
		/**
		 * 兼容 HTTP 头的 Content-Type 特殊处理
		 */
		if (HeaderEnum.CONTENT_TYPE.getValue().equalsIgnoreCase(headerMeta.getName())
			&& getHeader(HeaderEnum.CONTENT_TYPE.getValue()) != null) {
			setHeader(headerMeta);
			return this;
		} else if (HeaderEnum.COOKIE.getValue().equalsIgnoreCase(headerMeta.getName())) {
			if (cookiesMeta == null) {
				cookiesMeta = new CookiesMeta();
				cookiesMeta.addCookies(headerMeta.getValue());
				headerMeta.setValue(cookiesMeta);
				headerMetas.add(headerMeta);
			} else {
				cookiesMeta.addCookies(headerMeta.getValue());
			}
			return this;
		} else if (HeaderEnum.SET_COOKIE.getValue().equalsIgnoreCase(headerMeta.getName())) {
			CookieMeta parse = CookieMeta.parse(headerMeta.getValue());
			headerMeta.setValue(parse);
		}
		headerMetas.add(headerMeta);
		return this;
	}

	/**
	 * 批量添加请求头
	 * <p>一次性导入多个请求头元数据，适用于批量配置（如从配置文件/已有头列表导入）</p>
	 *
	 * @param headerMetas 请求头元数据集合（可为 null 或空集合：null 会抛出 {@link NullPointerException}，空集合无任何操作）
	 */
	public HeadersMeta addHeaders(Map<String, String> headerMetas) {
		headerMetas.forEach(this::addHeader);
		return this;
	}

	/**
	 * 批量添加请求头
	 * <p>一次性导入多个请求头元数据，适用于批量配置（如从配置文件/已有头列表导入）</p>
	 *
	 * @param headerMetas 请求头元数据集合（可为 null 或空集合：null 会抛出 {@link NullPointerException}，空集合无任何操作）
	 */
	public HeadersMeta addHeaders(Collection<HeaderMeta> headerMetas) {
		for (HeaderMeta headerMeta : headerMetas) {
			addHeader(headerMeta);
		}
		return this;
	}

	/**
	 * 批量添加请求头
	 * <p>一次性导入多个请求头元数据，适用于批量配置（如从配置文件/已有头列表导入）</p>
	 *
	 * @param headerMetas 请求头元数据集合
	 */
	public HeadersMeta addHeaders(HeadersMeta headerMetas) {
		for (HeaderMeta headerMeta : headerMetas.getHeaderMetas()) {
			addHeader(headerMeta.getName(), headerMeta.getValue());
		}
		return this;
	}

	/**
	 * 设置/覆盖请求头（单值）
	 * <p>核心逻辑：大小写不敏感匹配头名称，<b>找到则覆盖值，未找到则新增</b>，保证同一个头名称仅保留最后一次设置的值</p>
	 *
	 * @param name  请求头名称（非空，若传入 null 会导致 {@link HeaderMeta} 构造异常，需提前校验）
	 * @param value 请求头新值（可为 null 或空字符串，覆盖原有值）
	 */
	public HeadersMeta setHeader(String name, String value) {
		for (HeaderMeta headerMeta : this.headerMetas) {
			if (headerMeta.getName().equalsIgnoreCase(name)) {
				headerMeta.setValue(value);
				return this;
			}
		}
		return addHeader(name, value);

	}

	/**
	 * 设置/覆盖请求头（通过元数据对象）
	 * <p>核心逻辑：大小写不敏感匹配头名称，<b>找到则覆盖值，未找到则新增</b>，仅覆盖值属性，保留原有对象的其他属性（若有）</p>
	 *
	 * @param headerMeta 包含新值的请求头元数据对象（name 字段非空，否则匹配失败并新增 null 名称的头）
	 */
	public HeadersMeta setHeader(HeaderMeta headerMeta) {
		for (HeaderMeta olg : this.headerMetas) {
			if (olg.getName().equalsIgnoreCase(headerMeta.getName())) {
				olg.setValue(headerMeta.getValue());
				return this;
			}
		}
		return addHeader(headerMeta);
	}

	/**
	 * 添加Cookie元数据对象至集合
	 * 存在同名Cookie时，自动将新属性合并至原有Cookie；不存在则直接新增
	 *
	 * @param cookieMeta Cookie元数据对象
	 * @return 当前CookiesMeta对象本身，支持链式调用
	 */
	public HeadersMeta addCookie(CookieMeta cookieMeta) {
		if (cookiesMeta == null) {
			cookiesMeta = new CookiesMeta();
			this.headerMetas.add(new HeaderMeta(HeaderEnum.COOKIE.getValue(), cookiesMeta));
		}
		cookiesMeta.addCookie(cookieMeta);
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
	public HeadersMeta addCookie(String name, String value) {
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
	public HeadersMeta addCookie(String cookieStr) {
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
	public HeadersMeta addCookies(String cookieHeaderStr) {
		return addHeader(HeaderEnum.COOKIE.getValue(), cookieHeaderStr);
	}

	/**
	 * 批量添加Cookie元数据对象集合至当前容器
	 * 存在同名Cookie时自动合并属性，不存在则直接新增
	 *
	 * @param cookieMetas 待添加的Cookie元数据对象集合
	 * @return 当前CookiesMeta对象本身，支持链式调用
	 */
	public HeadersMeta addCookies(List<CookieMeta> cookieMetas) {
		for (CookieMeta cookieMeta : cookieMetas) {
			addCookie(cookieMeta);
		}
		return this;
	}

	/**
	 * 获取请求头的第一个匹配值
	 * <p>大小写不敏感匹配头名称，仅返回列表中<b>第一个匹配</b>的头值，适配 HTTP 单值头的常规场景</p>
	 *
	 * @param name 请求头名称（可为 null，传入 null 直接返回 null）
	 * @return 第一个匹配的头值；无匹配项时返回 null
	 */
	public HeaderMeta getHeader(String name) {
		for (HeaderMeta headerMeta : this.headerMetas) {
			if (headerMeta.getName().equalsIgnoreCase(name)) {
				return headerMeta;
			}
		}
		return null;
	}

	/**
	 * 获取请求头的第一个匹配值
	 * <p>大小写不敏感匹配头名称，仅返回列表中<b>第一个匹配</b>的头值，适配 HTTP 单值头的常规场景</p>
	 *
	 * @param name 请求头名称（可为 null，传入 null 直接返回 null）
	 * @return 第一个匹配的头值；无匹配项时返回 null
	 */
	public String getHeaderValue(String name) {
		for (HeaderMeta headerMeta : this.headerMetas) {
			if (headerMeta.getName().equalsIgnoreCase(name)) {
				return headerMeta.getValue();
			}
		}
		return null;
	}

	/**
	 * 获取请求头的所有匹配值
	 * <p>大小写不敏感匹配头名称，返回列表中<b>所有匹配</b>的头值，适配 HTTP 多值头场景（如 "Set-Cookie" 多值）</p>
	 *
	 * @param name 请求头名称（可为 null，传入 null 直接返回空列表）
	 * @return 所有匹配的头值列表；无匹配项时返回<b>空列表（非 null）</b>，避免空指针
	 */
	public List<String> getHeaderValues(String name) {
		List<String> values = new ArrayList<>();
		for (HeaderMeta headerMeta : this.headerMetas) {
			if (headerMeta.getName().equalsIgnoreCase(name)) {
				values.add(headerMeta.getValue());
			}
		}
		return values;
	}

	/**
	 * 删除指定名称的请求头
	 *
	 * @param name 请求头名称（可为 null，传入 null 无任何删除操作）
	 */
	public void removeHeader(String name) {
		List<HeaderMeta> removeHeaders = new ArrayList<>();
		for (HeaderMeta headerMeta : headerMetas) {
			if (headerMeta.getName().equalsIgnoreCase(name)) {
				removeHeaders.add(headerMeta);
			}
		}
		headerMetas.removeAll(removeHeaders);
	}

	/**
	 * 根据Cookie名称精准匹配获取对应的Cookie对象
	 *
	 * @param name Cookie名称
	 * @return 匹配到的CookieMeta对象，无匹配项时返回null
	 */
	public CookieMeta getCookie(String name) {
		if (this.cookiesMeta == null) {
			return null;
		}
		return this.cookiesMeta.getCookie(name);
	}

	/**
	 * 获取所有的Cookie对象
	 *
	 * @return 匹配到的CookieMeta对象，无匹配项时返回null
	 */
	public List<CookieMeta> getCookie() {
		return this.cookiesMeta.getCookieMetas();
	}

	/**
	 * 根据Cookie名称精准匹配获取对应的SetCookie对象
	 *
	 * @param name Cookie名称
	 * @return 匹配到的CookieMeta对象，无匹配项时返回null
	 */
	public CookieMeta getSetCookie(String name) {
		for (HeaderMeta headerMeta : this.headerMetas) {
			if (!HeaderEnum.SET_COOKIE.getValue().equalsIgnoreCase(headerMeta.getName())){
				continue;
			}
			if ((headerMeta.getOriginalValue() instanceof CookieMeta cookieMeta) && cookieMeta.getName().equalsIgnoreCase(name)) {
				return cookieMeta;
			}
		}
		return null;
	}

	/**
	 * 获取所有的Cookie对象
	 *
	 * @return 匹配到的CookieMeta对象，无匹配项时返回[]
	 */
	public List<CookieMeta> getSetCookie() {
		List<CookieMeta> cookies = new ArrayList<>();
		for (HeaderMeta headerMeta : this.headerMetas) {
			if (!HeaderEnum.SET_COOKIE.getValue().equalsIgnoreCase(headerMeta.getName())){
				continue;
			}
			if (headerMeta.getOriginalValue() instanceof CookieMeta cookieMeta) {
				cookies.add(cookieMeta);
			}
		}
		return cookies;
	}

	/**
	 * 判断请求头列表是否为空
	 *
	 * @return true：为空；false：非空
	 */
	public boolean isEmpty() {
		return headerMetas.isEmpty();
	}
}
