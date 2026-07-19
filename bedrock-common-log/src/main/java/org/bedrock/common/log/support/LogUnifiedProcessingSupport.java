package org.bedrock.common.log.support;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.servlet.http.HttpServletRequest;
import org.bedrock.common.auth.util.AuthUtil;
import org.bedrock.common.log.model.LogOperation;
import org.bedrock.common.log.service.ILogSaveService;
import org.bedrock.common.code.server.ServerInfo;
import org.bedrock.common.code.util.SpringUtil;
import org.bedrock.common.code.util.WebUtil;

import java.time.LocalDateTime;

public class LogUnifiedProcessingSupport {

	private static ServerInfo serverInfo;

	private static ILogSaveService logSaveService;

	/**
	 * 日志统一处理
	 */
	public static void logOperation(LogOperation logOperation) {
		logOperation.setServiceName(getServerInfo().getAppName());
		logOperation.setServerIp(getServerInfo().getAddress());
		logOperation.setEnv(getServerInfo().getActiveProfile());
		logOperation.setTenantId(AuthUtil.getTenantId());
		HttpServletRequest request = WebUtil.getRequest();
		if (request != null) {
			logOperation.setRemoteIp(WebUtil.getIP(request));
			logOperation.setRequestUrl(request.getRequestURI());
			logOperation.setRequestType(request.getMethod());
			logOperation.setUserAgent(request.getHeader("user-agent"));
		}
		logOperation.setCreateTime(LocalDateTime.now());
		logOperation.setCreateUserId(AuthUtil.getUserId());
		logOperation.setId(IdWorker.getId());
		getLogSaveService().saveOperationLog(logOperation);
	}

	private static ServerInfo getServerInfo() {
		if (serverInfo == null) {
			serverInfo = SpringUtil.getBean(ServerInfo.class);
		}
		return serverInfo;
	}

	private static ILogSaveService getLogSaveService() {
		if (logSaveService == null) {
			logSaveService = SpringUtil.getBean(ILogSaveService.class);
		}
		return logSaveService;
	}
}
