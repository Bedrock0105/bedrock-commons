package org.bedrock.common.log.operation.support;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.bedrock.common.auth.util.AuthUtil;
import org.bedrock.common.log.constant.OperationLogConstant;
import org.bedrock.common.log.model.LogOperation;
import org.bedrock.common.log.operation.convert.AnalysisClassConvert;
import org.bedrock.common.log.operation.convert.AnalysisClassConvertManager;
import org.bedrock.common.log.operation.model.OperationLogModel;
import org.bedrock.common.log.support.LogUnifiedProcessingSupport;
import org.bedrock.common.log.util.LogUtil;
import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.util.CollectionUtil;
import org.bedrock.common.code.util.JsonUtil;
import org.bedrock.common.code.util.SpELParserUtil;
import org.bedrock.common.code.util.StringUtil;
import org.springframework.expression.EvaluationContext;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OperationLogCodeSupport {

	private static final Pattern pattern = Pattern.compile("\\{\\s*(\\w*)\\s*\\{(.*?)}}");

	private static final Map<String, IParseFunction> FUNCTIONS = new HashMap<>();

	private static final Map<String, IDeffParseFunction> DEFF_FUNCTIONS = new HashMap<>();

	public static IParseFunction getParseFunctionMap(String key) {
		return FUNCTIONS.get(key);
	}

	public static IDeffParseFunction getDeffParseFunctionMap(String key) {
		return DEFF_FUNCTIONS.get(key);
	}

	/**
	 * 开始解析日志信息
	 *
	 * @param point
	 * @return
	 * @throws Throwable
	 */
	public static Object proceed(ProceedingJoinPoint point) throws Throwable {
		//获取方法
		String methodName = point.getSignature().getName();
		// 发送异步日志事件
		long beginTime = System.currentTimeMillis();

		/**
		 * 获取指向的方法
		 */
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = signature.getMethod();
		//执行方法
		Object result = null;
		Throwable error = null;
		Object[] args = point.getArgs();
		try {
			result = point.proceed();
		} catch (Throwable e) {
			error = e;
		}
		MethodSignature methodSignature = (MethodSignature) point.getSignature();
		String[] parameterNames = methodSignature.getParameterNames();

		//执行时长(毫秒)
		long time = System.currentTimeMillis() - beginTime;

		EvaluationContext context = SpELParserUtil.buildEvaluationContext(method, args, point.getTarget());
		context.setVariable(OperationLogConstant.RESULT_KEY, result);
		context.setVariable(OperationLogConstant.ERROR_MSG_KEY, error);
		Map<String, Object> variables = LogRecordContext.getVariables();
		LogRecordContext.clear();
		if (CollectionUtil.isNotEmpty(variables)) {
			variables.forEach(context::setVariable);
		}

		Collection<OperationLogModel> operationLogModels = OperationLogSource.getOperationLogModels(method, point.getTarget().getClass());
		/**
		 * 开始解析日志信息
		 */
		if (CollectionUtil.isNotEmpty(operationLogModels)) {
			for (OperationLogModel operationLogModel : operationLogModels) {
				handleOperationLogModel(operationLogModel, error, point.getTarget().getClass(), methodName, time, context, args, parameterNames);
			}
		}
		if (error != null) {
			throw error;
		}
		return result;
	}

	/**
	 * @param operationLogModel 模版
	 * @param error             错误信息
	 * @param methodName        方法名
	 * @param time              执行时长
	 */
	private static void handleOperationLogModel(OperationLogModel operationLogModel,
												Throwable error, Class<?> targetClass,
												String methodName, long time,
												EvaluationContext context, Object[] args,
												String[] parameterNames) {

		/**
		 * 条件不是空的时候先校验条件是否成立
		 */
		if (StringUtil.isNotBlank(operationLogModel.getCondition())) {
			Map<String, String> stringStringMap = parseSpel(Collections.singletonList(operationLogModel.getCondition()), context);
			String string = stringStringMap.get(operationLogModel.getCondition());
			if ("false".equals(string)) {
				return;
			}
		}

		/**
		 * 开始组装模版
		 */
		String template = operationLogModel.getSuccess();
		if (error != null) {
			template = operationLogModel.getFail();
		}
		List<String> list = Arrays.asList(template, operationLogModel.getExtra(),
			operationLogModel.getType(), operationLogModel.getSubType());
		/**
		 * 解析模版
		 */
		Map<String, String> stringStringMap = parseSpel(list, context);
		LogOperation logOperation = new LogOperation();
		logOperation.setAction(stringStringMap.get(template));
		logOperation.setType(stringStringMap.get(operationLogModel.getType()));
		logOperation.setSubType(stringStringMap.get(operationLogModel.getSubType()));
		logOperation.setExtra(stringStringMap.get(operationLogModel.getExtra()));
		logOperation.setUserName(AuthUtil.getNickname());
		logOperation.setDuration(time);
		logOperation.setMethod(targetClass.getName().concat(StringPool.DOT).concat(methodName));
		List<LogUtil.FileInfo> fileInfos = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();
		if (args != null && args.length > 0) {
			LogUtil.logParameters(args, parameterNames, params, fileInfos);
			if (CollectionUtil.isNotEmpty(fileInfos)) {
				params.put("fileInfos", fileInfos);
			}
			logOperation.setRequestParam(JsonUtil.toJson(params));
		} else {
			logOperation.setRequestParam("");
		}
		LogUnifiedProcessingSupport.logOperation(logOperation);
	}

	/**
	 * 解析Spel
	 *
	 * @param templates
	 * @return
	 */
	private static Map<String, String> parseSpel(Collection<String> templates,
												 EvaluationContext context) {
		Map<String, String> result = new HashMap<>();

		for (String template : templates) {
			if (!template.contains("{")) {
				result.put(template, template);
				continue;
			}
			Matcher matcher = pattern.matcher(template);
			StringBuilder parsedStr = new StringBuilder();
			while (matcher.find()) {
				String expression = matcher.group(2);
				String functionName = matcher.group(1);
				if (functionName.startsWith("_DIFF")) {
					String[] split = StringUtil.split(expression, ",");
					IDeffParseFunction iDeffParseFunction = DEFF_FUNCTIONS.get(functionName);
					if (iDeffParseFunction != null) {
						if (split.length == 2) {
							Object newValue = SpELParserUtil.parse(split[0], context, null);
							Object oldValue = SpELParserUtil.parse(split[1], context, null);
							expression = iDeffParseFunction.deff(newValue, oldValue);
						} else {
							Object newValue = SpELParserUtil.parse(split[0], context, null);
							expression = iDeffParseFunction.deff(newValue);
						}
					}
				} else {
					Object value = SpELParserUtil.parse(expression, context, null);
					expression = getFunction(value, functionName);
				}
				matcher.appendReplacement(parsedStr, Matcher.quoteReplacement(expression));
			}
			matcher.appendTail(parsedStr);
			result.put(template, parsedStr.toString());
		}
		return result;
	}

	/**
	 * 指向函数
	 *
	 * @param obj
	 * @param functionName
	 * @return
	 */
	private static String getFunction(Object obj, String functionName) {
		if (obj == null) {
			return "";
		}
		IParseFunction parseFunctionMap = OperationLogCodeSupport.getParseFunctionMap(functionName);
		if (parseFunctionMap != null) {
			return parseFunctionMap.apply(obj);
		}
		AnalysisClassConvert<?> analysisClassConvert = AnalysisClassConvertManager.getAnalysisClassConvert(obj.getClass());
		if (analysisClassConvert != null) {
			return analysisClassConvert.convert(obj, null);
		}
		return obj.toString();
	}

	/**
	 * 注册函数
	 *
	 * @param parseFunctionList
	 */
	public static void registerFunction(Collection<IParseFunction> parseFunctionList) {
		for (IParseFunction iParseFunction : parseFunctionList) {
			FUNCTIONS.put(iParseFunction.functionName(), iParseFunction);
		}
	}

	public static void registerDeffFunction(Collection<IDeffParseFunction> deffParseFunctionList) {
		for (IDeffParseFunction iDeffParseFunction : deffParseFunctionList) {
			DEFF_FUNCTIONS.put(iDeffParseFunction.functionName(), iDeffParseFunction);
		}
	}
}
