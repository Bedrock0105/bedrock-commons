package org.bedrock.common.code.tree;

import org.bedrock.common.code.util.CollectionUtil;

import java.io.Serializable;
import java.util.List;

/**
 * 树形节点标识，同时定义树形结果中节点的基本行为。
 * <p>
 * 请注意，树形结果的实现类必须实现该接口，同时需要添加{@link Comparable#compareTo(Object)}方法，用于排序<p/>
 * 根据自己的实际业务需求，自行删除或实现排序方法
 *
 * @param <T>  节点类型，实现该接口的实现类
 * @param <ID> 节点ID类型
 * @author KVSonen
 */
public interface INode<T extends INode<T, ID>, ID> extends Comparable<T>, Serializable {

	/**
	 * 获取当前节点的ID，此ID值一般推荐使用数据库中的ID。<p/>
	 * 这里的ID值为了通用性，定义为泛型，根据实际业务需求注入自己的ID类型。
	 *
	 * @return 当前节点的ID值
	 */
	ID getId();

	/**
	 * 获取当前节点的子节点集合，节点和子节点根据id和parentId进行关联
	 *
	 * @return 当前节点的子节点集合
	 * @see INode#getId()
	 * @see INode#getParentId()
	 */
	List<T> getChildren();

	/**
	 * 获取当前节点的父节点ID。默认实现返回null，表示没有父节点。
	 *
	 * @return 父节点的ID值，如果没有父节点则返回null。
	 */
	ID getParentId();

	/**
	 * 获取当前节点的排序字段，默认实现返回0
	 */
	default Integer getSort() {
		return 0;
	}

	/**
	 * 比较两个节点的排序字段，用于排序。默认实现返回排序字段的差值。
	 * 没有sort 自动排序，请自行实现排序逻辑
	 */
	@Override
	default int compareTo(T o) {
		Integer currentSort = getSort();
		Integer otherSort = o.getSort();

		// 两者都为null：视为相等
		if (currentSort == null && otherSort == null) {
			return 0;
		}
		// 当前对象sort为null：排在后面（当前对象大）
		if (currentSort == null) {
			return 1;
		}
		// 比较对象sort为null：当前对象排在前面（当前对象小）
		if (otherSort == null) {
			return -1;
		}
		// 两者都不为null：按数值从小到大排序
		return Integer.compare(currentSort, otherSort);
	}

	/**
	 * 判断当前节点是否拥有子节点。该方法主要用于前端展示的判断依据。
	 *
	 * @return 是否拥有子节点，如果当前节点拥有子节点，则返回true，否则返回false。
	 */
	default boolean isHasChildren() {
		return CollectionUtil.isNotEmpty(getChildren());
	}

}
