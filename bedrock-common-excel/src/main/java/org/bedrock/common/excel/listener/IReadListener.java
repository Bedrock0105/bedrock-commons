package org.bedrock.common.excel.listener;

import cn.idev.excel.read.listener.ReadListener;

import java.util.List;

public interface IReadListener<T> extends ReadListener<T> {

	/**
	 * 获取解析数据
	 *
	 * @return 解析数据
	 */
	List<T> getData();
}
