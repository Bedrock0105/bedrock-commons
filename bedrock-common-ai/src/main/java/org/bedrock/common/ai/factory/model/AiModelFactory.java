package org.bedrock.common.ai.factory.model;

import org.bedrock.common.ai.model.factory.model.*;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;

import java.util.function.Predicate;

/**
 * AI 模型工厂接口。
 * <p>
 * 统一管理 ChatModel、ChatClient、EmbeddingModel、ImageModel、
 * TextToSpeechModel、TranscriptionModel 实例的创建、缓存获取与缓存销毁，
 * 内部维护实例缓存，复用已有客户端/模型，避免重复初始化连接资源。
 * </p>
 */
public interface AiModelFactory {

	// region ChatModel 相关操作

	/**
	 * 优先从缓存获取聊天模型实例。
	 * 缓存不存在时，自动调用 create 方法生成新实例并放入缓存。
	 *
	 * @param param 构建聊天模型所需参数
	 * @return 聊天模型实例
	 */
	ChatModel getChatModel(ChatModelCreateParam param);

	/**
	 * 全新创建聊天模型实例（不读取缓存、不自动存入缓存）。
	 *
	 * @param param 构建聊天模型所需参数
	 * @return 全新聊天模型实例
	 */
	ChatModel createChatModel(ChatModelCreateParam param);

	/**
	 * 根据唯一标识删除缓存中的聊天模型实例。
	 *
	 * @param id 模型唯一标识
	 * @return 被移除的聊天模型实例，不存在时返回 {@code null}
	 */
	ChatModel removeChatModel(String id);

	// endregion

	// region ChatClient 相关操作

	/**
	 * 优先从缓存获取对话客户端实例。
	 * 缓存不存在时，自动调用 create 方法生成新实例并放入缓存。
	 *
	 * @param param 构建对话客户端所需参数
	 * @return 对话客户端实例
	 */
	ChatClient getChatClient(ChatModelCreateParam param);

	/**
	 * 全新创建对话客户端实例（不读取缓存、不自动存入缓存）。
	 *
	 * @param param 构建对话客户端所需参数
	 * @return 全新对话客户端实例
	 */
	ChatClient createChatClient(ChatModelCreateParam param);

	/**
	 * 根据唯一标识删除缓存中的对话客户端实例。
	 *
	 * @param id 客户端唯一标识
	 * @return 被移除的对话客户端实例，不存在时返回 {@code null}
	 */
	ChatClient removeChatClient(String id);

	// endregion

	// region EmbeddingModel 相关操作

	/**
	 * 优先从缓存获取嵌入模型实例。
	 * 缓存不存在时，自动调用 create 方法生成新实例并放入缓存。
	 *
	 * @param param 构建嵌入模型所需参数
	 * @return 嵌入模型实例
	 */
	EmbeddingModel getEmbeddingModel(EmbeddingModelCreateParam param);

	/**
	 * 全新创建嵌入模型实例（不读取缓存、不自动存入缓存）。
	 *
	 * @param param 构建嵌入模型所需参数
	 * @return 全新嵌入模型实例
	 */
	EmbeddingModel createEmbeddingModel(EmbeddingModelCreateParam param);

	/**
	 * 根据唯一标识删除缓存中的嵌入模型实例。
	 *
	 * @param id 嵌入模型唯一标识
	 * @return 被移除的嵌入模型实例，不存在时返回 {@code null}
	 */
	EmbeddingModel removeEmbeddingModel(String id);

	// endregion

	// region ImageModel 相关操作

	/**
	 * 优先从缓存获取图像模型实例。
	 * 缓存不存在时，自动调用 create 方法生成新实例并放入缓存。
	 *
	 * @param param 构建图像模型所需参数
	 * @return 图像模型实例
	 */
	ImageModel getImageModel(ImageModelCreateParam param);

	/**
	 * 全新创建图像模型实例（不读取缓存、不自动存入缓存）。
	 *
	 * @param param 构建图像模型所需参数
	 * @return 全新图像模型实例
	 */
	ImageModel createImageModel(ImageModelCreateParam param);

	/**
	 * 根据唯一标识删除缓存中的图像模型实例。
	 *
	 * @param id 图像模型唯一标识
	 * @return 被移除的图像模型实例，不存在时返回 {@code null}
	 */
	ImageModel removeImageModel(String id);

	// endregion

	// region TextToSpeechModel 相关操作

	/**
	 * 优先从缓存获取文字转语音模型实例。
	 * 缓存不存在时，自动调用 create 方法生成新实例并放入缓存。
	 *
	 * @param param 构建文字转语音模型所需参数
	 * @return 文字转语音模型实例
	 */
	TextToSpeechModel getTextToSpeechModel(TextToSpeechModelCreateParam param);

	/**
	 * 全新创建文字转语音模型实例（不读取缓存、不自动存入缓存）。
	 *
	 * @param param 构建文字转语音模型所需参数
	 * @return 全新文字转语音模型实例
	 */
	TextToSpeechModel createTextToSpeechModel(TextToSpeechModelCreateParam param);

	/**
	 * 根据唯一标识删除缓存中的文字转语音模型实例。
	 *
	 * @param id 文字转语音模型唯一标识
	 * @return 被移除的文字转语音模型实例，不存在时返回 {@code null}
	 */
	TextToSpeechModel removeTextToSpeechModel(String id);

	// endregion

	// region TranscriptionModel 相关操作

	/**
	 * 优先从缓存获取语音转文字模型实例。
	 * 缓存不存在时，自动调用 create 方法生成新实例并放入缓存。
	 *
	 * @param param 构建语音转文字模型所需参数
	 * @return 语音转文字模型实例
	 */
	TranscriptionModel getTranscriptionModel(TranscriptionModelCreateParam param);

	/**
	 * 全新创建语音转文字模型实例（不读取缓存、不自动存入缓存）。
	 *
	 * @param param 构建语音转文字模型所需参数
	 * @return 全新语音转文字模型实例
	 */
	TranscriptionModel createTranscriptionModel(TranscriptionModelCreateParam param);

	/**
	 * 根据唯一标识删除缓存中的语音转文字模型实例。
	 *
	 * @param id 语音转文字模型唯一标识
	 * @return 被移除的语音转文字模型实例，不存在时返回 {@code null}
	 */
	TranscriptionModel removeTranscriptionModel(String id);

	// endregion

	/**
	 * 按条件批量清理缓存中的全部模型实例。
	 * 同时删除对应 ChatModel、ChatClient、EmbeddingModel、ImageModel、
	 * TextToSpeechModel、TranscriptionModel 缓存。
	 *
	 * @param predicate 缓存 key 匹配谓词
	 */
	void removeAll(Predicate<String> predicate);

	/**
	 * 按唯一标识清理全部缓存实例。
	 * 同时删除对应 ChatModel、ChatClient、EmbeddingModel、ImageModel、
	 * TextToSpeechModel、TranscriptionModel 缓存。
	 *
	 * @param id 统一实例唯一标识
	 */
	default void removeAll(String id) {
		removeChatClient(id);
		removeChatModel(id);
		removeEmbeddingModel(id);
		removeImageModel(id);
		removeTextToSpeechModel(id);
		removeTranscriptionModel(id);
	}

}
