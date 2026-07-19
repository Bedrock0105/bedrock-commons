package org.bedrock.common.auth.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import org.bedrock.common.auth.props.JwtProperties;
import org.bedrock.common.code.util.SpringUtil;
import org.springframework.lang.Nullable;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

	private static JwtProperties jwtProperties;

	private static final String JWT_TYPE = "JWT";

	private static final String JWT_ALG = "HS256";

	/**
	 * 加密算法
	 */
	private final static MacAlgorithm ALGORITHM = Jwts.SIG.HS256;

	/**
	 * 秘钥实例
	 */
	public static volatile SecretKey KEY;

	/**
	 * 这些是一组预定义的声明，它们 不是强制性的，而是推荐的 ，以 提供一组有用的、可互操作的声明 。
	 * iss: jwt签发者
	 * sub: jwt所面向的用户
	 * aud: 接收jwt的一方
	 * exp: jwt的过期时间，这个过期时间必须要大于签发时间
	 * nbf: 定义在什么时间之前，该jwt都是不可用的.
	 * iat: jwt的签发时间
	 * jti: jwt的唯一身份标识，主要用来作为一次性token,从而回避重放攻击
	 */
	public static String createToken(String tokenId, Map<String, Object> claims, Integer expire) {
		Date exprireDate = Date.from(Instant.now().plusSeconds(expire));
		return Jwts.builder()
			// 设置头部信息header
			.header()
			.add("typ", JWT_TYPE)
			.add("alg", JWT_ALG)
			.and()
			// 设置自定义负载信息payload
			.claims(claims)
			// 令牌ID
			.id(tokenId)
			// 生效时间
			.notBefore(new Date())
			// 过期日期
			.expiration(exprireDate)
			// 签发时间
			.issuedAt(new Date())
			// 主题
			.subject(getJwtProperties().getJwtSubject())
			// 签发者
			.issuer(getJwtProperties().getJwtIss())
			// 签名
			.signWith(getKey(), ALGORITHM)
			.compact();
	}

	/**
	 * 解析token
	 *
	 * @param token token
	 * @return Jws<Claims>
	 */
	@Nullable
	public static Jws<Claims> parseClaim(String token) {
		try {
			return Jwts.parser()
				.verifyWith(getKey())
				.build()
				.parseSignedClaims(token);
		} catch (Exception e) {
			return null;
		}
	}

	@Nullable
	public static JwsHeader parseHeader(String token) {
		Jws<Claims> jws = parseClaim(token);
		if (jws == null) {
			return null;
		}
		return jws.getHeader();
	}

	@Nullable
	public static Claims parsePayload(String token) {
		Jws<Claims> jws = parseClaim(token);
		if (jws == null) {
			return null;
		}
		return jws.getPayload();
	}

	@Nullable
	public static String parseId(String token) {
		Jws<Claims> jws = parseClaim(token);
		if (jws == null) {
			return null;
		}
		return jws.getPayload().getId();
	}

	/**
	 * 获取jwt配置信息
	 *
	 * @return JwtProperties
	 */
	private static JwtProperties getJwtProperties() {
		if (jwtProperties == null) {
			jwtProperties = SpringUtil.getBean(JwtProperties.class);
		}
		return jwtProperties;
	}

	private static SecretKey getKey() {
		// 第一次检查，避免每次都加锁
		if (KEY == null) {
			// 加锁保证只有一个线程进入初始化逻辑
			synchronized (JwtUtil.class) {
				// 第二次检查，防止多个线程等待锁后重复初始化
				if (KEY == null) {
					KEY = Keys.hmacShaKeyFor(getJwtProperties().getSecretKey().getBytes());
				}
			}
		}
		return KEY;
	}

	/**
	 * 清空密钥
	 */
	public static void clearSecretKey() {
		synchronized (JwtUtil.class) {
			KEY = null;
		}
	}
}
