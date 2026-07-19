package org.bedrock.common.log.listener;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

import java.util.Properties;

public class LoggerStartupListener extends ContextAwareBase implements LoggerContextListener, LifeCycle {

	@Override
	public boolean isResetResistant() {
		return false;
	}

	@Override
	public void onStart(LoggerContext context) {

	}

	@Override
	public void onReset(LoggerContext context) {

	}

	@Override
	public void onStop(LoggerContext context) {

	}

	@Override
	public void onLevelChange(Logger logger, Level level) {

	}

	@Override
	public void start() {
		Context context = getContext();
		Properties props = System.getProperties();
		context.putProperty("APP_NAME", props.getProperty("spring.application.name"));
		context.putProperty("PROFILE", props.getProperty("spring.profiles.active"));
	}

	@Override
	public void stop() {

	}

	@Override
	public boolean isStarted() {
		return false;
	}
}
