package com.imp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import javax.annotation.Resource;

@Service("redisPool")
@Slf4j
public class RedisPool {

    @Resource(name = "shardedJedisPool")
    private ShardedJedisPool shardedJedisPool;

    // 获取一个连接
    public ShardedJedis instance() {
        return shardedJedisPool.getResource();
    }

    // 释放连接
    public void safeClose(ShardedJedis shardedJedis) {
        try {
           if (shardedJedis != null) {
               shardedJedis.close();
           }
        } catch (Exception e) {
            log.error("close redis resource exception", e);
        }
    }
}
