package org.bedrock.common.ai.model.factory.model;

import org.bedrock.common.ai.enums.AiPlatformEnum;

/**
 * 图像模型创建参数。
 *
 * @param id             模型唯一标识，用于工厂缓存 key
 * @param aiPlatformEnum AI 平台枚举
 * @param url            模型服务地址
 * @param key            API 密钥
 * @param path           API 半路径（相对 url），为空时使用厂商/SDK 默认 path
 */
public record ImageModelCreateParam(String id,
                                    AiPlatformEnum aiPlatformEnum,
                                    String url,
                                    String key,
                                    String path) {

}
