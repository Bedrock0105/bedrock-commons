package org.bedrock.common.excel.model;

import lombok.Getter;
import lombok.Setter;
import org.bedrock.common.code.constant.StringPool;
import org.bedrock.common.code.util.ObjectUtil;
import org.bedrock.common.code.util.StringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Excel 多级联动下拉框配置模型
 * <p>
 * 用于描述「父子级联动的下拉框规则」，支持无限级联动（如：省→市→区、部门→岗位→职级），
 * 核心逻辑：子级下拉选项的可选范围，依赖父级字段的选中值（通过 parentName 关联父级字段/父级选项）。
 * 配合对应的解析器/拦截器，可在 Excel 中实现「选择父级后，子级仅显示关联选项」的联动效果。
 */
@Getter
@Setter
public class ExcelDownOption {

	public static final String DEFAULT_PARENT_FIELD = "DEFAULT";

	/**
	 * 当前层级对应的 Java 字段名（必须与实体类字段名一致，用于定位 Excel 列）
	 * 示例：province（省）、city（市）、district（区）
	 */
	private String currentField;

	/**
	 * 当前层级对应的列索引
	 * 示例：0、1、2
	 */
	private Integer currentColumnIndex;

	/**
	 * 父级层级对应的 Java 字段名（关联上一级列）
	 * 规则：
	 * - 根级（第一级，如省）：parentField = null
	 * - 子级（如市）：parentField = "province"（父级字段名）
	 */
	private String parentField;

	/**
	 * 父级层级对应的列索引
	 */
	private Integer parentColumnIndex;

	/**
	 * 联动选项映射（核心！父级选中值 → 当前级可选选项列表）
	 * 规则：
	 * - 根级（parentField=null）：key 固定为 "DEFAULT"，value 是根级所有选项
	 * - 子级（parentField≠null）：key 是父级的选项值（如 "广东省"），value 是对应子选项（如 ["广州市", "深圳市"]）
	 */
	private Map<String, List<String>> optionMap = Map.of();

	/**
	 * 下拉框生效的起始行号（当前列专属）
	 * <p>
	 * 仅对当前字段对应的Excel列生效，采用Excel原生行号规则（从0开始计数）。
	 * 默认值1：适配「第0行是表头、第1行开始是数据行」的常规Excel模板结构，避免下拉框覆盖表头。
	 * 采用的下标，行数-1 就是下标
	 *
	 * @return 起始行号，默认值
	 */
	private int dropdownStartRow = 1;

	/**
	 * 下拉框生效的结束行号（当前列专属）
	 * <p>
	 * 仅对当前字段对应的Excel列生效，控制下拉框的最大生效范围，避免无限制扩展影响Excel性能。
	 * 默认值1000：覆盖大部分业务场景的最大数据行需求，若需支持更多行可手动调整。
	 *
	 * @return 结束行号，默认值1000
	 */
	private int dropdownEndRow = 1000;

	/**
	 * 错误提示框
	 */
	private boolean showErrorBox = false;

	/**
	 * 错误提示框标题
	 */
	private String errorTitle;

	/**
	 * 错误提示信息
	 */
	private String errorMessage;

	/**
	 * 错误样式
	 * 0 --->STOP
	 * 1 --->WARNING
	 * 2 --->INFO
	 */
	private int errorStyle;

	/**
	 * 输入提示框
	 */
	private boolean showPromptBox = false;

	/**
	 * 输入提示框标题
	 */
	private String promptTitle;

	/**
	 * 输入提示信息
	 */
	private String promptMessage;

	/**
	 * 便捷判断：是否为根级（无父级）
	 */
	public boolean isRootLevel() {
		return StringUtil.isBlank(parentField);
	}

	/**
	 * 便捷判断：是否为空
	 */
	public boolean isEmpty() {
		return optionMap == null || optionMap.isEmpty() || StringUtil.isBlank(currentField);
	}

	/**
	 * 添加联动选项
	 *
	 * @param parentValue 父级选项值
	 * @param options     当前级选项列表
	 * @return this
	 */
	public ExcelDownOption addOption(String parentValue, String... options) {
		if (ObjectUtil.isEmpty(options)) {
			return this;
		}
		if (optionMap.isEmpty()) {
			this.optionMap = new java.util.HashMap<>();
		}
		this.optionMap.put(parentValue, Arrays.asList(options));
		return this;
	}

	public ExcelDownOption(Object obj) {
		if (ObjectUtil.isEmpty(obj)) {
			return;
		}
		if (obj instanceof String str) {
			this.optionMap = Map.of(DEFAULT_PARENT_FIELD, Arrays.asList(str.split(StringPool.COMMA)));
		} else if (obj instanceof String[] options) {
			this.optionMap = Map.of(DEFAULT_PARENT_FIELD, Arrays.asList(options));
		} else if (obj instanceof List<?> list) {
			this.optionMap = Map.of(DEFAULT_PARENT_FIELD, list.stream().map(String::valueOf).toList());
		} else {
			throw new IllegalArgumentException("参数 obj 必须为 String、String[]、List<?> 类型");
		}
	}

	public ExcelDownOption(String options) {
		if (ObjectUtil.isEmpty(options)) {
			return;
		}
		this.optionMap = Map.of(DEFAULT_PARENT_FIELD, Arrays.asList(options.split(StringPool.COMMA)));
	}

	public ExcelDownOption(String... options) {
		if (ObjectUtil.isEmpty(options)) {
			return;
		}
		this.optionMap = Map.of(DEFAULT_PARENT_FIELD, Arrays.asList(options));
	}

	public ExcelDownOption(List<String> options) {
		if (ObjectUtil.isEmpty(options)) {
			return;
		}
		this.optionMap = Map.of(DEFAULT_PARENT_FIELD, options);
	}

	public ExcelDownOption() {
	}
}
