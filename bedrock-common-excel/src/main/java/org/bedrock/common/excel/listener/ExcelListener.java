package org.bedrock.common.excel.listener;

import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.event.AnalysisEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认监听器
 *
 * @param <T>
 */
public class ExcelListener<T> extends AnalysisEventListener<T> implements IReadListener<T> {

	private final List<T> dataList = new ArrayList<>();

	/**
	 * 跳过行数
	 */
	private Integer skipRowNum = 0;

	@Override
	public List<T> getData() {
		return dataList;
	}

	@Override
	public void invoke(T data, AnalysisContext context) {
		if (skipRowNum > 0) {
			skipRowNum--;
			return;
		}
		dataList.add(data);
	}

	@Override
	public void doAfterAllAnalysed(AnalysisContext context) {

	}

	public void setSkipRowNum(Integer skipRowNum) {
		this.skipRowNum = skipRowNum;
	}
}
