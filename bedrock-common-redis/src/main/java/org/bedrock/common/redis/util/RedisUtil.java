package org.bedrock.common.redis.util;

import lombok.Getter;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Redis 工具类：封装 Redis 常用操作，简化缓存交互逻辑
 *
 * <p>来源：基于 <a href="https://gitee.com/izxf/RedisUtil/blob/master/RedisUtils.java">RedisUtils</a> 优化改造
 *
 * <p>核心功能：
 * <ul>
 *   <li>支持 String、Hash、List、Set、ZSet 等 Redis 核心数据结构操作</li>
 *   <li>提供缓存增删改查、过期时间设置、批量操作等常用方法</li>
 *   <li>集成序列化/反序列化处理（依赖注入的 RedisTemplate）</li>
 * </ul>
 *
 * <p>注意事项：
 * <ul>
 *   <li>key 命名建议遵循 "业务模块:功能:唯一标识" 格式（如 "user:info:1001"），避免命名冲突</li>
 *   <li>序列化方式需与 RedisTemplate 配置保持一致（如使用 Kryo/Protostuff 时，确保对象可被正确序列化）</li>
 *   <li>批量操作（如 multiSet、delete）需注意单次操作数量，避免 Redis 阻塞</li>
 * </ul>
 */
@SuppressWarnings("unchecked")
public abstract class RedisUtil {

	@Getter
	private static RedisTemplate<String, Object> redisTemplate;

	@Getter
	private static StringRedisTemplate stringRedisTemplate;

	// -------------------key相关操作---------------------

	/**
	 * 删除key
	 *
	 * @param key 键
	 */
	public static void delete(String key) {
		redisTemplate.delete(key);
	}

	/**
	 * 批量删除key
	 *
	 * @param keys 多个键
	 */
	public static void delete(Collection<String> keys) {
		redisTemplate.delete(keys);
	}

	/**
	 * 序列化key
	 *
	 * @param key 键
	 * @return 字节数组
	 */
	public static byte[] dump(String key) {
		return redisTemplate.dump(key);
	}

	/**
	 * 是否存在key
	 *
	 * @param key 键
	 * @return 是否存在
	 */
	public static Boolean hasKey(String key) {
		return redisTemplate.hasKey(key);
	}

	/**
	 * 设置过期时间
	 *
	 * @param key     键
	 * @param timeout 过期时间
	 * @param unit    单位
	 * @return 是否成功
	 */
	public static Boolean expire(String key, long timeout, TimeUnit unit) {
		return redisTemplate.expire(key, timeout, unit);
	}

	/**
	 * 设置过期时间
	 *
	 * @param key  键
	 * @param date 日期
	 * @return 是否成功
	 */
	public static Boolean expireAt(String key, Date date) {
		return redisTemplate.expireAt(key, date);
	}

	/**
	 * 查找匹配的key
	 *
	 * @param pattern 正则表达式
	 * @return keySet
	 */
	public static Set<String> keys(String pattern) {
		return redisTemplate.keys(pattern);
	}

	/**
	 * 将当前数据库的 key 移动到给定的数据库 db 当中
	 *
	 * @param key     键
	 * @param dbIndex 数据库索引
	 * @return 是否成功
	 */
	public static Boolean move(String key, int dbIndex) {
		return redisTemplate.move(key, dbIndex);
	}

	/**
	 * 移除 key 的过期时间，key 将持久保持
	 *
	 * @param key 键
	 * @return 是否成功
	 */
	public static Boolean persist(String key) {
		return redisTemplate.persist(key);
	}

	/**
	 * 返回 key 的剩余的过期时间
	 *
	 * @param key  键
	 * @param unit 单位
	 * @return 剩余时间
	 */
	public static Long getExpire(String key, TimeUnit unit) {
		return redisTemplate.getExpire(key, unit);
	}

	/**
	 * 返回 key 的剩余的过期时间
	 *
	 * @param key 键
	 * @return 过期时间
	 */
	public static Long getExpire(String key) {
		return redisTemplate.getExpire(key);
	}

	/**
	 * 从当前数据库中随机返回一个 key
	 *
	 * @return 键
	 */
	public static String randomKey() {
		return redisTemplate.randomKey();
	}

	/**
	 * 修改 key 的名称
	 *
	 * @param oldKey 旧的键
	 * @param newKey 新的键
	 */
	public static void rename(String oldKey, String newKey) {
		redisTemplate.rename(oldKey, newKey);
	}

	/**
	 * 仅当 newkey 不存在时，将 oldKey 改名为 newkey
	 *
	 * @param oldKey 旧的键
	 * @param newKey 新的键
	 * @return 是否成功
	 */
	public static Boolean renameIfAbsent(String oldKey, String newKey) {
		return redisTemplate.renameIfAbsent(oldKey, newKey);
	}

	/**
	 * 返回 key 所储存的值的类型
	 *
	 * @param key 键
	 * @return 值类型
	 */
	public static DataType type(String key) {
		return redisTemplate.type(key);
	}

	// -------------------string相关操作---------------------

	/**
	 * 设置指定 key 的值
	 *
	 * @param key   键
	 * @param value 值
	 */
	public static void set(String key, Object value) {
		redisTemplate.opsForValue().set(key, value);
	}

	/**
	 * 设置指定 key 的值
	 *
	 * @param key     键
	 * @param value   值
	 * @param timeout 过期时间
	 * @param unit    单位
	 */
	public static void set(String key, Object value, long timeout, TimeUnit unit) {
		redisTemplate.opsForValue().set(key, value, timeout, unit);
	}

	/**
	 * 获取指定 key 的值
	 *
	 * @param key 键
	 * @return 值
	 */
	@Nullable
	public static <T> T get(String key) {
		return (T) redisTemplate.opsForValue().get(key);
	}

	/**
	 * 获取指定 key 的值
	 *
	 * @param key 键
	 * @return 值
	 */
	@Nullable
	public static <T> T get(String key, Function<String, T> callable) {
		T result = (T) redisTemplate.opsForValue().get(key);
		if (result == null) {
			result = callable.apply(key);
		}
		set(key, result);
		return result;
	}

	/**
	 * 返回 key 中字符串值的子字符
	 *
	 * @param key   键
	 * @param start 开始下标
	 * @param end   结束下标
	 * @return 子字符串
	 */
	public static String getRange(String key, long start, long end) {
		return redisTemplate.opsForValue().get(key, start, end);
	}

	/**
	 * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)
	 *
	 * @param key   键
	 * @param value 值
	 * @return 旧值
	 */
	@Nullable
	public static <T> T getAndSet(String key, Object value) {
		return (T) redisTemplate.opsForValue().getAndSet(key, value);
	}

	/**
	 * 对 key 所储存的字符串值，获取指定偏移量上的位(bit)
	 *
	 * @param key    键
	 * @param offset 偏移量
	 * @return 存储在偏移处的原始位值
	 */
	public static Boolean getBit(String key, long offset) {
		return redisTemplate.opsForValue().getBit(key, offset);
	}

	/**
	 * 批量获取
	 *
	 * @param keys 多个键
	 * @return 多个值
	 */
	public static List<Object> multiGet(Collection<String> keys) {
		return redisTemplate.opsForValue().multiGet(keys);
	}

	/**
	 * 设置ASCII码, 字符串'a'的ASCII码是97, 转为二进制是'01100001', 此方法是将二进制第offset位值变为value
	 *
	 * @param key    键
	 * @param offset 位置
	 * @param value  值,true为1, false为0
	 * @return 存储在偏移处的原始位值
	 */
	public static Boolean setBit(String key, long offset, boolean value) {
		return redisTemplate.opsForValue().setBit(key, offset, value);
	}

	/**
	 * 将值 value 关联到 key ，并将 key 的过期时间设为 timeout
	 *
	 * @param key     键
	 * @param value   值
	 * @param timeout 过期时间
	 * @param unit    时间单位, 天:TimeUnit.DAYS 小时:TimeUnit.HOURS 分钟:TimeUnit.MINUTES
	 *                秒:TimeUnit.SECONDS 毫秒:TimeUnit.MILLISECONDS
	 */
	public static void setEx(String key, Object value, long timeout, TimeUnit unit) {
		redisTemplate.opsForValue().set(key, value, timeout, unit);
	}

	/**
	 * 只有在 key 不存在时设置 key 的值
	 *
	 * @param key   键
	 * @param value 值
	 * @return 之前已经存在返回false, 不存在返回true
	 */
	public static Boolean setIfAbsent(String key, Object value) {
		return redisTemplate.opsForValue().setIfAbsent(key, value);
	}

	/**
	 * 用 value 参数覆写给定 key 所储存的字符串值，从偏移量 offset 开始
	 *
	 * @param key    键
	 * @param value  值
	 * @param offset 从指定位置开始覆写
	 */
	public static void setRange(String key, Object value, long offset) {
		redisTemplate.opsForValue().set(key, value, offset);
	}

	/**
	 * 获取字符串的长度
	 *
	 * @param key 键
	 * @return 长度
	 */
	public static Long size(String key) {
		return redisTemplate.opsForValue().size(key);
	}

	/**
	 * 批量添加
	 *
	 * @param maps 多个map
	 */
	public static void multiSet(Map<String, Object> maps) {
		redisTemplate.opsForValue().multiSet(maps);
	}

	/**
	 * 同时设置一个或多个 key-value 对，当且仅当所有给定 key 都不存在
	 *
	 * @param maps 多个map
	 * @return 之前已经存在返回false, 不存在返回true
	 */
	public static Boolean multiSetIfAbsent(Map<String, Object> maps) {
		return redisTemplate.opsForValue().multiSetIfAbsent(maps);
	}

	/**
	 * 增加(自增长), 负数则为自减
	 *
	 * @param key       键
	 * @param increment 自增数值
	 * @return 增量后的键值
	 */
	public static Long incrBy(String key, long increment) {
		return redisTemplate.opsForValue().increment(key, increment);
	}

	/**
	 * 增加(自增长), 负数则为自减
	 *
	 * @param key       键
	 * @param increment 自增数值
	 * @return 增量后的键值
	 */
	public static Double incrByFloat(String key, double increment) {
		return redisTemplate.opsForValue().increment(key, increment);
	}

	/**
	 * 追加到末尾
	 *
	 * @param key   键
	 * @param value 值
	 * @return 增量后的键值
	 */
	public static Integer append(String key, String value) {
		return redisTemplate.opsForValue().append(key, value);
	}

	// -------------------hash相关操作-------------------------

	/**
	 * 获取存储在哈希表中指定字段的值
	 *
	 * @param key   键
	 * @param field 字段
	 * @return 值
	 */
	public static Object hGet(String key, String field) {
		return redisTemplate.opsForHash().get(key, field);
	}

	/**
	 * 获取所有给定字段的值
	 *
	 * @param key 键
	 * @return map
	 */
	public static Map<Object, Object> hGetAll(String key) {
		return redisTemplate.opsForHash().entries(key);
	}

	/**
	 * 获取所有给定字段的值
	 *
	 * @param key    键
	 * @param fields 字段
	 * @return list
	 */
	public static List<Object> hMultiGet(String key, Collection<Object> fields) {
		return redisTemplate.opsForHash().multiGet(key, fields);
	}

	/**
	 * 增加一个哈希表字段
	 *
	 * @param key     键
	 * @param hashKey hash键
	 * @param value   hash值
	 */
	public static void hPut(String key, String hashKey, Object value) {
		redisTemplate.opsForHash().put(key, hashKey, value);
	}

	/**
	 * 增加多个哈希表字段
	 *
	 * @param key  键
	 * @param maps 多个map
	 */
	public static void hPutAll(String key, Map<String, Object> maps) {
		redisTemplate.opsForHash().putAll(key, maps);
	}

	/**
	 * 仅当hashKey不存在时才设置
	 *
	 * @param key     键
	 * @param hashKey hash键
	 * @param value   值
	 * @return 是否成功
	 */
	public static Boolean hPutIfAbsent(String key, String hashKey, Object value) {
		return redisTemplate.opsForHash().putIfAbsent(key, hashKey, value);
	}

	/**
	 * 删除一个或多个哈希表字段
	 *
	 * @param key    键
	 * @param fields 多个值（可选）
	 * @return 删除数量
	 */
	public static Long hDelete(String key, Object... fields) {
		return redisTemplate.opsForHash().delete(key, fields);
	}

	/**
	 * 查看哈希表 key 中，指定的字段是否存在
	 *
	 * @param key   键
	 * @param field 字段
	 * @return 是否存在
	 */
	public static Boolean hExists(String key, String field) {
		return redisTemplate.opsForHash().hasKey(key, field);
	}

	/**
	 * 为哈希表 key 中的指定字段的整数值加上增量 increment
	 *
	 * @param key       键
	 * @param field     字段
	 * @param increment 自增数值
	 * @return 增量后的值
	 */
	public static Long hIncrBy(String key, Object field, long increment) {
		return redisTemplate.opsForHash().increment(key, field, increment);
	}

	/**
	 * 为哈希表 key 中的指定字段的整数值加上增量 increment
	 *
	 * @param key   键
	 * @param field 字段
	 * @param delta 自增数值
	 * @return 增量后的值
	 */
	public static Double hIncrByFloat(String key, Object field, double delta) {
		return redisTemplate.opsForHash().increment(key, field, delta);
	}

	/**
	 * 获取所有哈希表中的字段
	 *
	 * @param key 键
	 * @return 所有字段
	 */
	public static Set<Object> hKeys(String key) {
		return redisTemplate.opsForHash().keys(key);
	}

	/**
	 * 获取哈希表中字段的数量
	 *
	 * @param key 键
	 * @return 哈希表长度
	 */
	public static Long hSize(String key) {
		return redisTemplate.opsForHash().size(key);
	}

	/**
	 * 获取哈希表中所有值
	 *
	 * @param key 键
	 * @return 所有值
	 */
	public static List<Object> hValues(String key) {
		return redisTemplate.opsForHash().values(key);
	}

	/**
	 * 迭代哈希表中的键值对
	 *
	 * @param key     键
	 * @param options 扫描选项
	 * @return 游标
	 */
	public static Cursor<Map.Entry<Object, Object>> hScan(String key, ScanOptions options) {
		return redisTemplate.opsForHash().scan(key, options);
	}

	// ------------------------list相关操作----------------------------

	/**
	 * 通过索引获取列表中的元素
	 *
	 * @param key   键
	 * @param index 索引
	 * @return 值
	 */
	public static <T> T lIndex(String key, long index) {
		return (T) redisTemplate.opsForList().index(key, index);
	}

	/**
	 * 获取列表指定范围内的元素
	 *
	 * @param key   键
	 * @param start 开始位置, 0是开始位置
	 * @param end   结束位置, -1返回所有
	 * @return 多个值
	 */
	public static <T> List<T> lRange(String key, long start, long end) {
		return (List<T>) redisTemplate.opsForList().range(key, start, end);
	}

	/**
	 * 存储在list头部
	 *
	 * @param key   键
	 * @param value 值
	 * @return 操作后列表的长度
	 */
	public static Long lLeftPush(String key, Object value) {
		return redisTemplate.opsForList().leftPush(key, value);
	}

	/**
	 * 存储在list头部
	 *
	 * @param key   键
	 * @param value 多个值（可选）
	 * @return 操作后列表的长度
	 */
	public static Long lLeftPushAll(String key, Object... value) {
		return redisTemplate.opsForList().leftPushAll(key, value);
	}

	/**
	 * 存储在list头部
	 *
	 * @param key   键
	 * @param value 多个值
	 * @return 操作后列表的长度
	 */
	public static Long lLeftPushAll(String key, Collection<Object> value) {
		return redisTemplate.opsForList().leftPushAll(key, value);
	}

	/**
	 * 当list存在的时候才加入
	 *
	 * @param key   键
	 * @param value 值
	 * @return 操作后列表的长度
	 */
	public static Long lLeftPushIfPresent(String key, Object value) {
		return redisTemplate.opsForList().leftPushIfPresent(key, value);
	}

	/**
	 * 如果pivot存在,再pivot前面添加
	 *
	 * @param key   键
	 * @param pivot pivot
	 * @param value 值
	 * @return 操作后列表的长度
	 */
	public static Long lLeftPush(String key, Object pivot, Object value) {
		return redisTemplate.opsForList().leftPush(key, pivot, value);
	}

	/**
	 * 存储在list尾部
	 *
	 * @param key   键
	 * @param value 值
	 * @return 操作后列表的长度
	 */
	public static Long lRightPush(String key, Object value) {
		return redisTemplate.opsForList().rightPush(key, value);
	}

	/**
	 * 存储在list尾部
	 *
	 * @param key   键
	 * @param value 多个值（可选）
	 * @return 操作后列表的长度
	 */
	public static Long lRightPushAll(String key, Object... value) {
		return redisTemplate.opsForList().rightPushAll(key, value);
	}

	/**
	 * 存储在list尾部
	 *
	 * @param key   键
	 * @param value 多个值
	 * @return 操作后列表的长度
	 */
	public static Long lRightPushAll(String key, Collection<Object> value) {
		return redisTemplate.opsForList().rightPushAll(key, value);
	}

	/**
	 * 为已存在的列表添加值
	 *
	 * @param key   键
	 * @param value 值
	 * @return 操作后列表的长度
	 */
	public static Long lRightPushIfPresent(String key, Object value) {
		return redisTemplate.opsForList().rightPushIfPresent(key, value);
	}

	/**
	 * 在pivot元素的右边添加值
	 *
	 * @param key   键
	 * @param pivot pivot
	 * @param value 值
	 * @return 操作后列表的长度
	 */
	public static Long lRightPush(String key, Object pivot, Object value) {
		return redisTemplate.opsForList().rightPush(key, pivot, value);
	}

	/**
	 * 通过索引设置列表元素的值
	 *
	 * @param key   键
	 * @param index 位置
	 * @param value 值
	 */
	public static void lSet(String key, long index, Object value) {
		redisTemplate.opsForList().set(key, index, value);
	}

	/**
	 * 移出并获取列表的第一个元素
	 *
	 * @param key 键
	 * @return 删除的元素
	 */
	public static <T> T lLeftPop(String key) {
		return (T) redisTemplate.opsForList().leftPop(key);
	}

	/**
	 * 移出并获取列表的第一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
	 *
	 * @param key     键
	 * @param timeout 等待时间
	 * @param unit    时间单位
	 * @return 第一个元素
	 */
	public static <T> T lLeftPop(String key, long timeout, TimeUnit unit) {
		return (T) redisTemplate.opsForList().leftPop(key, timeout, unit);
	}

	/**
	 * 移除并获取列表最后一个元素
	 *
	 * @param key 键
	 * @return 删除的元素
	 */
	public static <T> T lRightPop(String key) {
		return (T) redisTemplate.opsForList().rightPop(key);
	}

	/**
	 * 移出并获取列表的最后一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
	 *
	 * @param key     键
	 * @param timeout 等待时间
	 * @param unit    时间单位
	 * @return 最后一个元素
	 */
	public static <T> T lRightPop(String key, long timeout, TimeUnit unit) {
		return (T) redisTemplate.opsForList().rightPop(key, timeout, unit);
	}

	/**
	 * 移除列表的最后一个元素，并将该元素添加到另一个列表并返回
	 *
	 * @param sourceKey      来源key
	 * @param destinationKey 目标key
	 * @return 最后一个元素
	 */
	public static <T> T lRightPopAndLeftPush(String sourceKey, String destinationKey) {
		return (T) redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey);
	}

	/**
	 * 从列表中弹出一个值，将弹出的元素插入到另外一个列表中并返回它； 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
	 *
	 * @param sourceKey      来源key
	 * @param destinationKey 目标key
	 * @param timeout        过期时间
	 * @param unit           单位
	 * @return 值
	 */
	public static <T> T lRightPopAndLeftPush(String sourceKey, String destinationKey, long timeout, TimeUnit unit) {
		return (T) redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey, timeout, unit);
	}

	/**
	 * 删除集合中值等于value得元素
	 *
	 * @param key   键
	 * @param index index=0, 删除所有值等于value的元素; index>0, 从头部开始删除第一个值等于value的元素;
	 *              index<0, 从尾部开始删除第一个值等于value的元素;
	 * @param value 值
	 * @return list长度
	 */
	public static Long lRemove(String key, long index, Object value) {
		return redisTemplate.opsForList().remove(key, index, value);
	}

	/**
	 * 裁剪list
	 *
	 * @param key   键
	 * @param start 开始下标
	 * @param end   结果下标
	 */
	public static void lTrim(String key, long start, long end) {
		redisTemplate.opsForList().trim(key, start, end);
	}

	/**
	 * 获取列表长度
	 *
	 * @param key 键
	 * @return list长度
	 */
	public static Long lLen(String key) {
		return redisTemplate.opsForList().size(key);
	}

	// --------------------set相关操作--------------------------

	/**
	 * set添加元素
	 *
	 * @param key    键
	 * @param values 多个值（可选）
	 * @return 添加到集合中的元素数量，不包括已经存在于集合中的所有元素
	 */
	public static Long sAdd(String key, Object... values) {
		return redisTemplate.opsForSet().add(key, values);
	}

	/**
	 * set移除元素
	 *
	 * @param key    键
	 * @param values 多个值
	 * @return 从集合中删除的成员数，不包括非现有成员。
	 */
	public static Long sRemove(String key, Object... values) {
		return redisTemplate.opsForSet().remove(key, values);
	}

	/**
	 * 移除并返回集合的一个随机元素
	 *
	 * @param key 键
	 * @return 随机元素
	 */
	public static <T> T sPop(String key) {
		return (T) redisTemplate.opsForSet().pop(key);
	}

	/**
	 * 将元素value从一个集合移到另一个集合
	 *
	 * @param key     键
	 * @param value   值
	 * @param destKey 目标key
	 * @return 如果元素被移动，则为true。 如果元素不是源成员且未执行任何操作，则为false。
	 */
	public static Boolean sMove(String key, Object value, String destKey) {
		return redisTemplate.opsForSet().move(key, value, destKey);
	}

	/**
	 * 获取集合的大小
	 *
	 * @param key 键
	 * @return set长度
	 */
	public static Long sSize(String key) {
		return redisTemplate.opsForSet().size(key);
	}

	/**
	 * 判断集合是否包含value
	 *
	 * @param key   键
	 * @param value 值
	 * @return 是否存在
	 */
	public static Boolean sIsMember(String key, Object value) {
		return redisTemplate.opsForSet().isMember(key, value);
	}

	/**
	 * 获取两个集合的交集
	 *
	 * @param key      键一
	 * @param otherKey 键二
	 * @return 交集
	 */
	public static <T> Set<T> sIntersect(String key, String otherKey) {
		return (Set<T>) redisTemplate.opsForSet().intersect(key, otherKey);
	}

	/**
	 * 获取key集合与多个集合的交集
	 *
	 * @param key       键一
	 * @param otherKeys 多个其他键
	 * @return 交集
	 */
	public static <T> Set<T> sIntersect(String key, Collection<String> otherKeys) {
		return (Set<T>) redisTemplate.opsForSet().intersect(key, otherKeys);
	}

	/**
	 * key集合与otherKey集合的交集存储到destKey集合中
	 *
	 * @param key      键一
	 * @param otherKey 键二
	 * @param destKey  目标键
	 * @return set长度
	 */
	public static Long sIntersectAndStore(String key, String otherKey, String destKey) {
		return redisTemplate.opsForSet().intersectAndStore(key, otherKey, destKey);
	}

	/**
	 * key集合与多个集合的交集存储到destKey集合中
	 *
	 * @param key       键一
	 * @param otherKeys 多个其他键
	 * @param destKey   目标键
	 * @return 结果集中的元素数
	 */
	public static Long sIntersectAndStore(String key, Collection<String> otherKeys, String destKey) {
		return redisTemplate.opsForSet().intersectAndStore(key, otherKeys, destKey);
	}

	/**
	 * 获取两个集合的并集
	 *
	 * @param key       键
	 * @param otherKeys 多个其他键
	 * @return 并集
	 */
	public static <T> Set<T> sUnion(String key, String otherKeys) {
		return (Set<T>) redisTemplate.opsForSet().union(key, otherKeys);
	}

	/**
	 * 获取key集合与多个集合的并集
	 *
	 * @param key       键
	 * @param otherKeys 多个其他键
	 * @return 并集
	 */
	public static <T> Set<T> sUnion(String key, Collection<String> otherKeys) {
		return (Set<T>) redisTemplate.opsForSet().union(key, otherKeys);
	}

	/**
	 * key集合与otherKey集合的并集存储到destKey中
	 *
	 * @param key      键一
	 * @param otherKey 键二
	 * @param destKey  目标键
	 * @return 结果集中的元素数
	 */
	public static Long sUnionAndStore(String key, String otherKey, String destKey) {
		return redisTemplate.opsForSet().unionAndStore(key, otherKey, destKey);
	}

	/**
	 * key集合与多个集合的并集存储到destKey中
	 *
	 * @param key       键
	 * @param otherKeys 多个其他键
	 * @param destKey   目标键
	 * @return 结果集中的元素数
	 */
	public static Long sUnionAndStore(String key, Collection<String> otherKeys, String destKey) {
		return redisTemplate.opsForSet().unionAndStore(key, otherKeys, destKey);
	}

	/**
	 * 获取两个集合的差集
	 *
	 * @param key      键一
	 * @param otherKey 键二
	 * @return 差集
	 */
	public static <T> Set<T> sDifference(String key, String otherKey) {
		return (Set<T>) redisTemplate.opsForSet().difference(key, otherKey);
	}

	/**
	 * 获取key集合与多个集合的差集
	 *
	 * @param key       键
	 * @param otherKeys 多个其他键
	 * @return 差集
	 */
	public static <T> Set<T> sDifference(String key, Collection<String> otherKeys) {
		return (Set<T>) redisTemplate.opsForSet().difference(key, otherKeys);
	}

	/**
	 * key集合与otherKey集合的差集存储到destKey中
	 *
	 * @param key      键一
	 * @param otherKey 键二
	 * @param destKey  目标键
	 * @return 结果集中的元素数
	 */
	public static Long sDifference(String key, String otherKey, String destKey) {
		return redisTemplate.opsForSet().differenceAndStore(key, otherKey, destKey);
	}

	/**
	 * key集合与多个集合的差集存储到destKey中
	 *
	 * @param key       键
	 * @param otherKeys 多个其他键
	 * @param destKey   目标键
	 * @return 结果集中的元素数
	 */
	public static Long sDifference(String key, Collection<String> otherKeys, String destKey) {
		return redisTemplate.opsForSet().differenceAndStore(key, otherKeys, destKey);
	}

	/**
	 * 获取集合所有元素
	 *
	 * @param key 键
	 * @return 所有元素
	 */
	public static <T> Set<T> setMembers(String key) {
		return (Set<T>) redisTemplate.opsForSet().members(key);
	}

	/**
	 * 随机获取集合中的一个元素
	 *
	 * @param key 键
	 * @return 随机元素
	 */
	public static <T> Set<T> sRandomMember(String key) {
		return (Set<T>) redisTemplate.opsForSet().randomMember(key);
	}

	/**
	 * 随机获取集合中count个元素
	 *
	 * @param key   键
	 * @param count 数量
	 * @return 随机元素list
	 */
	public static <T> Set<T> sRandomMembers(String key, long count) {
		return (Set<T>) redisTemplate.opsForSet().randomMembers(key, count);
	}

	/**
	 * 随机获取集合中count个元素并且去除重复的
	 *
	 * @param key   键
	 * @param count 数量
	 * @return 随机元素set
	 */
	public static <T> Set<T> sDistinctRandomMembers(String key, long count) {
		return (Set<T>) redisTemplate.opsForSet().distinctRandomMembers(key, count);
	}

	/**
	 * 获取Cursor
	 *
	 * @param key     键
	 * @param options 扫描选项
	 * @return 游标
	 */
	public static <T> Cursor<T> sScan(String key, ScanOptions options) {
		return (Cursor<T>) redisTemplate.opsForSet().scan(key, options);
	}

	// ------------------zSet相关操作--------------------------------

	/**
	 * 添加元素,有序集合是按照元素的score值由小到大排列
	 *
	 * @param key   键
	 * @param value 值
	 * @param score 分数
	 * @return 是否成功
	 */
	public static Boolean zAdd(String key, Object value, double score) {
		return redisTemplate.opsForZSet().add(key, value, score);
	}

	/**
	 * 添加多个元素
	 *
	 * @param key    键
	 * @param values 多个值
	 * @return 添加的数量
	 */
	public static Long zAdd(String key, Set<ZSetOperations.TypedTuple<Object>> values) {
		return redisTemplate.opsForZSet().add(key, values);
	}

	/**
	 * 移除元素
	 *
	 * @param key    键
	 * @param values 多个值（可选）
	 * @return 删除的数量
	 */
	public static Long zRemove(String key, Object... values) {
		return redisTemplate.opsForZSet().remove(key, values);
	}

	/**
	 * 增加元素的score值，并返回增加后的值
	 *
	 * @param key   键
	 * @param value 值
	 * @param delta delta
	 * @return 成员的新分数（双精度浮点数），表示为字符串
	 */
	public static Double zIncrementScore(String key, Object value, double delta) {
		return redisTemplate.opsForZSet().incrementScore(key, value, delta);
	}

	/**
	 * 返回元素在集合的排名,有序集合是按照元素的score值由小到大排列
	 *
	 * @param key   键
	 * @param value 值
	 * @return 0表示第一位
	 */
	public static Long zRank(String key, Object value) {
		return redisTemplate.opsForZSet().rank(key, value);
	}

	/**
	 * 返回元素在集合的排名,按元素的score值由大到小排列
	 *
	 * @param key   键
	 * @param value 值
	 * @return 0表示第一位
	 */
	public static Long zReverseRank(String key, Object value) {
		return redisTemplate.opsForZSet().reverseRank(key, value);
	}

	/**
	 * 获取集合的元素, 从小到大排序
	 *
	 * @param key   键
	 * @param start 开始位置
	 * @param end   结束位置, -1查询所有
	 * @return
	 */
	public static <T> Set<T> zRange(String key, long start, long end) {
		return (Set<T>) redisTemplate.opsForZSet().range(key, start, end);
	}

	/**
	 * 获取集合元素, 并且把score值也获取
	 *
	 * @param key   键
	 * @param start 开始下标
	 * @param end   结束下标
	 * @return 集合元素
	 */
	public static Set<ZSetOperations.TypedTuple<Object>> zRangeWithScores(String key, long start, long end) {
		return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
	}

	/**
	 * 根据Score值查询集合元素
	 *
	 * @param key 键
	 * @param min 最小值
	 * @param max 最大值
	 * @return 集合元素
	 */
	public static <T> Set<T> zRangeByScore(String key, double min, double max) {
		return (Set<T>) redisTemplate.opsForZSet().rangeByScore(key, min, max);
	}

	/**
	 * 根据Score值查询集合元素, 从小到大排序
	 *
	 * @param key 键
	 * @param min 最小值
	 * @param max 最大值
	 * @return 集合元素
	 */
	public static Set<ZSetOperations.TypedTuple<Object>> zRangeByScoreWithScores(String key, double min, double max) {
		return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
	}

	/**
	 * 根据Score值查询集合元素, 从小到大排序
	 *
	 * @param key   键
	 * @param min   最小值
	 * @param max   最大值
	 * @param start 开始下标
	 * @param end   结束下标
	 * @return 集合元素
	 */
	public static Set<ZSetOperations.TypedTuple<Object>> zRangeByScoreWithScores(String key, double min, double max, long start, long end) {
		return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max, start, end);
	}

	/**
	 * 获取集合的元素, 从大到小排序
	 *
	 * @param key   键
	 * @param start 开始下标
	 * @param end   结束下标
	 * @return 集合元素
	 */
	public static <T> Set<T> zReverseRange(String key, long start, long end) {
		return (Set<T>) redisTemplate.opsForZSet().reverseRange(key, start, end);
	}

	/**
	 * 获取集合的元素, 从大到小排序, 并返回score值
	 *
	 * @param key   键
	 * @param start 开始下标
	 * @param end   结束下标
	 * @return 集合元素
	 */
	public static Set<ZSetOperations.TypedTuple<Object>> zReverseRangeWithScores(String key, long start, long end) {
		return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
	}

	/**
	 * 根据Score值查询集合元素, 从大到小排序
	 *
	 * @param key 键
	 * @param min 最小值
	 * @param max 最大值
	 * @return 集合元素
	 */
	public static <T> Set<T> zReverseRangeByScore(String key, double min, double max) {
		return (Set<T>) redisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
	}

	/**
	 * 根据Score值查询集合元素, 从大到小排序
	 *
	 * @param key 键
	 * @param min 最小值
	 * @param max 最大值
	 * @return 集合元素
	 */
	public static Set<ZSetOperations.TypedTuple<Object>> zReverseRangeByScoreWithScores(
		String key, double min, double max) {
		return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, min, max);
	}

	/**
	 * 根据Score值查询集合元素, 从大到小排序
	 *
	 * @param key   键
	 * @param min   最小值
	 * @param max   最大值
	 * @param start 开始下标
	 * @param end   结束下标
	 * @return 集合元素
	 */
	public static <T> Set<T> zReverseRangeByScore(String key, double min, double max, long start, long end) {
		return (Set<T>) redisTemplate.opsForZSet().reverseRangeByScore(key, min, max,
			start, end);
	}

	/**
	 * 根据score值获取集合元素数量
	 *
	 * @param key 键
	 * @param min 最小值
	 * @param max 最大值
	 * @return 数量
	 */
	public static Long zCount(String key, double min, double max) {
		return redisTemplate.opsForZSet().count(key, min, max);
	}

	/**
	 * 获取集合大小
	 *
	 * @param key 键
	 * @return 集合大小
	 */
	public static Long zSize(String key) {
		return redisTemplate.opsForZSet().size(key);
	}

	/**
	 * 获取集合大小
	 *
	 * @param key 键
	 * @return 集合大小
	 */
	public static Long zCard(String key) {
		return redisTemplate.opsForZSet().zCard(key);
	}

	/**
	 * 获取集合中value元素的score值
	 *
	 * @param key   键
	 * @param value 值
	 * @return score值
	 */
	public static Double zScore(String key, Object value) {
		return redisTemplate.opsForZSet().score(key, value);
	}

	/**
	 * 移除指定索引位置的成员
	 *
	 * @param key   键
	 * @param start 开始下标
	 * @param end   结束下标
	 * @return 值
	 */
	public static Long zRemoveRange(String key, long start, long end) {
		return redisTemplate.opsForZSet().removeRange(key, start, end);
	}

	/**
	 * 根据指定的score值的范围来移除成员
	 *
	 * @param key 键
	 * @param min 最小值
	 * @param max 最大值
	 * @return
	 */
	public static Long zRemoveRangeByScore(String key, double min, double max) {
		return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
	}

	/**
	 * 获取key和otherKey的并集并存储在destKey中
	 *
	 * @param key      键一
	 * @param otherKey 键二
	 * @param destKey  目标键
	 * @return 结果集中的元素数
	 */
	public static Long zUnionAndStore(String key, String otherKey, String destKey) {
		return redisTemplate.opsForZSet().unionAndStore(key, otherKey, destKey);
	}

	/**
	 * 获取key和otherKey的并集并存储在destKey中
	 *
	 * @param key       键
	 * @param otherKeys 多个其他键
	 * @param destKey   目标键
	 * @return 结果集中的元素数
	 */
	public static Long zUnionAndStore(String key, Collection<String> otherKeys, String destKey) {
		return redisTemplate.opsForZSet().unionAndStore(key, otherKeys, destKey);
	}

	/**
	 * 交集
	 *
	 * @param key      键一
	 * @param otherKey 键二
	 * @param destKey  目标键
	 * @return 结果集中的元素数
	 */
	public static Long zIntersectAndStore(String key, String otherKey, String destKey) {
		return redisTemplate.opsForZSet().intersectAndStore(key, otherKey, destKey);
	}

	/**
	 * 交集
	 *
	 * @param key       键
	 * @param otherKeys 多个其他键
	 * @param destKey   目标键
	 * @return 结果集中的元素数
	 */
	public static Long zIntersectAndStore(String key, Collection<String> otherKeys, String destKey) {
		return redisTemplate.opsForZSet().intersectAndStore(key, otherKeys, destKey);
	}

	/**
	 * 获取Cursor
	 *
	 * @param key     键
	 * @param options 扫描选项
	 * @return 游标
	 */
	public static Cursor<ZSetOperations.TypedTuple<Object>> zScan(String key, ScanOptions options) {
		return redisTemplate.opsForZSet().scan(key, options);
	}

	/**
	 * 初始化redis
	 */
	public static void init(RedisTemplate<String, Object> redisTemplate) {
		RedisUtil.redisTemplate = redisTemplate;
	}

	/**
	 * 初始化redis
	 */
	public static void init(StringRedisTemplate stringRedisTemplate) {
		RedisUtil.stringRedisTemplate = stringRedisTemplate;
	}
}
