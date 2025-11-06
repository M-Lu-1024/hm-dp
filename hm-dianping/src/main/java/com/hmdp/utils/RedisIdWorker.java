package com.hmdp.utils;


import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {

    //要使用redis去做一个全局唯一的id
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }


    private long BEGIN_TIMESTAMP = 1577836800L;
    private long LENGTH = 1577836800L;

    public long nextID(String key){
        //获取到时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSeconds = now.toEpochSecond(ZoneOffset.UTC);
        long timeStamp = nowSeconds - BEGIN_TIMESTAMP;
        //获取到redis的自增Id
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + key + ":" + date);
        //拼接
        return timeStamp<<LENGTH | count;
    }
}
