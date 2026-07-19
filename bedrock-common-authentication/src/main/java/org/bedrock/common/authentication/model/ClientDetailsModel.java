package org.bedrock.common.authentication.model;

import lombok.Setter;
import org.bedrock.common.authentication.support.ClientDetails;
import org.bedrock.common.code.util.CollectionUtil;
import org.bedrock.common.code.util.ObjectUtil;
import org.bedrock.common.code.util.StringUtil;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Setter
public class ClientDetailsModel implements ClientDetails, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private String clientId;

	private String clientSecret;

	private Set<String> resourceIds = Collections.emptySet();

	private Set<String> scope = Collections.emptySet();

	private Set<String> authorizedGrantTypes = Collections.emptySet();

	private Set<String> registeredRedirectUri = Collections.emptySet();

	private Integer accessTokenValidity;

	private Integer refreshTokenValidity;

	private Set<String> autoApprove = Collections.emptySet();

	private Map<String, Object> additionalInformation = Collections.emptyMap();

	@Override
	public String getClientId() {
		return this.clientId;
	}

	@Override
	public Set<String> getResourceIds() {
		return this.resourceIds;
	}

	@Override
	public boolean isSecretRequired() {
		return StringUtil.isBlank(this.clientSecret);
	}

	@Override
	public String getClientSecret() {
		return this.clientSecret;
	}

	@Override
	public boolean isScoped() {
		return CollectionUtil.isEmpty(this.scope);
	}

	@Override
	public Set<String> getScope() {
		return this.scope;
	}

	@Override
	public Set<String> getAuthorizedGrantTypes() {
		return this.authorizedGrantTypes;
	}

	@Override
	public Set<String> getRegisteredRedirectUri() {
		return this.registeredRedirectUri;
	}

	@Override
	public Integer getAccessTokenValiditySeconds() {
		return ObjectUtil.isEmpty(this.accessTokenValidity) ? 3600 : this.accessTokenValidity;
	}

	@Override
	public Integer getRefreshTokenValiditySeconds() {
		return ObjectUtil.isEmpty(this.refreshTokenValidity) ? 7200 : this.refreshTokenValidity;
	}

	@Override
	public boolean isAutoApprove(String scope) {
		if (autoApprove == null) {
			return false;
		}
		for (String s : autoApprove) {
			if (s.equals("true") || s.matches(scope)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Map<String, Object> getAdditionalInformation() {
		return this.additionalInformation;
	}

	public ClientDetailsModel() {
	}

	public ClientDetailsModel(String clientId,
							  String clientSecret,
							  Set<String> resourceIds,
							  Set<String> scope,
							  Set<String> authorizedGrantTypes,
							  Set<String> registeredRedirectUri,
							  Integer accessTokenValidity,
							  Integer refreshTokenValidity,
							  Set<String> autoApprove,
							  Map<String, Object> additionalInformation) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.resourceIds = resourceIds;
		this.scope = scope;
		this.authorizedGrantTypes = authorizedGrantTypes;
		this.registeredRedirectUri = registeredRedirectUri;
		this.accessTokenValidity = accessTokenValidity;
		this.refreshTokenValidity = refreshTokenValidity;
		this.autoApprove = autoApprove;
		this.additionalInformation = additionalInformation;
	}
}
