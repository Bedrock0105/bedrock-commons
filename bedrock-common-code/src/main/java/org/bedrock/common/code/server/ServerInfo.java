package org.bedrock.common.code.server;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.constant.EnvConstant;
import org.bedrock.common.code.util.INetUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 服务的基本信息
 */
@Slf4j
@Getter
@AutoConfiguration
public class ServerInfo {

	private final ServerProperties serverProperties;

	private final Environment environment;

	private final String appName;

	private final String hostName;

	private final String ip;

	private Integer port;

	private String address;

	private String contextPath;

	private final String activeProfile;

	private static long startTime = -1L;

	public ServerInfo(ServerProperties serverProperties, Environment environment) {
		this.serverProperties = serverProperties;
		this.environment = environment;
		this.appName = environment.getProperty("spring.application.name");
		this.hostName = INetUtil.getHostName();
		this.ip = INetUtil.getHostIp();
		this.port = serverProperties.getPort();
		this.contextPath = serverProperties.getServlet().getContextPath();
		String[] activeProfiles = environment.getActiveProfiles();
		this.activeProfile = (activeProfiles.length > 0 ? String.join(",", activeProfiles) : EnvConstant.DEV_CODE);
		if (!StringUtils.hasText(contextPath)) {
			this.contextPath = "/";
		}
		this.address = String.format("%s:%d", ip, port);
		if (ServerInfo.startTime <= 0L) {
			ServerInfo.startTime = System.currentTimeMillis();
		}
	}

	public long getStartTime() {
		return ServerInfo.startTime;
	}

	public static void setStartTime(long startTime) {
		if (startTime <= 0L) {
			return;
		}
		if (ServerInfo.startTime <= 0L) {
			ServerInfo.startTime = startTime;
		}

	}

	@Order
	@EventListener(WebServerInitializedEvent.class)
	public void applicationReadyEvent(WebServerInitializedEvent event) {
		if (port == null || port == 0) {
			this.port = event.getWebServer().getPort();
			this.address = String.format("%s:%d", ip, port);
		}
	}

	@Async
	@Order
	@EventListener(ApplicationReadyEvent.class)
	public void applicationReadyEvent(ApplicationReadyEvent event) {
		long startupTime = System.currentTimeMillis() - ServerInfo.startTime;
		log.info("=========================================");
		log.info("🎉 应用 '{}' 启动成功！耗时: {}ms", appName, startupTime);
		log.info("-----------------------------------------");
		log.info("🔢 端口:          {}", port);
		log.info("📂 上下文路径:     {}", contextPath);
		log.info("🌍 激活环境:       {}", activeProfile);
		log.info("⏱️ 启动时间:       {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		log.info("=========================================");
	}
}
