package org.bedrock.common.tenant.handler;

import org.bedrock.common.auth.util.AuthUtil;
import org.bedrock.common.code.util.ThreadLocalUtil;

import java.util.function.Supplier;

public class TenantHandler {

	public static final String KEY = "TokenHandlerIsIgnore";

	public static final String KEY_TENANT_ID = "TokenHandlerTenantId";

	/**
	 * 是否忽略租户
	 */
	public static boolean isIgnore() {
		return Boolean.TRUE.equals(ThreadLocalUtil.get(KEY));
	}

	/**
	 * 设置开启租户
	 */
	public static void setIgnore(boolean ignore) {
		ThreadLocalUtil.set(KEY, ignore);
	}

	/**
	 * 获取租户ID
	 */
	public static String getTenantId() {
		Object o = ThreadLocalUtil.get(KEY_TENANT_ID);
		if (o != null) {
			return o.toString();
		}
		return AuthUtil.getTenantId();
	}

	/**
	 * 设置租户ID
	 */
	public static void setTenantId(String tenantId) {
		ThreadLocalUtil.set(KEY_TENANT_ID, tenantId);
	}

	/**
	 * 忽略租户
	 * 并且执行一个没有返回值的函数
	 */
	public static void ignore(Runnable runnable) {
		boolean ignore = isIgnore();
		setIgnore(true);
		try {
			runnable.run();
		} finally {
			setIgnore(ignore);
		}
	}

	/**
	 * 忽略租户
	 * 并且执行一个有返回值的函数
	 */
	public static <T> T ignore(Supplier<T> supplier) {
		boolean ignore = isIgnore();
		setIgnore(true);
		try {
			return supplier.get();
		} finally {
			setIgnore(ignore);
		}
	}

	/**
	 * 设置租户
	 * 并且执行一个没有返回值的函数
	 */
	public static void tenant(String tenant, Runnable runnable) {
		String tenantId = getTenantId();
		setTenantId(tenant);
		try {
			runnable.run();
		} finally {
			setTenantId(tenantId);
		}
	}

	/**
	 * 忽略租户
	 * 并且执行一个有返回值的函数
	 */
	public static <T> T tenant(String tenant, Supplier<T> supplier) {
		String tenantId = getTenantId();
		setTenantId(tenant);
		try {
			return supplier.get();
		} finally {
			setTenantId(tenantId);
		}
	}

	static {
		ThreadLocalUtil.addIgnoredThreadLocalKeyForTransfer(KEY);
		ThreadLocalUtil.addIgnoredThreadLocalKeyForTransfer(KEY_TENANT_ID);
	}
}
