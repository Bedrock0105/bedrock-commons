package org.bedrock.common.mybatisplus.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.bedrock.common.mybatisplus.base.Query;
import org.bedrock.common.code.util.NumberUtil;

public class PageUtil {

	/**
	 * 获取分页对象
	 */
	public static <T> IPage<T> getPage(Query query) {
		return new Page<>(NumberUtil.toInt(query.getCurrent(), 1), NumberUtil.toInt(query.getSize(), 10));
	}

	/**
	 * 获取分页对象
	 *
	 * @param query            查询参数
	 * @param optimizeCountSql 是否优化count sql
	 */
	public static <T> IPage<T> getPage(Query query, boolean optimizeCountSql) {
		Page<T> page = new Page<>(NumberUtil.toInt(query.getCurrent(), 1), NumberUtil.toInt(query.getSize(), 10));
		page.setOptimizeCountSql(optimizeCountSql);
		return page;
	}
}
