package org.bedrock.common.tenant.handler;

import org.apache.ibatis.reflection.MetaObject;
import org.bedrock.common.auth.entity.AuthUser;
import org.bedrock.common.auth.util.AuthUtil;
import org.bedrock.common.mybatisplus.handler.DefaultMetaObjectHandler;

import java.time.LocalDateTime;

public class TenantMetaObjectHandler extends DefaultMetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "tenantId", String.class, TenantHandler.getTenantId());
        super.insertFill(metaObject);
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
