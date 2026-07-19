package org.bedrock.common.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bedrock.common.authentication.support.UserDetails;

/**
 * 用户实现类
 */
@Getter
@Setter
@AllArgsConstructor
public class UserModel implements UserDetails {

	private String username;

	private String password;

	private boolean enabled;

}
