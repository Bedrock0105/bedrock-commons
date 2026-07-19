package org.bedrock.common.excel.support;

import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.builder.ExcelWriterBuilder;
import cn.idev.excel.write.builder.ExcelWriterSheetBuilder;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.api.R;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.excel.annotation.ResponseExcel;
import org.bedrock.common.excel.util.ExcelUtil;
import org.bedrock.common.log.exception.ServiceException;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
public class ResponseReturnValueHandler implements HandlerMethodReturnValueHandler {

	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return returnType.hasMethodAnnotation(ResponseExcel.class);
	}

	@Override
	public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
		mavContainer.setRequestHandled(true);
		ResponseExcel methodAnnotation = returnType.getMethodAnnotation(ResponseExcel.class);
		/* check */
		HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
		Assert.state(response != null, "No HttpServletResponse");
		if (returnValue instanceof R<?> r) {
			returnValue = r.getData();
		}
		if (!(returnValue instanceof List) && !(returnValue instanceof Map<?, ?>)) {
			throw new ServiceException("The return value annotated with @ResponseExcel must be a List or a Map");
		}
		String fileName = methodAnnotation.fileName();
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
		response.setHeader("Content-disposition", "attachment;filename=" + fileName + methodAnnotation.excelType().getValue());
		ExcelWriterBuilder excelWriterBuilder = buildExcelWriterBuilder(response.getOutputStream(), methodAnnotation);
		/**
		 * 返回值是list 就是单个sheet
		 */
		if (returnValue instanceof List list) {
			ResponseExcel.Sheet sheet = methodAnnotation.sheets()[0];
			excelWriterBuilder.sheet(sheet.index(), sheet.name())
				.head(sheet.head())
				.doWrite(list);
		} else {
			Map<?, ?> map = (Map<?, ?>) returnValue;
			ExcelWriter excelWriter = excelWriterBuilder.build();
			for (ResponseExcel.Sheet sheet : methodAnnotation.sheets()) {
				Object result = map.get(sheet.name());
				if (result == null) {
					result = map.get(sheet.index());
				}
				if (!(result instanceof List<?>)) {
					throw new ServiceException("The value generic type of Map must be List");
				}
				ExcelWriterSheetBuilder sheetBuilder = FastExcel.writerSheet(sheet.index(), sheet.name())
					.head(sheet.head());
				excelWriter.write((List<?>) result, sheetBuilder.build());
			}
			excelWriter.finish();
		}
	}

	private ExcelWriterBuilder buildExcelWriterBuilder(OutputStream outputStream, ResponseExcel methodAnnotation) {
		ExcelWriterBuilder excelWriterBuilder = FastExcel.write(outputStream)
			.inMemory(methodAnnotation.inMemory())
			.excelType(methodAnnotation.excelType())
			.registerWriteHandler(ExcelUtil.EXCEL_CUSTOMIZE_WRITE_HANDLER);
		if (StringUtil.isNotBlank(methodAnnotation.password())) {
			excelWriterBuilder.password(methodAnnotation.password());
		}
		return excelWriterBuilder;
	}
}
