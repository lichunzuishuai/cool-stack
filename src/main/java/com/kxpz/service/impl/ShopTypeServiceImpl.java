package com.kxpz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONUtil;
import com.kxpz.entity.ShopType;
import com.kxpz.mapper.ShopTypeMapper;
import com.kxpz.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kxpz.utils.CacheClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.kxpz.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 春深
 * @since  
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CacheClient cacheClient;
    /**
     * 获取所有商铺类型
     * @return 商铺类型列表
     */
    @Override
    public List<ShopType> listWithCache() {
        // 1.从redis中查询
        List<String> range = stringRedisTemplate.opsForList().range(CACHE_SHOP_TYPE_KEY, 0, -1);
        // 2.判断缓存是否存在
        if(range!= null){
            // 2.1 判断是否为空值标记
            if (range.isEmpty() || (range.size() == 1 && "".equals(range.get(0)))) {
                return List.of(); // 识别为空值缓存或空列表
            }
            // 2.2 缓存中有数据，直接返回
            if (CollUtil.isNotEmpty( range)) {
                return range.stream().map(json -> {
                    return JSONUtil.toBean(json, ShopType.class);
                }).toList();
            }
        }
        //3.缓存中不存在查询数据库
        List<ShopType> sort = query().orderByAsc("sort").list();
        if (CollUtil.isEmpty(sort)) {
            stringRedisTemplate.opsForValue().set(CACHE_SHOP_TYPE_KEY, "", CACHE_SHOP_TYPE_TTL, TimeUnit.MINUTES);
            return List.of();
        }
        //有数据，保存到redis中
        List<String> list = sort.stream().map(JSONUtil::toJsonStr).toList();
        stringRedisTemplate.opsForList().leftPushAll(CACHE_SHOP_TYPE_KEY, list);
        stringRedisTemplate.expire(
                CACHE_SHOP_TYPE_KEY,
                CACHE_SHOP_TYPE_EXPIRE_TTL+new Random().nextInt(10),
                TimeUnit.MINUTES);
        //3.返回
        return sort;

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
