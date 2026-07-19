package org.bedrock.common.mybatisplus.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.bedrock.common.auth.util.AuthUtil;
import org.bedrock.common.mybatisplus.constant.BedrockDBConstant;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;

public class BaseServiceImpl<M extends BaseMapper<T>, T extends BaseEntity> extends ServiceImpl<M, T> implements IBaseService<T> {

	@Override
	public boolean logicRemoveById(Serializable id) {
		return getBaseMapper().update(null, Wrappers.lambdaUpdate(getEntityClass())
			.set(T::getIsDeleted, BedrockDBConstant.DB_IS_DELETED)
			.set(T::getUpdateTime, LocalDateTime.now())
			.set(T::getUpdateUserId, AuthUtil.getUserId())
			.eq(T::getId, id)) > 0;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean logicRemoveByIds(Collection<?> list) {
		return getBaseMapper().update(null, Wrappers.lambdaUpdate(getEntityClass())
			.set(T::getIsDeleted, BedrockDBConstant.DB_IS_DELETED)
			.set(T::getUpdateTime, LocalDateTime.now())
			.set(T::getUpdateUserId, AuthUtil.getUserId())
			.in(T::getId, list)) > 0;
	}
}
