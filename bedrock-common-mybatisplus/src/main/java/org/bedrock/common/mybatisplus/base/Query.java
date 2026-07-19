package org.bedrock.common.mybatisplus.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.bedrock.common.mybatisplus.util.PageUtil;

@Data
@Schema(description = "查询参数")
public class Query {

	/**
	 * 当前页
	 */
	@Schema(description = "当前页")
	private Integer current;

	/**
	 * 每页的数量
	 */
	@Schema(description = "每页的数量")
	private Integer size;

	/**
	 * 转换为分页对象
	 */
	public <T> IPage<T> toPage() {
		return PageUtil.getPage(this);
	}

	/**
	 * 转换为分页对象
	 *
	 * @param optimizeCountSql 是否优化count sql
	 */
	public <T> IPage<T> toPage(boolean optimizeCountSql) {
		return PageUtil.getPage(this, optimizeCountSql);
	}
}
