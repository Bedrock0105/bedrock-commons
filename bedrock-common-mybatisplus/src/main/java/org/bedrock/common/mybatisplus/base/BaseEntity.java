package org.bedrock.common.mybatisplus.base;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.bedrock.common.code.util.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "基础实体")
public class BaseEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.ASSIGN_ID)
	@JsonSerialize(using = ToStringSerializer.class)
	@Schema(description = "主键")
	private Long id;

	@Schema(description = "创建时间")
	@JsonFormat(pattern = DateUtil.PATTERN_DATETIME)
	@DateTimeFormat(pattern = DateUtil.PATTERN_DATETIME)
	@TableField(value = "create_time", fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@Schema(description = "更新时间")
	@JsonFormat(pattern = DateUtil.PATTERN_DATETIME)
	@DateTimeFormat(pattern = DateUtil.PATTERN_DATETIME)
	@TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;

	@Schema(description = "创建人")
	@JsonSerialize(using = ToStringSerializer.class)
	@TableField(value = "create_user_id", fill = FieldFill.INSERT)
	private Long createUserId;

	@Schema(description = "更新人")
	@TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
	@JsonSerialize(using = ToStringSerializer.class)
	private Long updateUserId;

	@Schema(description = "删除状态 0 是未删除，1 是已经删除")
	@TableLogic
	@TableField(value = "is_deleted", fill = FieldFill.INSERT)
	private Integer isDeleted;

}
