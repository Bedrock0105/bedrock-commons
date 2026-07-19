package org.bedrock.common.datascope.handler;

import org.bedrock.common.datascope.annotation.DataScopeFilter;
import org.bedrock.common.datascope.model.DataScopeModel;
import org.bedrock.common.code.util.ThreadLocalUtil;

/**
 * 这里面的方法只针对 {@link org.bedrock.common.datascope.plugins.DruidDataScopeAuthInterceptor} 生效
 */
public class DataScopeAuthHolder {

	/**
	 * 是否开启数据权限
	 */
	public static final String IS_AUTH = "DataScopeAuthHolderIsAuth";

	/**
	 * 数据权限模型
	 */
	public static final String DATA_SCOPE_MODEL = "DataScopeAuthHolderDataScopeModel";

	/**
	 * 是否开启数据权限
	 *
	 * @return
	 */
	public static boolean isAuth() {
		return Boolean.TRUE.equals(ThreadLocalUtil.get(IS_AUTH));
	}

	/**
	 * 设置代码数据权限
	 *
	 * @param isAuth
	 */
	public static void setIsAuth(boolean isAuth) {
		ThreadLocalUtil.set(IS_AUTH, isAuth);
	}

	/**
	 * 设置数据权限模型
	 */
	public static void setDataScopeModel(DataScopeModel dataScopeModel) {
		if (dataScopeModel == null) {
			ThreadLocalUtil.remove(DATA_SCOPE_MODEL);
			return;
		}
		ThreadLocalUtil.set(DATA_SCOPE_MODEL, dataScopeModel);
	}

	/**
	 * 设置数据权限模型
	 */
	public static void setDataScopeModel(String mapperId, DataScopeFilter dataScopeFilter) {
		setDataScopeModel(createDataScopeModel(mapperId, dataScopeFilter));
	}

	/**
	 * 获取数据权限模型
	 *
	 * @return
	 */
	public static DataScopeModel getDataScopeModel() {
		return ThreadLocalUtil.get(DATA_SCOPE_MODEL);
	}

	/**
	 * 创建数据权限模型
	 *
	 * @param mapperId
	 * @return
	 */
	public static DataScopeModel createDataScopeModel(String mapperId, DataScopeFilter dataScopeFilter) {
		return new DataScopeModel(mapperId, dataScopeFilter);
	}

	static {
		ThreadLocalUtil.addIgnoredThreadLocalKeyForTransfer(IS_AUTH);
		ThreadLocalUtil.addIgnoredThreadLocalKeyForTransfer(DATA_SCOPE_MODEL);
	}
}
