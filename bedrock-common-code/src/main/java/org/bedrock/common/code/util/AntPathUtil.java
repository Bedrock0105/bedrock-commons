package org.bedrock.common.code.util;

import org.springframework.util.AntPathMatcher;

import java.util.Collection;
import java.util.Map;

public abstract class AntPathUtil {

	private static final AntPathMatcher DEFAULT_PATH_MATCHER = new AntPathMatcher();

	/**
	 * 判断是否是匹配模式
	 */
	public static boolean isPattern(String path) {
		return DEFAULT_PATH_MATCHER.isPattern(path);
	}

	/**
	 * 匹配
	 *
	 * @param pattern 匹配模式
	 * @param path    匹配路径
	 * @return
	 */
	public static boolean match(String pattern, String path) {
		if (pattern == null) {
			return false;
		}
		return DEFAULT_PATH_MATCHER.match(pattern, path);
	}

	/**
	 * 匹配
	 *
	 * @param pattern 匹配模式
	 * @param path    匹配路径
	 * @return
	 */
	public static boolean match(Collection<String> pattern, String path) {
		if (CollectionUtil.isEmpty(pattern)) {
			return false;
		}
		return pattern.stream().anyMatch(patternItem -> match(patternItem, path));
	}

	/**
	 * 匹配开头
	 *
	 * @param pattern 匹配模式
	 * @param path    匹配路径
	 * @return
	 */
	public static boolean matchStart(String pattern, String path) {
		return DEFAULT_PATH_MATCHER.matchStart(pattern, path);
	}

	/**
	 * 提取路径
	 *
	 * @param pattern 匹配模式
	 * @param path    匹配路径 <br/>
	 *                Given a pattern and a full path, determine the pattern-mapped part. <p>For example: <ul>
	 *                <li>'{@code /docs/cvs/commit.html}' and '{@code /docs/cvs/commit.html} &rarr; ''</li>
	 *                <li>'{@code /docs/*}' and '{@code /docs/cvs/commit} &rarr; '{@code cvs/commit}'</li>
	 *                <li>'{@code /docs/cvs/*.html}' and '{@code /docs/cvs/commit.html} &rarr; '{@code commit.html}'</li>
	 *                <li>'{@code /docs/**}' and '{@code /docs/cvs/commit} &rarr; '{@code cvs/commit}'</li>
	 *                <li>'{@code /docs/**\/*.html}' and '{@code /docs/cvs/commit.html} &rarr; '{@code cvs/commit.html}'</li>
	 *                <li>'{@code /*.html}' and '{@code /docs/cvs/commit.html} &rarr; '{@code docs/cvs/commit.html}'</li>
	 *                <li>'{@code *.html}' and '{@code /docs/cvs/commit.html} &rarr; '{@code /docs/cvs/commit.html}'</li>
	 *                <li>'{@code *}' and '{@code /docs/cvs/commit.html} &rarr; '{@code /docs/cvs/commit.html}'</li> </ul>
	 *                <p>Assumes that {@link #match} returns {@code true} for '{@code pattern}' and '{@code path}', but
	 *                does <strong>not</strong> enforce this.
	 * @return
	 */
	public static String extractPathWithinPattern(String pattern, String path) {
		return DEFAULT_PATH_MATCHER.extractPathWithinPattern(pattern, path);
	}

	/**
	 * 提取路径变量
	 *
	 * @param pattern 匹配模式
	 * @param path    匹配路径
	 * @return
	 */
	public static Map<String, String> extractUriTemplateVariables(String pattern, String path) {
		return DEFAULT_PATH_MATCHER.extractUriTemplateVariables(pattern, path);
	}

	/**
	 * Combine two patterns into a new pattern.
	 * <p>This implementation simply concatenates the two patterns, unless
	 * the first pattern contains a file extension match (e.g., {@code *.html}).
	 * In that case, the second pattern will be merged into the first. Otherwise,
	 * an {@code IllegalArgumentException} will be thrown.
	 * <h4>Examples</h4>
	 * <table border="1">
	 * <tr><th>Pattern 1</th><th>Pattern 2</th><th>Result</th></tr>
	 * <tr><td>{@code null}</td><td>{@code null}</td><td>&nbsp;</td></tr>
	 * <tr><td>/hotels</td><td>{@code null}</td><td>/hotels</td></tr>
	 * <tr><td>{@code null}</td><td>/hotels</td><td>/hotels</td></tr>
	 * <tr><td>/hotels</td><td>/bookings</td><td>/hotels/bookings</td></tr>
	 * <tr><td>/hotels</td><td>bookings</td><td>/hotels/bookings</td></tr>
	 * <tr><td>/hotels/*</td><td>/bookings</td><td>/hotels/bookings</td></tr>
	 * <tr><td>/hotels/&#42;&#42;</td><td>/bookings</td><td>/hotels/&#42;&#42;/bookings</td></tr>
	 * <tr><td>/hotels</td><td>{hotel}</td><td>/hotels/{hotel}</td></tr>
	 * <tr><td>/hotels/*</td><td>{hotel}</td><td>/hotels/{hotel}</td></tr>
	 * <tr><td>/hotels/&#42;&#42;</td><td>{hotel}</td><td>/hotels/&#42;&#42;/{hotel}</td></tr>
	 * <tr><td>/*.html</td><td>/hotels.html</td><td>/hotels.html</td></tr>
	 * <tr><td>/*.html</td><td>/hotels</td><td>/hotels.html</td></tr>
	 * <tr><td>/*.html</td><td>/*.txt</td><td>{@code IllegalArgumentException}</td></tr>
	 * </table>
	 *
	 * @param pattern1 the first pattern
	 * @param pattern2 the second pattern
	 * @return the combination of the two patterns
	 * @throws IllegalArgumentException if the two patterns cannot be combined
	 */
	public static String combine(String pattern1, String pattern2) {
		return DEFAULT_PATH_MATCHER.combine(pattern1, pattern2);
	}
}
