package org.bedrock.common.ai.enums;

import lombok.Getter;

/**
 * ai 厂商
 */
@Getter
public enum AiPlatformEnum {
	// ===================== 国内厂商 =====================
	DEEP_SEEK("deepseek", "DeepSeek 深度求索"),
	DASH_SCOPE("dashscope", "通义千问（阿里云DashScope）"),
	MINIMAX("minimax", "MiniMax 稀宇科技"),
	MOONSHOT("moonshot", "月之暗面 Moonshot Kimi"),
	ZHI_PU("zhipu", "智谱清言 GLM"),
	QIAN_FAN("qianfan", "百度千帆 文心一言"),
	HUNYUAN("hunyuan", "腾讯混元"),
	SPARK("spark", "讯飞星火"),
	BAICHUAN("baichuan", "百川智能"),
	VOLCENGINE("volcengine", "火山方舟（字节企业）"),
	YI("yi", "零一万物 Yi"),
	SILICON_FLOW("siliconflow", "硅基流动"),

	// ===================== 海外厂商 =====================
	ANTHROPIC("anthropic", "Anthropic Claude"),
	AZURE_OPENAI("azure_openai", "Azure OpenAI 微软"),
	GEMINI("gemini", "Gemini 谷歌"),
	OPENAI("openai", "OpenAI"),
	MISTRAL("mistral", "Mistral AI"),
	OLLAMA("ollama", "Ollama 本地开源模型"),
	COHERE("cohere", "Cohere"),
	XAI("xai", "xAI Grok"),
	;

	private final String platform;

	private final String description;

	AiPlatformEnum(String platform, String description) {
		this.platform = platform;
		this.description = description;
	}

	/**
	 * 根据平台名称获取枚举
	 *
	 * @param platform 平台名称
	 * @return 枚举
	 */
	public static AiPlatformEnum of(String platform) {
		for (AiPlatformEnum aiPlatformEnum : AiPlatformEnum.values()) {
			if (aiPlatformEnum.platform.equals(platform)) {
				return aiPlatformEnum;
			}
		}
		throw new IllegalArgumentException("invalid ai platform: " + platform);
	}
}
