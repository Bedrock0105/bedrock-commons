package org.bedrock.common.code.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bedrock.common.code.support.CommonResultCode;
import org.bedrock.common.code.support.ResultCode;

import java.io.Serial;

@Getter
@Setter
@Schema(description = "返回信息")
@ToString
@NoArgsConstructor
public class R<T> implements java.io.Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "状态码", requiredMode = Schema.RequiredMode.REQUIRED)
	private int code;

	@Schema(description = "返回信息", requiredMode = Schema.RequiredMode.REQUIRED)
	private String msg;

	@Schema(description = "返回数据", requiredMode = Schema.RequiredMode.AUTO)
	private T data;

	@Schema(description = "时间戳", requiredMode = Schema.RequiredMode.REQUIRED)
	private long timestamp;

	private R(int code, String msg, T data) {
		this.code = code;
		this.msg = msg;
		this.data = data;
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * 判断是否成功
	 */
	public boolean isSuccess() {
		return code == CommonResultCode.SUCCESS.getCode();
	}

	public static <T> R<T> status(boolean status) {
		return status ? R.success() : R.failure();
	}

	/**
	 * 操作成功
	 *
	 * @param <T>
	 * @return
	 */
	public static <T> R<T> success() {
		return success(null);
	}

	/**
	 * 操作成功 有返回数据
	 */
	public static <T> R<T> success(T data) {
		return success(CommonResultCode.SUCCESS.getMessage(), data);
	}

	public static <T> R<T> success(String msg, T data) {
		return new R<>(HttpServletResponse.SC_OK, msg, data);
	}

	/**
	 * 操作成功 有返回数据
	 */
	public static <T> R<T> ok(T data) {
		return ok(CommonResultCode.SUCCESS.getMessage(), data);
	}

	public static <T> R<T> ok(String msg, T data) {
		return new R<>(CommonResultCode.SUCCESS.getCode(), msg, data);
	}

	/**
	 * 操作失败
	 */
	public static <T> R<T> failure() {
		return failure(CommonResultCode.FAILURE.getMessage());
	}

	/**
	 * 操作失败
	 */
	public static <T> R<T> failure(int code) {
		return failure(code, CommonResultCode.FAILURE.getMessage());
	}

	/**
	 * 操作失败
	 */
	public static <T> R<T> failure(String msg) {
		return failure(CommonResultCode.FAILURE.getCode(), msg);
	}

	/**
	 * 操作失败
	 */
	public static <T> R<T> failure(int code, String msg) {
		return new R<>(code, msg, null);
	}

	/**
	 * 操作失败
	 */
	public static <T> R<T> failure(ResultCode resultCode, Object... args) {
		return new R<>(resultCode.getCode(), resultCode.formatMessage(args), null);
	}

	/**
	 * 发生错误
	 */
	public static <T> R<T> error() {
		return error(CommonResultCode.ERROR.getMessage());
	}

	/**
	 * 发生错误
	 */
	public static <T> R<T> error(int code) {
		return error(code, CommonResultCode.ERROR.getMessage());
	}

	/**
	 * 发生错误
	 */
	public static <T> R<T> error(String msg) {
		return error(CommonResultCode.ERROR.getCode(), msg);
	}

	/**
	 * 发生错误
	 */
	public static <T> R<T> error(int code, String msg) {
		return new R<>(code, msg, null);
	}

	/**
	 * 发生错误
	 */
	public static <T> R<T> error(ResultCode resultCode, Object... args) {
		return new R<>(resultCode.getCode(), resultCode.formatMessage(args), null);
	}
}
