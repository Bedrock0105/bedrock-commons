package org.bedrock.common.excel.handler;

import cn.idev.excel.metadata.Head;
import cn.idev.excel.metadata.data.RichTextStringData;
import cn.idev.excel.util.StyleUtil;
import cn.idev.excel.write.handler.CellWriteHandler;
import cn.idev.excel.write.handler.SheetWriteHandler;
import cn.idev.excel.write.metadata.holder.WriteSheetHolder;
import cn.idev.excel.write.metadata.holder.WriteTableHolder;
import cn.idev.excel.write.metadata.holder.WriteWorkbookHolder;
import cn.idev.excel.write.metadata.style.WriteFont;
import cn.idev.excel.write.property.ExcelWriteHeadProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.bedrock.common.code.util.ObjectUtil;
import org.bedrock.common.code.util.SpELParserUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.excel.annotation.ExcelContentDown;
import org.bedrock.common.excel.annotation.ExcelHeaderComment;
import org.bedrock.common.excel.annotation.ExcelHeaderDesc;
import org.bedrock.common.excel.model.ExcelDownOption;
import org.bedrock.common.excel.support.WriteFontParser;
import org.bedrock.common.excel.util.ExcelUtil;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 自定义Excel写入处理器
 */
@Slf4j
public class ExcelCustomizeWriteHandler implements SheetWriteHandler, CellWriteHandler {

	@Override
	public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
		/**
		 * 处理ExcelHeaderDesc 注解
		 */
		processExcelHeaderDesc(writeWorkbookHolder, writeSheetHolder);
		/**
		 * 处理ExcelContentDown 注解
		 */
		processExcelContentDown(writeWorkbookHolder, writeSheetHolder);
	}

	/**
	 * 处理ExcelHeaderDesc 注解
	 */
	public void processExcelHeaderDesc(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
		Workbook workbook = writeWorkbookHolder.getWorkbook();
		int startDoWrite = writeSheetHolder.getNewRowIndexAndStartDoWrite();
		Sheet sheet = writeSheetHolder.getSheet();
		/**
		 * 获取目标类
		 * 上面的注解
		 */
		ExcelHeaderDesc targetClass = getTargetClass(writeSheetHolder);
		if (targetClass == null) {
			return;
		}

		int headSize = getHeadSize(writeSheetHolder);
		if (headSize <= 0) return;

		Row descRow = sheet.createRow(startDoWrite);
		descRow.setHeightInPoints(targetClass.rowHeight()); // 设置行高

		Cell descCell = descRow.createCell(0);
		if (headSize > 1) {
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headSize - 1));
		}
		/**
		 * 描述行内容
		 */
		WriteFont writeFont = new WriteFont();
		writeFont.setBold(targetClass.bold());
		writeFont.setFontName(targetClass.fontName());
		writeFont.setFontHeightInPoints(targetClass.fontHeightInPoints());

		RichTextStringData richTextStringData = WriteFontParser.parse(targetClass.value());
		richTextStringData.applyFont(writeFont);

		descCell.setCellValue(StyleUtil.buildRichTextString(writeSheetHolder.getParentWriteWorkbookHolder(), richTextStringData));
		// 设置样式
		CellStyle descStyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(targetClass.bold());
		font.setFontName(targetClass.fontName());
		font.setFontHeightInPoints(targetClass.fontHeightInPoints());
		descStyle.setFont(font);
		descStyle.setAlignment(HorizontalAlignment.LEFT);
		descStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		// 背景色：浅灰色
//		descStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
//		descStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		// 自动换行（支持 \n 换行）
		descStyle.setWrapText(true);
		descStyle.setBorderTop(BorderStyle.THIN);
		descStyle.setBorderLeft(BorderStyle.THIN);
		descStyle.setBorderRight(BorderStyle.THIN);
		descStyle.setBorderBottom(BorderStyle.THIN);
		descCell.setCellStyle(descStyle);
	}

	public int getHeadSize(WriteSheetHolder writeSheetHolder) {
		return writeSheetHolder.excelWriteHeadProperty().getHeadMap().size();
	}

	public ExcelHeaderDesc getTargetClass(WriteSheetHolder writeSheetHolder) {
		ExcelWriteHeadProperty excelWriteHeadProperty = writeSheetHolder.excelWriteHeadProperty();
		Class<?> clazz = excelWriteHeadProperty.getHeadClazz();
		return clazz != null && clazz.isAnnotationPresent(ExcelHeaderDesc.class) ? clazz.getAnnotation(ExcelHeaderDesc.class) : null;
	}

	/**
	 * 处理ExcelHeaderDesc 注解
	 */
	public void processExcelContentDown(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
		ExcelWriteHeadProperty excelWriteHeadProperty = writeSheetHolder.excelWriteHeadProperty();
		List<ExcelDownOption> options = new java.util.ArrayList<>();
		Map<String, Integer> indexMap = new HashMap<>();
		excelWriteHeadProperty.getHeadMap().forEach((index, head) -> {
			if (StringUtil.isNotBlank(head.getFieldName())) {
				indexMap.put(head.getFieldName(), index);
			}
			Field field = head.getField();
			if (field == null || !field.isAnnotationPresent(ExcelContentDown.class)) {
				return;
			}
			ExcelContentDown contentDown = field.getAnnotation(ExcelContentDown.class);
			/**
			 * 获取ExcelDownOption
			 */
			getExcelDownOption(contentDown, head, options);
		});
		if (options.isEmpty()) {
			return;
		}
		/**
		 * 开始添加下拉框
		 */
		for (ExcelDownOption option : options) {
			if (option.getCurrentColumnIndex() == null) {
				option.setCurrentColumnIndex(indexMap.get(option.getCurrentField()));
			}
			if (!option.isRootLevel() && option.getParentColumnIndex() == null) {
				option.setParentColumnIndex(indexMap.get(option.getParentField()));
			}
		}
		ExcelUtil.addValidationToSheet(writeWorkbookHolder.getWorkbook(), writeSheetHolder.getSheet(), options);
	}

	/**
	 * 获取ExcelDownOption
	 */
	public void getExcelDownOption(ExcelContentDown contentDown, Head head, List<ExcelDownOption> options) {
		String[] down = contentDown.dataDown();
		/**
		 * 优先静态数据
		 */
		if (ObjectUtil.isNotEmpty(down)) {
			ExcelDownOption excelDownOption = new ExcelDownOption(down);
			excelDownOption.setCurrentField(head.getFieldName());
			buildExcelDownOption(contentDown, excelDownOption);
			options.add(excelDownOption);
			return;
		}
		if (ObjectUtil.isEmpty(contentDown.value())) {
			return;
		}
		/**
		 * 执行spel脚本
		 * 并且判断结果是否非空
		 */
		Object parse = SpELParserUtil.parse(contentDown.value(), Map.of(), null);
		if (ObjectUtil.isEmpty(parse)) {
			return;
		}
		/**
		 * 判断是否是ExcelDownOption
		 */
		if (parse instanceof ExcelDownOption excelDownOption) {
			excelDownOption.setCurrentField(head.getFieldName());
			buildExcelDownOption(contentDown, excelDownOption);
			options.add(excelDownOption);
			return;
		}
		/**
		 * 判断是否是集合
		 * 并且集合的泛型是ExcelDownOption
		 */
		if (parse instanceof List<?> list) {
			Object o = list.get(0);
			if (o instanceof ExcelDownOption) {
				options.addAll((Collection<? extends ExcelDownOption>) list);
				return;
			}
		}
		/**
		 * 默认处理
		 */
		ExcelDownOption excelDownOption = new ExcelDownOption(parse);
		excelDownOption.setCurrentField(head.getFieldName());
		buildExcelDownOption(contentDown, excelDownOption);
		options.add(excelDownOption);
	}

	/**
	 * 构建ExcelDownOption
	 */
	public void buildExcelDownOption(ExcelContentDown contentDown, ExcelDownOption excelDownOption) {
		excelDownOption.setDropdownStartRow(contentDown.dropdownStartRow());
		excelDownOption.setDropdownEndRow(contentDown.dropdownEndRow());
		excelDownOption.setErrorStyle(contentDown.errorStyle());
		excelDownOption.setErrorTitle(contentDown.errorTitle());
		excelDownOption.setErrorMessage(contentDown.errorMessage());
		excelDownOption.setShowErrorBox(contentDown.showErrorBox());
		excelDownOption.setPromptTitle(contentDown.promptTitle());
		excelDownOption.setPromptMessage(contentDown.promptMessage());
		excelDownOption.setShowPromptBox(contentDown.showPromptBox());
	}

	/**
	 * 处理表头描述
	 */
	@Override
	public void afterCellCreate(WriteSheetHolder writeSheetHolder,
								WriteTableHolder writeTableHolder,
								Cell cell,
								Head head,
								Integer relativeRowIndex,
								Boolean isHead) {
		if (!isHead || head == null) {
			return;
		}
		processExcelHeaderComment(head, writeSheetHolder, cell);

	}

	public void processExcelHeaderComment(Head head, WriteSheetHolder writeSheetHolder, Cell cell) {
		// 只处理表头行（isHead=true）
		try {
			// 获取当前字段的 @ExcelHeaderComment 注解
			Field field = head.getField();
			if (field == null || !field.isAnnotationPresent(ExcelHeaderComment.class)) {
				return;
			}
			ExcelHeaderComment commentAnnotation = field.getAnnotation(ExcelHeaderComment.class);
			Sheet sheet = writeSheetHolder.getSheet();
			Drawing<?> drawing = sheet.createDrawingPatriarch();
			ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, cell.getColumnIndex(), cell.getRowIndex(), cell.getColumnIndex() + 1, cell.getRowIndex() + commentAnnotation.rowHeight());
			Comment comment = drawing.createCellComment(anchor);
			RichTextString commentText = StyleUtil.buildRichTextString(writeSheetHolder.getParentWriteWorkbookHolder(), new RichTextStringData(commentAnnotation.value()));
			comment.setString(commentText);
			comment.setAuthor(commentAnnotation.author());
			cell.setCellComment(comment);
		} catch (Exception e) {
			log.error("表头注释处理异常：{}", e.getMessage(), e);
		}
	}
}
