package org.bedrock.common.authentication.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bedrock.common.authentication.exception.ClientNotFoundOauth2Exception;
import org.bedrock.common.authentication.model.ClientDetailsModel;
import org.bedrock.common.authentication.service.ClientDetailsService;
import org.bedrock.common.authentication.support.ClientDetails;
import org.bedrock.common.code.util.JsonUtil;
import org.bedrock.common.code.util.StringUtil;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class ClientDetailsServiceImpl implements ClientDetailsService {

	private final ClientDetailsRowMapper clientDetailsRowMapper = new ClientDetailsRowMapper();

	/**
	 * 数据库操作工具类
	 */
	private final JdbcClient jdbcClient;

	/**
	 * 查询客户端详情
	 */
	@Setter
	private String selectClientDetailsSql = """
		SELECT client_id,resource_ids,client_secret,scope,authorized_grant_types,
		 registered_redirect_uri,access_token_validity,refresh_token_validity,
		 additional_information,auto_approve
		 FROM bedrock_client WHERE client_id = ? and is_deleted = 0
		""";

	@Override
	public ClientDetails loadClientByClientId(String clientId) {
		try {
			Optional<ClientDetailsModel> first = jdbcClient.sql(selectClientDetailsSql)
				.param(clientId)
				.query(clientDetailsRowMapper)
				.stream()
				.findFirst();
			return first.orElseThrow(ClientNotFoundOauth2Exception::new);
		} catch (ClientNotFoundOauth2Exception e) {
			throw e;
		} catch (Exception e) {
			throw new ClientNotFoundOauth2Exception(e);
		}
	}

	private static class ClientDetailsRowMapper implements RowMapper<ClientDetailsModel> {

		@Override
		public ClientDetailsModel mapRow(ResultSet rs, int rowNum) throws SQLException {
			ClientDetailsModel clientDetailsModel = new ClientDetailsModel();
			String clientId = rs.getString(1);
			String resourceIds = rs.getString(2);
			String clientSecret = rs.getString(3);
			String scope = rs.getString(4);
			String authorizedGrantTypes = rs.getString(5);
			String registeredRedirectUri = rs.getString(6);
			Integer accessTokenValidity = rs.getInt(7);
			Integer refreshTokenValidity = rs.getInt(8);
			String additionalInformation = rs.getString(9);
			String autoApprove = rs.getString(10);

			clientDetailsModel.setClientId(clientId);
			clientDetailsModel.setClientSecret(clientSecret);
			if (StringUtil.isNotBlank(resourceIds)) {
				clientDetailsModel.setResourceIds(StringUtil.commaDelimitedListToSet(resourceIds));
			}
			if (StringUtil.isNotBlank(scope)) {
				clientDetailsModel.setScope(StringUtil.commaDelimitedListToSet(scope));
			}
			if (StringUtil.isNotBlank(authorizedGrantTypes)) {
				clientDetailsModel.setAuthorizedGrantTypes(StringUtil.commaDelimitedListToSet(authorizedGrantTypes));
			}
			if (StringUtil.isNotBlank(registeredRedirectUri)) {
				clientDetailsModel.setRegisteredRedirectUri(StringUtil.commaDelimitedListToSet(registeredRedirectUri));
			}
			if (StringUtil.isNotBlank(autoApprove)) {
				clientDetailsModel.setAutoApprove(StringUtil.commaDelimitedListToSet(autoApprove));
			}
			clientDetailsModel.setAccessTokenValidity(accessTokenValidity);
			clientDetailsModel.setRefreshTokenValidity(refreshTokenValidity);
			if (StringUtil.isNotBlank(additionalInformation)) {
				Map<String, Object> additionalInformationMap = JsonUtil.parse(additionalInformation, JsonUtil.getMapType(String.class, Object.class));
				clientDetailsModel.setAdditionalInformation(additionalInformationMap);
			}
			return clientDetailsModel;
		}
	}

}
