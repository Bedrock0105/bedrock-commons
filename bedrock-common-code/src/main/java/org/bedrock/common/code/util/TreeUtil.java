package org.bedrock.common.code.util;

import org.bedrock.common.code.tree.INode;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TreeUtils类提供了树形结构的构建方法，当一个列表数据需要转换成树形结构时，可以使用该类提供的方法。
 * <p>
 * 但列表类型必须实现接口{@link INode}，在接口中定义了父子结构的关联属性，此处构建则是依据该关联属性进行构建。
 *
 * @see INode
 */

public abstract class TreeUtil {

	/**
	 * 根据给定的列表构建树形结构，列表不能为空
	 *
	 * @param list 所有节点的集合，节点必须实现接口{@link INode}
	 * @param <T>  节点类型，接口{@link INode}的实现类
	 * @return 树形结构，在返回列表中，根节点为所有parentId为null的节点
	 * @see INode
	 */
	public static <T extends INode<T, Long>> List<T> buildTree(@Nullable List<T> list) {
		return buildTree(list, 0L);
	}

	/**
	 * 根据给定的列表构建树形结构，列表不能为空
	 *
	 * @param list 所有节点的集合，节点必须实现接口{@link INode}
	 * @param <T>  节点类型，接口{@link INode}的实现类
	 * @return 树形结构，在返回列表中，根节点为所有parentId为null的节点
	 * @see INode
	 */
	public static <T extends INode<T, ID>, ID> List<T> buildTree(@Nullable List<T> list, @NonNull ID topId) {
		if (CollectionUtil.isEmpty(list)) {
			return list;
		}
		// 1. 构建ID到节点的映射（O(n)）
		Map<ID, T> nodeMap = list.stream()
			.collect(Collectors.toMap(T::getId, node -> node,
				(oldNode, newNode) -> newNode));
		Set<ID> parentIds = new HashSet<>();
		// 2. 遍历所有节点，给父节点添加子节点（O(n)）
		for (T node : list) {
			ID parentId = node.getParentId();
			// 根节点：parentId为null或等于topId
			if (parentId == null || parentId.equals(topId)) {
				continue; // 根节点后续统一收集
			}
			// 找到父节点，添加到其子节点列表
			T parentNode = nodeMap.get(parentId);
			if (parentNode != null) {
				parentNode.getChildren().add(node);
			} else {
				//父节点不存在，说明该节点的父节点不存在，则该节点为顶级节点
				parentIds.add(node.getId());
			}
		}
		// 3. 收集所有根节点并排序
		return list.stream()
			.filter(node -> node.getParentId() == null || node.getParentId().equals(topId) || parentIds.contains(node.getId()))
			.sorted()
			.peek(node -> {
				if (CollectionUtil.isNotEmpty(node.getChildren())) {
					Collections.sort(node.getChildren());
				}
			})
			.collect(Collectors.toList());
	}
}
