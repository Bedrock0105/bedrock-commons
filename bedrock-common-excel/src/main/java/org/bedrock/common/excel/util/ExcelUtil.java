package org.bedrock.common.excel.util;

import cn.idev.excel.FastExcel;
import cn.idev.excel.read.listener.ReadListener;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.bedrock.common.excel.handler.ExcelCustomizeWriteHandler;
import org.bedrock.common.excel.listener.ExcelListener;
import org.bedrock.common.excel.model.ExcelDownOption;
import org.bedrock.common.excel.model.OptionsSheet;
import org.bedrock.common.log.exception.ServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * excel 工具类
 */
@Slf4j
public abstract class ExcelUtil {

	public static final ExcelCustomizeWriteHandler EXCEL_CUSTOMIZE_WRITE_HANDLER = new ExcelCustomizeWriteHandler();

	/**
	 * 读取 excel
	 *
	 * @param is          excel输入流
	 * @param entityClass 读取的实体类
	 * @param <T>         实体类类型
	 * @return 读取的实体类列表
	 */
	public static <T> List<T> readExcel(InputStream is, Class<T> entityClass) {
		ExcelListener<T> listener = new ExcelListener<>();
		FastExcel.read(is, entityClass, listener).sheet().doRead();
		return listener.getData();
	}

	/**
	 * 读取 excel
	 *
	 * @param is          excel输入流
	 * @param entityClass 读取的实体类
	 * @param skipRowNum  跳过的行数 例如表头下面有示例的数据
	 * @param <T>         实体类类型
	 * @return 读取的实体类列表
	 */
	public static <T> List<T> readExcel(InputStream is, Class<T> entityClass, int skipRowNum) {
		ExcelListener<T> listener = new ExcelListener<>();
		listener.setSkipRowNum(skipRowNum);
		FastExcel.read(is, entityClass, listener).sheet().doRead();
		return listener.getData();
	}

	/**
	 * 读取 excel
	 *
	 * @param is            excel输入流
	 * @param entityClass   读取的实体类
	 * @param skipRowNum    跳过的行数 例如表头下面有示例的数据 默认0 就可以
	 * @param sheetNo       表单编号(0开始)
	 * @param headRowNumber 表头行数
	 * @param <T>           实体类类型
	 * @return 读取的实体类列表
	 */
	public static <T> List<T> readExcel(InputStream is, Class<T> entityClass, int sheetNo, int headRowNumber, int skipRowNum) {
		ExcelListener<T> listener = new ExcelListener<>();
		listener.setSkipRowNum(skipRowNum);
		FastExcel.read(is, entityClass, listener)
			.sheet(sheetNo)
			.headRowNumber(headRowNumber)
			.doRead();
		return listener.getData();
	}

	/**
	 * 读取 excel
	 *
	 * @param is            excel输入流
	 * @param entityClass   读取的实体类
	 * @param listener      监听器
	 * @param sheetNo       表单编号(0开始)
	 * @param headRowNumber 表头行数
	 * @param <T>           实体类类型
	 * @return 读取的实体类列表
	 */
	public static <T> void readExcel(InputStream is, Class<T> entityClass, ReadListener<T> listener, int sheetNo, int headRowNumber) {
		FastExcel.read(is, entityClass, listener)
			.sheet(sheetNo)
			.headRowNumber(headRowNumber)
			.doRead();
	}

	/**
	 * 下载 excel
	 *
	 * @param response    响应
	 * @param fileName    文件名
	 * @param sheetName   表单名称
	 * @param data        数据
	 * @param entityClass 实体类
	 * @param <T>
	 */
	public static <T> void exportExcel(HttpServletResponse response, String fileName, String sheetName, List<T> data, Class<T> entityClass) {
		try {
			// 设置响应头
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
			response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
			exportExcel(response.getOutputStream(), sheetName, data, entityClass);
		} catch (IOException e) {
			throw new ServiceException(e.getMessage(), e);
		}
	}

	/**
	 * 写入 excel
	 *
	 * @param outputStream 输出流
	 * @param sheetName    表单名称
	 * @param data         数据
	 * @param entityClass  实体类
	 * @param <T>
	 */
	public static <T> void exportExcel(OutputStream outputStream, String sheetName, List<T> data, Class<T> entityClass) {
		FastExcel
			.write(outputStream, entityClass)
			.registerWriteHandler(ExcelUtil.EXCEL_CUSTOMIZE_WRITE_HANDLER)
			.sheet(sheetName)
			.doWrite(data);
	}

	/**
	 * 创建数据有效性对象约束
	 *
	 * @param workbook    工作簿
	 * @param targetSheet 目标工作表
	 * @param options     选项
	 */
	public static void addValidationToSheet(Workbook workbook,
											Sheet targetSheet,
											List<ExcelDownOption> options) {
		if (options.isEmpty()) {
			return;
		}
		Set<Integer> optionSet = new HashSet<>();
		/**
		 * 隐藏的选项表
		 * 用来存储数据
		 */
		OptionsSheet optionsSheet = null;
		for (ExcelDownOption option : options) {
			if (option.isEmpty()) {
				continue;
			}
			if (option.getCurrentColumnIndex() == null) {
				log.info("当前字段[{}]没有配置列索引", option.getCurrentField());
				continue;
			}
			/**
			 * 每个索引只能添加一次
			 */
			if (optionSet.contains(option.getCurrentColumnIndex())) {
				continue;
			}
			optionSet.add(option.getCurrentColumnIndex());
			/**
			 * 判断是否是根级
			 */
			DataValidationHelper helper = targetSheet.getDataValidationHelper();
			DataValidationConstraint constraint = null;
			if (option.isRootLevel()) {
				/**
				 * 选项列表小于25个的时候，使用显式列表
				 */
				List<String> list = option.getOptionMap().get(ExcelDownOption.DEFAULT_PARENT_FIELD);
				if (list.size() <= 25) {
					constraint = helper.createExplicitListConstraint(list.toArray(new String[0]));
				}
			} else {
				if (option.getParentColumnIndex() == null) {
					log.info("当前字段[{}]没有配置父字段索引", option.getCurrentField());
					continue;
				}
			}
			/**
			 * 如果没有使用显示列表
			 * 则使用隐藏的选项表
			 */
			if (constraint == null) {
				if (optionsSheet == null) {
					optionsSheet = new OptionsSheet(createHiddenSheet(workbook));
				}
				constraint = createDataValidationConstraint(workbook, targetSheet, option, optionsSheet);
			}
			addDataValidation(targetSheet, helper.createValidation(constraint, createCellRangeAddressList(option)), option);
		}

	}

	/**
	 * 创建隐藏的选项表
	 */
	public static Sheet createHiddenSheet(Workbook workbook) {
		String hiddenSheetName = "hiddenSheet" + workbook.getNumberOfSheets();
		Sheet optionsSheet = workbook.createSheet(hiddenSheetName);
		workbook.setSheetHidden(workbook.getSheetIndex(optionsSheet), true);
		return optionsSheet;
	}

	/**
	 * 创建数据有效性对象约束
	 */
	public static CellRangeAddressList createCellRangeAddressList(ExcelDownOption option) {
		return new CellRangeAddressList(option.getDropdownStartRow(), option.getDropdownEndRow(), option.getCurrentColumnIndex(), option.getCurrentColumnIndex());
	}

	/**
	 * 设置数据有效性对象
	 */
	public static void addDataValidation(Sheet targetSheet, DataValidation validation, ExcelDownOption option) {
		// 配置错误提示
		validation.setErrorStyle(option.getErrorStyle());
		validation.setShowErrorBox(option.isShowErrorBox());
		validation.createErrorBox(option.getErrorTitle(), option.getErrorMessage());
		// 输入提示
		validation.setShowPromptBox(option.isShowPromptBox());
		validation.createPromptBox(option.getPromptTitle(), option.getPromptMessage());
		targetSheet.addValidationData(validation);
	}

	/**
	 * 创建数据有效性约束条件
	 *
	 * @param workbook     工作簿
	 * @param targetSheet  目标工作表
	 * @param option       选项
	 * @param optionsSheet 隐藏的选项表
	 * @return
	 */
	public static DataValidationConstraint createDataValidationConstraint(Workbook workbook,
																		  Sheet targetSheet,
																		  ExcelDownOption option,
																		  OptionsSheet optionsSheet) {
		DataValidationHelper dataValidationHelper = targetSheet.getDataValidationHelper();
		int rowIndexStart = optionsSheet.rowIndex();
		String nameName = option.getCurrentField() + "_" + option.getCurrentColumnIndex() + "_parent";
		/**
		 * 判断是否是顶级
		 */
		if (option.isRootLevel()) {
			/**
			 *  顶级 map里面只有 DEFAULT 的key
			 */
			List<String> list = option.getOptionMap().get(ExcelDownOption.DEFAULT_PARENT_FIELD);
			for (String value : list) {
				/**
				 * 设置选项表内容
				 */
				optionsSheet.setCellValue(rowIndexStart++, value);
			}
			createName(workbook, nameName, optionsSheet.formula(list.size()));
			optionsSheet.addColumnIndex();
			return dataValidationHelper.createFormulaListConstraint(nameName);
		}
		Map<String, List<String>> optionMap = option.getOptionMap();
		for (Map.Entry<String, List<String>> entry : optionMap.entrySet()) {
			List<String> list = entry.getValue();
			String parent = formatNameName(option.getCurrentField() + "_" + entry.getKey());
			/**
			 * 开始写入下拉数据
			 */
			for (String value : list) {
				/**
				 * 设置选项表内容
				 */
				optionsSheet.setCellValue(rowIndexStart++, value);
			}
			createName(workbook, parent, optionsSheet.formula(list.size()));
			optionsSheet.addColumnIndex();
			rowIndexStart = optionsSheet.rowIndex();
		}
		return dataValidationHelper.createFormulaListConstraint(String.format("INDIRECT(\"%s_\"&$%s%d)", option.getCurrentField(), OptionsSheet.coverColumnIndex(option.getParentColumnIndex() + 1), option.getDropdownStartRow() + 1));
	}

	/**
	 * 不可数字开头
	 *
	 * @param name
	 * @return
	 */
	private static String formatNameName(String name) {
		name = name.replaceAll(" ", "").replaceAll("-", "_").replaceAll(":", ".");
		if (Character.isDigit(name.charAt(0))) {
			name = "_" + name;
		}
		return name;
	}

	/**
	 * 创建数据有效性对象名称
	 */
	private static Name createName(Workbook workbook, String nameName, String formula) {
		Name name = workbook.createName();
		name.setNameName(nameName);
		name.setRefersToFormula(formula);
		return name;
	}
}
