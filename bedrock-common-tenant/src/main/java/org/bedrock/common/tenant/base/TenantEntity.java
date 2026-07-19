package org.bedrock.common.tenant.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bedrock.common.mybatisplus.base.BaseEntity;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class TenantEntity extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "租户ID")
	@TableField(value = "tenant_id", fill = FieldFill.INSERT)
	private String tenantId;

}
