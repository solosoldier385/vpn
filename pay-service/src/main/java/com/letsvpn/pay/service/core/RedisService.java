package com.letsvpn.pay.service.core;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.letsvpn.pay.util.PayConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Aiden
 */
@Service
public class RedisService {
	private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

	@Autowired
	RedisTemplate<String, String> redisTemplate;

	public Boolean lock(String key) {
		return lock(key, 180);
	}

	public Boolean lock(String key, int expire) {
		return redisTemplate.boundValueOps(key).setIfAbsent(PayConstant.lock_value, expire, TimeUnit.SECONDS);
//		return redisTemplate.execute(new RedisCallback<String>() {
//			@Override
//			public String doInRedis(RedisConnection connection) throws DataAccessException {
//				JedisCommands commands = (JedisCommands) connection.getNativeConnection();
//				return commands.set(key, AppConstant.lock_value, "NX", "EX", expire);
//			}
//		});
	}

	public void set(String key, String value, long timeout) {
		redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
	}

	public String get(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	public void unLock(String key) {
		// 最常见的解锁代码就是直接使用jedis.del()方法删除锁，这种不先判断锁的拥有者而直接解锁的方式，会导致任何客户端都可以随时进行解锁，即使这把锁不是它的。

		if (PayConstant.lock_value.equals(redisTemplate.opsForValue().get(key))) {
			redisTemplate.delete(key);
		}
	}

	public String id(int length) {
		String recordIdStr = "000000000000000000" + id();
		return recordIdStr.substring(recordIdStr.length() - length);
	}

	public String id36(int length) {
		String recordIdStr = "000000000000000000" + Long.toString(id(), 36);
		return recordIdStr.substring(recordIdStr.length() - length);
	}

	public Long id() {
		return redisTemplate.opsForValue().increment("id", 1);
	}

	public String id4(String prefix, Date now) {
		return prefix + DateUtil.format(now, DatePattern.PURE_DATETIME_MS_PATTERN) + id(4);
	}

	public String id4h(String prefix, String salt, Date now) {
		return DateUtil.format(now, "yyMMddHHmmss") + prefix + salt + id36(4);
	}

	public String idLength36(Date now, int length) {
		String times = Long.toString(now.getTime(), 36);
		return times + id36(length - times.length() > 0 ? length - times.length() : 3);
	}
	public String idLength362(Date now, int length) {
		String times = Long.toString(now.getTime()/1000, 36);
		return times + id36(length - times.length() > 0 ? length - times.length() : 3);
	}
//	public static void main(String[] args) {
//		System.out.println(Long.toString(new Date().getTime()+1000L*3600*24*360*4, 36));
//	}

	public String id(String prefix, Date now, int length) {
		return prefix + DateUtil.format(now, DatePattern.PURE_DATETIME_MS_PATTERN) + id(length);
	}

//	public Long rightPush(String key, String value) {
//		return redisTemplate.boundListOps(key).rightPush(value);
//	}
	public Long leftPush(String key, String value) {
		return redisTemplate.boundListOps(key).leftPush(value);
	}

	public Long addSetAll(String key, String... value) {
		return redisTemplate.boundSetOps(key).add(value);
	}

	public Set<String> allSetData(String key) {
		return redisTemplate.boundSetOps(key).members();
	}

	public Long removeSet(String key, Object... value) {
		return redisTemplate.boundSetOps(key).remove(value);
	}

	public Boolean isMember(String key, String value) {
		return redisTemplate.opsForSet().isMember(key, value);
	}

	public Boolean delete(String key) {
		return redisTemplate.delete(key);
	}

//	public String leftPop(String key) {
//		return redisTemplate.opsForList().leftPop(key);
//	}
	public String rightPop(String key) {
		return redisTemplate.opsForList().rightPop(key);
	}

	public String rightPopAndLeftPush(String sourceKey, String destinationKey) {
		return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey);
	}

	public String rightPopAndLeftPush(String sourceKey, String destinationKey, long timeout) {
		return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey, timeout, TimeUnit.SECONDS);
	}

	public ListOperations<String, String> opsForList() {
		return redisTemplate.opsForList();
	}

	public Long removeList(String key, String value) {
		return redisTemplate.opsForList().remove(key, 0l, value);
	}

	public List<String> list(String key, long start, long end) {
		return redisTemplate.opsForList().range(key, start, end);
	}

	public Boolean expire(String key, long timeout) {
		return redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
	}

	public void convertAndSend(String channel, String msg) {
		redisTemplate.convertAndSend(channel, msg);
	}
}
