package org.bedrock.common.mybatisplus.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.bedrock.common.auth.entity.AuthUser;
import org.bedrock.common.auth.util.AuthUtil;
import org.bedrock.common.mybatisplus.constant.BedrockDBConstant;

import java.time.LocalDateTime;

public class DefaultMetaObjectHandler implements MetaObjectHandler {

	@Override
	public void insertFill(MetaObject metaObject) {
		AuthUser authUser = AuthUtil.getAuthUser();
		this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
		this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
		this.strictInsertFill(metaObject, "isDeleted", Integer.class, BedrockDBConstant.DB_NOT_DELETED);
		if (authUser != null) {
			this.strictInsertFill(metaObject, "createUserId", Long.class, authUser.getUserId());
			this.strictInsertFill(metaObject, "updateUserId", Long.class, authUser.getUserId());
		}

	}

	@Override
	public void updateFill(MetaObject metaObject) {
		AuthUser authUser = AuthUtil.getAuthUser();
		this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
		if (authUser != null) {
			this.strictUpdateFill(metaObject, "updateUserId", Long.class, authUser.getUserId());
		}
	}
}
