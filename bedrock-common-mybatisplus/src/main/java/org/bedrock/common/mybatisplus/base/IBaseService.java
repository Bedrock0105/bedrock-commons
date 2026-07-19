package org.bedrock.common.mybatisplus.base;

import com.baomidou.mybatisplus.extension.service.IService;

import java.io.Serializable;
import java.util.Collection;

public interface IBaseService<T extends BaseEntity> extends IService<T> {

	/**
	 * 逻辑删除
	 */
	boolean logicRemoveById(Serializable id);

	/**
	 * 批量逻辑删除
	 */
	boolean logicRemoveByIds(Collection<?> list);
}
