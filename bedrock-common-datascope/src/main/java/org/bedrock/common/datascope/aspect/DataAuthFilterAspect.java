package org.bedrock.common.datascope.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.bedrock.common.datascope.annotation.DataScopeFilter;
import org.bedrock.common.datascope.handler.DataScopeAuthHolder;
import org.bedrock.common.datascope.model.DataScopeModel;
import org.bedrock.common.code.util.SpELParserUtil;
import org.bedrock.common.code.util.StringUtil;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

@Slf4j
@Order
@Aspect
public class DataAuthFilterAspect {

	@Around("@annotation(dataScopeFilter)")
	public Object around(ProceedingJoinPoint point, DataScopeFilter dataScopeFilter) throws Throwable {
		DataScopeModel dataScopeModel = DataScopeAuthHolder.getDataScopeModel();
		boolean auth = DataScopeAuthHolder.isAuth();

		try {
			if (StringUtil.isBlank(dataScopeFilter.mapperId())) {
				return point.proceed();
			}
			String mapperId = evalDataScopeFilterParam(point, dataScopeFilter.mapperId());
			if (StringUtil.isBlank(mapperId)) {
				return point.proceed();
			}
			DataScopeAuthHolder.setDataScopeModel(mapperId, dataScopeFilter);
			DataScopeAuthHolder.setIsAuth(true);
			return point.proceed();
		} finally {
			/**
			 * 恢复调用该方法前的状态
			 */
			DataScopeAuthHolder.setDataScopeModel(dataScopeModel);
			DataScopeAuthHolder.setIsAuth(auth);
		}
	}

	private String evalDataScopeFilterParam(ProceedingJoinPoint point, String value) {
		MethodSignature ms = (MethodSignature) point.getSignature();
		Method method = ms.getMethod();
		Object[] args = point.getArgs();
		return SpELParserUtil.parse(value, method, args, String.class, null);
	}
}
