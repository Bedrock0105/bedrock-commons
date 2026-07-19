package org.bedrock.common.ai.factory.model;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.ai.enums.AiPlatformEnum;
import org.bedrock.common.ai.model.factory.model.*;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.Ordered;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Slf4j
public class DefaultAiModelFactoryImpl implements AiModelFactory, SmartInitializingSingleton {

	private final Map<String, ChatModel> chatModelCache = new ConcurrentHashMap<>();

	private final Map<String, ChatClient> chatClientCache = new ConcurrentHashMap<>();

	private final Map<String, EmbeddingModel> embeddingModelCache = new ConcurrentHashMap<>();

	private final Map<String, ImageModel> imageModelCache = new ConcurrentHashMap<>();

	private final Map<String, TextToSpeechModel> textToSpeechModelCache = new ConcurrentHashMap<>();

	private final Map<String, TranscriptionModel> transcriptionModelCache = new ConcurrentHashMap<>();

	/**
	 * AI 模型构造器映射。
	 * key: AI 平台枚举；value: 对应平台的模型构造器
	 */
	private final Map<AiPlatformEnum, AiModelBuilder> aiModelBuilderMap = new EnumMap<>(AiPlatformEnum.class);

	@Override
	public ChatModel getChatModel(ChatModelCreateParam param) {
		return chatModelCache.computeIfAbsent(param.id(), _id -> createChatModel(param));
	}

	@Override
	public ChatModel createChatModel(ChatModelCreateParam param) {
		AiModelBuilder builder = aiModelBuilderMap.get(param.aiPlatformEnum());
		if (builder != null) {
			return builder.createChatModel(param);
		}
		log.error("AI 平台[{}]暂不支持创建聊天模型", param.aiPlatformEnum());
		throw new UnsupportedOperationException("AI 平台[" + param.aiPlatformEnum() + "]暂不支持创建聊天模型");
	}

	@Override
	public ChatModel removeChatModel(String id) {
		return this.chatModelCache.remove(id);
	}

	@Override
	public ChatClient getChatClient(ChatModelCreateParam param) {
		return chatClientCache.computeIfAbsent(param.id(), _id -> ChatClient.builder(getChatModel(param)).build());
	}

	@Override
	public ChatClient createChatClient(ChatModelCreateParam param) {
		return ChatClient.builder(createChatModel(param)).build();
	}

	@Override
	public ChatClient removeChatClient(String id) {
		return this.chatClientCache.remove(id);
	}

	@Override
	public EmbeddingModel getEmbeddingModel(EmbeddingModelCreateParam param) {
		return embeddingModelCache.computeIfAbsent(param.id(), _id -> createEmbeddingModel(param));
	}

	@Override
	public EmbeddingModel createEmbeddingModel(EmbeddingModelCreateParam param) {
		AiModelBuilder builder = aiModelBuilderMap.get(param.aiPlatformEnum());
		if (builder == null) {
			log.error("AI 平台[{}]未注册模型构造器，无法创建嵌入模型", param.aiPlatformEnum());
			throw new UnsupportedOperationException("AI 平台[" + param.aiPlatformEnum() + "]未注册模型构造器，无法创建嵌入模型");
		}
		try {
			return builder.createEmbeddingModel(param);
		} catch (UnsupportedOperationException ex) {
			log.error("AI 平台[{}]暂不支持创建嵌入模型", param.aiPlatformEnum());
			throw ex;
		}
	}

	@Override
	public EmbeddingModel removeEmbeddingModel(String id) {
		return this.embeddingModelCache.remove(id);
	}

	@Override
	public ImageModel getImageModel(ImageModelCreateParam param) {
		return imageModelCache.computeIfAbsent(param.id(), _id -> createImageModel(param));
	}

	@Override
	public ImageModel createImageModel(ImageModelCreateParam param) {
		AiModelBuilder builder = aiModelBuilderMap.get(param.aiPlatformEnum());
		if (builder == null) {
			log.error("AI 平台[{}]未注册模型构造器，无法创建图像模型", param.aiPlatformEnum());
			throw new UnsupportedOperationException("AI 平台[" + param.aiPlatformEnum() + "]未注册模型构造器，无法创建图像模型");
		}
		try {
			return builder.createImageModel(param);
		} catch (UnsupportedOperationException ex) {
			log.error("AI 平台[{}]暂不支持创建图像模型", param.aiPlatformEnum());
			throw ex;
		}
	}

	@Override
	public ImageModel removeImageModel(String id) {
		return this.imageModelCache.remove(id);
	}

	@Override
	public TextToSpeechModel getTextToSpeechModel(TextToSpeechModelCreateParam param) {
		return textToSpeechModelCache.computeIfAbsent(param.id(), _id -> createTextToSpeechModel(param));
	}

	@Override
	public TextToSpeechModel createTextToSpeechModel(TextToSpeechModelCreateParam param) {
		AiModelBuilder builder = aiModelBuilderMap.get(param.aiPlatformEnum());
		if (builder == null) {
			log.error("AI 平台[{}]未注册模型构造器，无法创建文字转语音模型", param.aiPlatformEnum());
			throw new UnsupportedOperationException("AI 平台[" + param.aiPlatformEnum() + "]未注册模型构造器，无法创建文字转语音模型");
		}
		try {
			return builder.createTextToSpeechModel(param);
		} catch (UnsupportedOperationException ex) {
			log.error("AI 平台[{}]暂不支持创建文字转语音模型", param.aiPlatformEnum());
			throw ex;
		}
	}

	@Override
	public TextToSpeechModel removeTextToSpeechModel(String id) {
		return this.textToSpeechModelCache.remove(id);
	}

	@Override
	public TranscriptionModel getTranscriptionModel(TranscriptionModelCreateParam param) {
		return transcriptionModelCache.computeIfAbsent(param.id(), _id -> createTranscriptionModel(param));
	}

	@Override
	public TranscriptionModel createTranscriptionModel(TranscriptionModelCreateParam param) {
		AiModelBuilder builder = aiModelBuilderMap.get(param.aiPlatformEnum());
		if (builder == null) {
			log.error("AI 平台[{}]未注册模型构造器，无法创建语音转文字模型", param.aiPlatformEnum());
			throw new UnsupportedOperationException("AI 平台[" + param.aiPlatformEnum() + "]未注册模型构造器，无法创建语音转文字模型");
		}
		try {
			return builder.createTranscriptionModel(param);
		} catch (UnsupportedOperationException ex) {
			log.error("AI 平台[{}]暂不支持创建语音转文字模型", param.aiPlatformEnum());
			throw ex;
		}
	}

	@Override
	public TranscriptionModel removeTranscriptionModel(String id) {
		return this.transcriptionModelCache.remove(id);
	}

	@Override
	public void removeAll(Predicate<String> predicate) {
		chatModelCache.keySet().removeIf(predicate);
		chatClientCache.keySet().removeIf(predicate);
		embeddingModelCache.keySet().removeIf(predicate);
		imageModelCache.keySet().removeIf(predicate);
		textToSpeechModelCache.keySet().removeIf(predicate);
		transcriptionModelCache.keySet().removeIf(predicate);
	}

	/**
	 * 容器单例初始化完成后，通过 SPI 发现并注册所有 {@link AiModelBuilder} 实现。
	 */
	@Override
	public void afterSingletonsInstantiated() {
		ServiceLoader.load(AiModelBuilder.class)
			.stream()
			.map(ServiceLoader.Provider::get)
			.sorted(Comparator.comparing(Ordered::getOrder))
			.forEach(builder -> {
				aiModelBuilderMap.put(builder.getAiPlatform(), builder);
				log.info("已注册AI模型构造器: {}", builder.getAiPlatform());
			});

	}
}
