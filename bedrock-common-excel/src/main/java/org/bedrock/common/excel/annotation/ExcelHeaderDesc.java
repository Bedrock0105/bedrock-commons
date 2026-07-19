package org.bedrock.common.excel.annotation;

import java.lang.annotation.*;

/**
 * Excel表头上方跨列描述注解
 * <p>作用：在Excel导出时，于表头行之前插入一行「跨所有列」的描述文本行，支持富文本样式配置</p>
 * <p>适用场景：导出Excel时需要在表头顶部添加说明性文本（如填写须知、数据说明等）</p>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelHeaderDesc {

	/**
	 * 表头描述内容（核心配置）
	 * <h3>特性说明：</h3>
	 * 1. 换行支持：使用换行符 {@code \n} 可分割多行文本，Excel中自动换行显示；
	 * 2. 富文本支持：可嵌套 {@code <WriteFont>} 标签配置文本样式，标签属性对应 {@link cn.idev.excel.write.metadata.style.WriteFont} 字段；
	 * 3. 标签语法：{@code <WriteFont 键=值>需要样式的文本</WriteFont>}，支持多属性组合（如字体、颜色、加粗）。
	 *
	 * <h3>标签支持的核心属性示例：</h3>
	 * <pre>
	 * 1. 基础样式：<WriteFont fontName=宋体 color=10 bold=true>姓名</WriteFont>
	 * 2. 多属性组合：<WriteFont fontName=微软雅黑 fontSize=14 color=17 italic=true>所属组织</WriteFont>
	 * 3. 换行+样式："填写须知：\n1. <WriteFont color=10>红色字段</WriteFont>为必填项\n2. 手机号格式为11位数字"
	 * </pre>
	 *
	 * <h3>富文本兼容性说明：</h3>
	 * - 当使用FastExcel默认的 {@link org.apache.poi.xssf.streaming.SXSSFWorkbook} 导出时，不支持RichTextString，{@code <WriteFont>} 标签会失效（仅显示纯文本）；
	 * - 需设置inMemory=true（切换为 {@link org.apache.poi.xssf.usermodel.XSSFWorkbook}）才能使富文本生效，此模式为内存内操作，大批量数据导出时禁用（易OOM）。
	 *
	 * @return 描述文本内容，支持换行符和WriteFont富文本标签
	 */
	String value();

	/**
	 * 描述行的高度（单位：磅，1磅≈0.035毫米）
	 * <p>默认值30磅，适配单行/短多行文本；若描述内容行数多，建议根据实际调整（如60磅适配3行文本）</p>
	 *
	 * @return 行高，取值范围建议5~500磅（超出Excel默认显示范围可能被截断）
	 */
	short rowHeight() default 30;

	/**
	 * 描述文本的默认加粗状态（未被WriteFont标签覆盖的文本生效）
	 * <p>优先级：WriteFont标签的bold属性 > 此全局bold属性</p>
	 *
	 * @return true=默认加粗（默认值），false=默认不加粗
	 */
	boolean bold() default true;

	/**
	 * 描述文本的默认字体名称（未被WriteFont标签覆盖的文本生效）
	 * <p>支持Excel内置字体（如宋体、微软雅黑、Arial等），需确保导出环境字体存在</p>
	 * <p>优先级：WriteFont标签的fontName属性 > 此全局fontName属性</p>
	 *
	 * @return 字体名称，默认值「宋体」
	 */
	String fontName() default "宋体";

	/**
	 * 描述文本的默认字体大小（单位：磅）
	 * <p>优先级：WriteFont标签的fontHeightInPoints属性 > 此全局fontHeightInPoints属性</p>
	 *
	 * @return 字体大小，默认值12磅（Excel常规文本大小）
	 */
	short fontHeightInPoints() default 12;

}
