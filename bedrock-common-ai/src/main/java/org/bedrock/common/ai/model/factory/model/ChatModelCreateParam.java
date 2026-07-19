package org.bedrock.common.ai.model.factory.model;

import org.bedrock.common.ai.enums.AiPlatformEnum;

/**
 * 聊天模型创建参数
 *
 * @param id             模型ID
 * @param aiPlatformEnum AI平台枚举
 * @param url            模型地址
 * @param key            模型密钥
 * @param path           API 半路径（相对 url），为空时使用厂商/SDK 默认 path
 */
public record ChatModelCreateParam(String id,
                                   AiPlatformEnum aiPlatformEnum,
                                   String url,
                                   String key,
                                   String path) {

}
