package org.bedrock.common.log.operation.support;

import org.bedrock.common.log.annotation.OperationLog;
import org.bedrock.common.log.annotation.OperationLogs;
import org.bedrock.common.log.operation.model.OperationLogModel;
import org.bedrock.common.code.util.CollectionUtil;
import org.springframework.core.annotation.AnnotatedElementUtils;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class OperationLogSource {

	/**
	 * 把OperationLog 封装为 OperationLogModel
	 *
	 * @param method
	 * @param targetClass
	 * @return
	 */
	public static Collection<OperationLogModel> getOperationLogModels(Method method, Class<?> targetClass) {
		Collection<OperationLogModel> operationLogModels = new ArrayList<>();
		operationLogModels.addAll(getOperationLogs(method, targetClass));
		operationLogModels.addAll(getOperationLog(method, targetClass));
		return operationLogModels;
	}

	/**
	 * 获取 OperationLog 主键的信息
	 *
	 * @param method
	 * @param targetClass
	 * @return
	 */
	private static Collection<OperationLogModel> getOperationLogs(Method method, Class<?> targetClass) {
		Collection<OperationLogs> logRecordAnnotationAnnotations = AnnotatedElementUtils.findAllMergedAnnotations(method, OperationLogs.class);
		if (CollectionUtil.isEmpty(logRecordAnnotationAnnotations)) {
			return Collections.emptyList();
		}
		return logRecordAnnotationAnnotations.stream()
			.flatMap(logRecord -> Arrays.stream(logRecord.value()))
			.map(OperationLogSource::build)
			.collect(Collectors.toList());
	}

	/**
	 * 获取OperationLog 主键的信息
	 *
	 * @param method
	 * @param targetClass
	 * @return
	 */
	private static Collection<OperationLogModel> getOperationLog(Method method, Class<?> targetClass) {
		Collection<OperationLog> logRecordAnnotationAnnotations = AnnotatedElementUtils.findAllMergedAnnotations(method, OperationLog.class);
		if (CollectionUtil.isEmpty(logRecordAnnotationAnnotations)) {
			return Collections.emptyList();
		}
		return logRecordAnnotationAnnotations.stream()
			.map(OperationLogSource::build)
			.collect(Collectors.toList());
	}

	/**
	 * 生成操作日志model
	 *
	 * @param operationLog
	 * @return
	 */
	private static OperationLogModel build(OperationLog operationLog) {
		return OperationLogModel.builder()
			.extra(operationLog.extra())
			.fail(operationLog.fail())
			.condition(operationLog.condition())
			.subType(operationLog.subType())
			.type(operationLog.type())
			.success(operationLog.success())
			.build();
	}
}
