package org.bedrock.common.swagger.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = SwaggerProperties.PREFIX)
public class SwaggerProperties {

	public static final String PREFIX = "bedrock.swagger";

	/**
	 * 是否启用swagger
	 */
	private boolean enabled;

	/**
	 * 注解用于描述 API 文档的基本信息
	 */
	private Info info;

	/**
	 * 扫描的包路径
	 */
	private List<String> packagesToScan = List.of();

	/**
	 * 排除的包路径
	 */
	private List<String> packagesToExclude = List.of();

	/**
	 * 扫描的路径
	 */
	private List<String> pathsToMatch = List.of("/**");

	/**
	 * 排除的路径
	 */
	private List<String> pathsToExclude = List.of();

	@Getter
	@Setter
	public static final class Info {

		/**
		 * API 的标题或名称。这通常是 API 的简短描述，用于在文档中标识和区分不同的 API
		 */
		private String title;

		/**
		 * API 的详细描述。这可以是一个较长的文本，用于解释 API 的目的、功能、使用方式等。它是对标题和版本的补充，提供了更详细的信息。
		 */
		private String description;

		/**
		 * API 的服务条款。这是一个 URL，指向包含 API 使用条款和条件的页面。这有助于确保 API 的使用者了解并遵守相关的使用规定。
		 */
		private String termsOfService;

		/**
		 * API 的联系信息。这可以是一个 Contact 对象，包含姓名、URL 和电子邮件地址，用于提供 API 的支持或反馈渠道。
		 */
		private Contact contact;

		/**
		 * API 的许可信息。这是一个 License 对象，描述了 API 的许可类型（如 Apache 2.0）和许可 URL（如指向许可详细信息的页面）
		 */
		private License license;

		/**
		 * API 的版本。这有助于区分 API 的不同迭代，并允许使用者了解他们正在使用的是哪个版本的 API。
		 */
		private String version;
	}

	@Getter
	@Setter
	public static final class Contact {

		/**
		 * 姓名
		 */
		private String name;

		/**
		 * 地址
		 */
		private String url;

		/**
		 * 邮箱
		 */
		private String email;
	}

	@Getter
	@Setter
	public static final class License {

		/**
		 * 名称
		 */
		private String name;

		/**
		 * 地址
		 */
		private String url;
	}
}
