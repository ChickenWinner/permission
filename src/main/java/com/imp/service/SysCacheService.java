package com.imp.service;

import com.google.common.base.Joiner;
import com.imp.beans.CacheKeyConstants;
import com.imp.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.ShardedJedis;

import javax.annotation.Resource;

@Service
@Slf4j
public class SysCacheService {

    @Resource
    private RedisPool redisPool;

    public void saveCache(String toSavedValue, int timeoutSeconds, CacheKeyConstants prefix) {
        saveCache(toSavedValue, timeoutSeconds, prefix, null);
    }

    public void saveCache(String toSavedValue, int timeoutSeconds, CacheKeyConstants prefix, String... keys) {
        if (toSavedValue == null) {
            return;
        }
        ShardedJedis shardedJedis = null;
        try {
            // key
            String cacheKey = generateCacheKey(prefix, keys);
            // 获取连接
            shardedJedis = redisPool.instance();
            // 保存值
            shardedJedis.setex(cacheKey, timeoutSeconds, toSavedValue);
        } catch (Exception e) {
            log.error("save cache exception, prefix:{}, keys:{}", prefix.name(), JsonMapper.obj2String(keys), e);
        } finally {
            // 关闭连接
            redisPool.safeClose(shardedJedis);
        }
    }

    public String getFromCache(CacheKeyConstants prefix, String... keys) {
        ShardedJedis shardedJedis = null;
        // key
        String cacheKey = generateCacheKey(prefix, keys);
        try {
            // 获取连接
            shardedJedis = redisPool.instance();
            // 得到value String类型
            String value = shardedJedis.get(cacheKey);
            return value;
        } catch (Exception e) {
            log.error("get from cache exception, prefix:{}, keys:{}", prefix.name(), JsonMapper.obj2String(keys), e);
            return null;
        } finally {
            redisPool.safeClose(shardedJedis);
        }
    }

    private String generateCacheKey(CacheKeyConstants prefix, String... keys) {
        String key = prefix.name();
        if (keys != null && keys.length > 0) {
            key += "_" + Joiner.on("_").join(keys);
        }
        return key;
    }
}
