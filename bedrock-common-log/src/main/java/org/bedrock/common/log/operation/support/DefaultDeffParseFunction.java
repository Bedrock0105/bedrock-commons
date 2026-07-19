package org.bedrock.common.log.operation.support;

import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.Visit;
import de.danielbechler.diff.selector.ElementSelector;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.log.annotation.DiffLogField;
import org.bedrock.common.log.constant.OperationLogConstant;
import org.bedrock.common.log.operation.convert.AnalysisClassConvert;
import org.bedrock.common.log.operation.convert.AnalysisClassConvertManager;
import org.bedrock.common.code.util.JsonUtil;
import org.bedrock.common.code.util.StringUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

@Slf4j
public class DefaultDeffParseFunction implements IDeffParseFunction {

	@Override
	public String functionName() {
		return "_DIFF";
	}

	@Override
	public String deff(Object oldValue, Object newValue) {
		if (newValue == null || oldValue == null || newValue.getClass() != oldValue.getClass()) {
			return "";
		}
		DiffNode diffNode = ObjectDifferBuilder
			.buildDefault()
			.compare(newValue, oldValue);
		Set<DiffNode> childNodes = new HashSet<>();
		StringBuffer sb = new StringBuffer();
		diffNode.visit(new DiffNode.Visitor() {
			@Override
			public void node(DiffNode node, Visit differenceType) {
				AnalysisClassConvert<?> analysisClassConvert = AnalysisClassConvertManager.getAnalysisClassConvert(node.getValueType());
				if (node.isRootNode() || (node.getValueTypeInfo() != null && analysisClassConvert == null) || childNodes.contains(node)) {
					return;
				}
				DiffLogField fieldAnnotation = node.getFieldAnnotation(DiffLogField.class);
				if (fieldAnnotation == null) {
					childNodes.add(node);
					return;
				}
				boolean valueIsContainer = valueIsContainer(node);
				String fieldName = getFieldName(node);
				String diffLogContent = valueIsContainer ? getCollectionDiffLogContent(fieldName, node, oldValue, newValue, fieldAnnotation.function(), fieldAnnotation.dict()) :
					getDiffLogContent(fieldName, node, oldValue, newValue, fieldAnnotation.function(), fieldAnnotation.dict());
				if (sb.length() > 0) {
					sb.append(" 并且 ");
				}
				sb.append(diffLogContent);
				memorandum(node, childNodes);
			}
		});
		return sb.toString();
	}

	private void memorandum(DiffNode node, Set<DiffNode> set) {
		set.add(node);
		if (node.hasChildren()) {
			Field childrenField = ReflectionUtils.findField(DiffNode.class, "children");
			assert childrenField != null;
			ReflectionUtils.makeAccessible(childrenField);
			Map<ElementSelector, DiffNode> children = (Map<ElementSelector, DiffNode>) ReflectionUtils.getField(childrenField, node);
			assert children != null;
			for (DiffNode value : children.values()) memorandum(value, set);
		}
	}

	@Override
	public String deff(Object newValue) {
		Object variable = LogRecordContext.getVariable(OperationLogConstant.OLD_VALUE_KEY);
		return deff(variable, newValue);
	}

	public String getDiffLogContent(String filedLogName, DiffNode node, Object sourceObject, Object targetObject, String functionName, String dict) {
		AnalysisClassConvert<?> analysisClassConvert = AnalysisClassConvertManager.getAnalysisClassConvert(node.getValueType());
		Set<Annotation> fieldAnnotations = node.getFieldAnnotations();
		switch (node.getState()) {
			case ADDED:
				return "【" + filedLogName + "】从【空】修改为【" + getFunction(getFieldValue(node, targetObject), functionName, analysisClassConvert, fieldAnnotations, dict) + "】";
			case CHANGED:
				return "【" + filedLogName + "】从【" + getFunction(getFieldValue(node, sourceObject), functionName, analysisClassConvert, fieldAnnotations, dict) + "】修改为【" + getFunction(getFieldValue(node, targetObject), functionName, analysisClassConvert, fieldAnnotations, dict) + "】";
			case REMOVED:
				return "删除了【" + filedLogName + "】：【" + getFunction(getFieldValue(node, sourceObject), functionName, analysisClassConvert, fieldAnnotations, dict) + "】";
			default:
				log.warn("diff log not support");
				return "";
		}
	}

	public String getCollectionDiffLogContent(String filedLogName, DiffNode node, Object sourceObject, Object targetObject, String functionName, String dict) {
		//集合走单独的diff模板
		Collection<Object> sourceList = getListValue(node, sourceObject);
		Collection<Object> targetList = getListValue(node, targetObject);
		Collection<Object> addItemList = listSubtract(targetList, sourceList);
		Collection<Object> delItemList = listSubtract(sourceList, targetList);
		String listAddContent = listToContent(functionName, addItemList, dict);
		String listDelContent = listToContent(functionName, delItemList, dict);
		if (StringUtil.isNotBlank(listAddContent) || StringUtil.isNotBlank(listDelContent)) {
			return "【" + filedLogName + "】添加了【" + listAddContent + "】移除了【" + listDelContent + "】";
		} else if (StringUtil.isNotBlank(listAddContent)) {
			return "【" + filedLogName + "】添加了【" + listAddContent + "】";
		} else if (StringUtil.isNotBlank(listDelContent)) {
			return "【" + filedLogName + "】移除了【" + listDelContent + "】";
		}
		return "";
	}

	private String listToContent(String functionName, Collection<Object> addItemList, String dict) {
		StringBuilder listAddContent = new StringBuilder();
		if (!CollectionUtils.isEmpty(addItemList)) {
			for (Object item : addItemList) {
				listAddContent.append(getFunction(item, functionName, null, null, dict)).append(", ");
			}
		}
		return listAddContent.toString().replaceAll(", " + "$", "");
	}

	private boolean valueIsContainer(DiffNode node) {
		if (Collection.class.isAssignableFrom(node.getValueType()) || node.getValueType().isArray()) {
			return true;
		}
		return false;
	}

	private Collection<Object> getListValue(DiffNode node, Object object) {
		Object fieldSourceValue = getFieldValue(node, object);
		//noinspection unchecked
		if (fieldSourceValue != null && fieldSourceValue.getClass().isArray()) {
			return new ArrayList<>(Arrays.asList((Object[]) fieldSourceValue));
		}
		return fieldSourceValue == null ? new ArrayList<>() : (Collection<Object>) fieldSourceValue;
	}

	private Collection<Object> listSubtract(Collection<Object> minuend, Collection<Object> subTractor) {
		Collection<Object> addItemList = new ArrayList<>(minuend);
		addItemList.removeAll(subTractor);
		return addItemList;
	}

	private String getFieldName(DiffNode node) {

		DiffLogField fieldAnnotation = node.getFieldAnnotation(DiffLogField.class);
		if (node.getParentNode().isRootNode()) {
			if (fieldAnnotation != null) {
				return fieldAnnotation.name();
			}
			return node.getPropertyName();
		}
		StringBuffer sb = new StringBuffer();
		getFieldName(node.getParentNode(), sb);
		sb.append(fieldAnnotation.name());
		return sb.toString();
	}

	private void getFieldName(DiffNode node, StringBuffer sb) {
		DiffLogField fieldAnnotation = node.getFieldAnnotation(DiffLogField.class);
		if (node.getParentNode().isRootNode()) {
			sb.append(fieldAnnotation.name()).append(".");
			return;
		}
		getFieldName(node.getParentNode(), sb);
	}

	/**
	 * 获取属性值
	 *
	 * @param node
	 * @param obj
	 * @return
	 */
	private Object getFieldValue(DiffNode node, Object obj) {
		if (obj == null) {
			return null;
		}
		return node.canonicalGet(obj);
	}

	/**
	 * 指向函数
	 *
	 * @param obj
	 * @param functionName
	 * @return
	 */
	private String getFunction(Object obj, String functionName,
							   AnalysisClassConvert<?> analysisClassConvert,
							   Set<Annotation> fieldAnnotations,
							   String dict) {
		if (obj == null) {
			return "";
		}
		IParseFunction parseFunctionMap = OperationLogCodeSupport.getParseFunctionMap(functionName);
		if (parseFunctionMap != null) {
			return parseFunctionMap.apply(obj);
		}
		if (analysisClassConvert != null) {
			return analysisClassConvert.convert(obj, fieldAnnotations);
		}
		if (StringUtil.isNotBlank(dict)) {
			try {
				Map<String, String> map = JsonUtil.parseMap(dict, String.class, String.class);
				return map.get(obj.toString());
			} catch (Exception e) {
				log.warn("字典转换失败", e);
			}
		}
		return obj.toString();
	}

}

