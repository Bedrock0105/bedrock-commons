package org.bedrock.common.auth.props;

import io.jsonwebtoken.JwtException;
import lombok.Getter;
import lombok.Setter;
import org.bedrock.common.auth.util.JwtUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = JwtProperties.PREFIX)
public class JwtProperties {

	public static final String PREFIX = "bedrock.authentication";

	/**
	 * 私钥 / 生成签名的时候使用的秘钥secret，一般可以从本地配置文件中读取，切记这个秘钥不能外露，只在服务端使用，在任何场景都不应该流露出去。
	 * 一旦客户端得知这个secret, 那就意味着客户端是可以自我签发jwt了。
	 * 应该大于等于 256位(长度32及以上的字符串)，并且是随机的字符串
	 */
	private String secretKey = "MIHwMIGoBgcqhkjOOsdsAQBMIGcAkEAKaCzo4Syrom78z3EQ5SbbB4sF7ey80etKII";

	/**
	 * jwt签发者
	 */
	private String jwtIss = "github";

	/**
	 * jwt主题
	 */
	private String jwtSubject = "github.com";

	public String getSecretKey() {
		if (secretKey == null || secretKey.length() < 32) {
			throw new JwtException("jwt secretKey秘钥 不能小于32位");
		}
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
		JwtUtil.clearSecretKey();
	}
}
