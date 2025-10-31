package com.kxpz.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


import static com.kxpz.utils.RedisConstants.*;
@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object  value , Long time, TimeUnit unit){
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value),time,unit);
    }
    /**
     * 逻辑过期解决缓存击穿
     */
    public void setWithLogicalExpire(String key, Object value , Long time, TimeUnit unit){
        // 设置过期时间
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        // 写入redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }
    //缓存穿透
    public <R,ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type , Function<ID,R> dbFallback,Long time, TimeUnit unit){
        //1.从redis中查询缓存
        String json = stringRedisTemplate.opsForValue().get(keyPrefix + id);
        //2.判断是否命中
        if(StrUtil.isNotBlank( json)){
            //3.如果命中，直接返回
            return JSONUtil.toBean(json, type);
        }
        //3.1判断是不是空值
        if(json!= null){
            //返回一个错误信息
            return null;
        }
        //4.没有命中，查询数据库
        R r=dbFallback.apply(id);
        //5.判断数据库中是否有数据
        if(r == null){
            //5.1不存在，将空值写入redis中
            stringRedisTemplate.opsForValue().set(keyPrefix+id,"",CACHE_NULL_TTL,TimeUnit.MINUTES);
            return null;
        }
        //6.有数据，返回然后存到redis中
        this.set(keyPrefix+id,r,time, unit);
        //7.返回
        return r;
    }
    //线程池
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
    //逻辑过期
    public <R,ID> R queryLogicalExpire(String keyPrefix, ID id,Class<R> type
            ,Function<ID,R> dbFallback ,Long time, TimeUnit unit){
        //1.从redis中查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(keyPrefix + id);
        //2.判断是否命中
        if(StrUtil.isBlank(json)){
            //3.如果未命中，直接返回
            return null;
        }
        //4.命中，需要先把反序列化为对象
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        JSONObject data = (JSONObject) redisData.getData();
        R r = JSONUtil.toBean(data, type);
        //需要判断过期时间
        //redisData.getExpireTime().isAfter(LocalDateTime.now())这行的意思是过期时间是不是在当前时间之后
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            //未过期，直接返回
            return r;
        }
        //过期，需要缓存重建
        //缓存重建
        //获取互斥锁
        boolean isLock = tryLock(LOCK_SHOP_KEY + id);
        //判断是否获取锁
        if (!isLock) {
            //获取锁失败直接返回旧的数据
            return r;
        }
        //开启独立线程，实现缓存重建
        CACHE_REBUILD_EXECUTOR.submit(()->{
            try {
                //缓存重建
                R r1 = dbFallback.apply(id);
                //写入redis
                this.setWithLogicalExpire(keyPrefix+id,r1,time, unit);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }finally {
                //释放锁
                unLock(LOCK_SHOP_KEY+id);
            }

        });
        //6.返回
        return r;
    }
    //获取锁
    private boolean tryLock(String key){
        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(aBoolean);
    }
    //释放锁
    private void unLock(String key){
        stringRedisTemplate.delete(key);
    }

}
