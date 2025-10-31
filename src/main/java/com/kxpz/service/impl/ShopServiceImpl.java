package com.kxpz.service.impl;

import com.kxpz.dto.Result;
import com.kxpz.entity.Shop;
import com.kxpz.mapper.ShopMapper;
import com.kxpz.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kxpz.utils.CacheClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

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
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CacheClient cacheClient;
   /**
     * 根据id查询商铺信息
     * @param id 商铺id
     * @return 商铺详情数据
     */
    @Override
    public Result queryById(Long id) {
        //缓存穿透
//        Shop shop = cacheClient
//                .queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //用互斥锁解决缓存击穿
        //Shop shop = queryWithMutex(id);
        //逻辑过期解决缓存击穿问题
        Shop shop = cacheClient.queryLogicalExpire(CACHE_SHOP_KEY, id, Shop.class
                , this::getById, 20L, TimeUnit.SECONDS);
        if(shop == null){
            return Result.fail("店铺不存在");
        }
        //7.返回
        return Result.ok(shop);
    }

    //线程池
    //private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

   //逻辑过期
   /* public Shop queryLogicalExpire(Long id){
        //1.从redis中查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
        //2.判断是否命中
        if(StrUtil.isBlank( shopJson)){
            //3.如果未命中，直接返回
            return null;
        }
        //4.命中，需要先把反序列化为对象
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        JSONObject data = (JSONObject) redisData.getData();
        Shop shop = JSONUtil.toBean(data, Shop.class);
        //需要判断过期时间
        //redisData.getExpireTime().isAfter(LocalDateTime.now())这行的意思是过期时间是不是在当前时间之后
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            //未过期，直接返回
            return shop;
        }
        //过期，需要缓存重建
        //缓存重建
        //获取互斥锁
        boolean isLock = tryLock(LOCK_SHOP_KEY + id);
        //判断是否获取锁
        if (!isLock) {
            //获取锁失败直接返回旧的数据
            return shop;
        }
        //5.获取锁成功,先查询一下缓存里面有没数据
        shopJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
        if(StrUtil.isNotBlank(shopJson)){
            //3.如果命中，直接返回
            shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        //开启独立线程，实现缓存重建
        CACHE_REBUILD_EXECUTOR.submit(()->{
            try {
                //实际中应该设置30 分钟这里是20秒
                this.saveShop2Redis(id, 5L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }finally {
                //释放锁
                unLock(LOCK_SHOP_KEY+id);
            }

        });
        //6.返回
        return shop;
    }*/

   //缓存击穿
   /*public Shop queryWithMutex(Long id){
        //1.从redis中查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
        //2.判断是否命中
        if(StrUtil.isNotBlank( shopJson)){
            //3.如果命中，直接返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        //3.1判断是不是空值
        if(shopJson!= null){
            //返回一个错误信息
            return null;
        }
        //4.实现缓存重建
        //4.1获取互斥锁
        Shop shop = null;
        try {
            boolean tryLock = tryLock(LOCK_SHOP_KEY + id);
            //判断锁是否获取成功
            if(!tryLock){
                //获取锁失败，休眠重试
                Thread.sleep(50);
                //递归
                return queryWithMutex(id);
            }
            //4.2获取锁成功再次检测redis缓存是否存在
            shopJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
            if(StrUtil.isNotBlank( shopJson)){
                //3.如果命中，直接返回
                shop = JSONUtil.toBean(shopJson, Shop.class);
                return shop;
            }
            //4.2缓存中还是没有数据，根据id查询数据库
            shop = getById(id);
            //5.判断数据库中是否有数据
            if(shop == null){
                //5.1不存在，将空值写入redis中
                stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY+id,"",CACHE_NULL_TTL,TimeUnit.MINUTES);
                return null;
            }
            //6.有数据，返回然后存到redis中
            stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY+id,JSONUtil.toJsonStr( shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            //释放锁
            unLock(LOCK_SHOP_KEY+id);
        }
        //7.返回
        return shop;
    }*/

    //缓存穿透
    /*  public Shop queryWithPassThrough(Long id){
        //1.从redis中查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
        //2.判断是否命中
        if(StrUtil.isNotBlank( shopJson)){
            //3.如果命中，直接返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        //3.1判断是不是空值
        if(shopJson!= null){
            //返回一个错误信息
            return null;
        }
        //4.没有命中，查询数据库
        Shop shop = getById(id);
        //5.判断数据库中是否有数据
        if(shop == null){
            //5.1不存在，将空值写入redis中
            stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY+id,"",CACHE_NULL_TTL,TimeUnit.MINUTES);
            return null;
        }
        //6.有数据，返回然后存到redis中
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY+id,JSONUtil.toJsonStr( shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //7.返回
        return shop;
    }*/


    /*//获取锁
    private boolean tryLock(String key){
        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(aBoolean);
    }
    //释放锁
    private void unLock(String key){
        stringRedisTemplate.delete(key);
    }

    //逻辑过期
    public void saveShop2Redis(Long id,Long expireSeconds) throws InterruptedException {
        //1.查询店铺数据
        Shop byId = getById(id);
        Thread.sleep(200);
        //2.封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(byId);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        //3.写入redis
        //实际中应该设置30分钟这里是20秒
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY+id,JSONUtil.toJsonStr(redisData));
    }*/

    /**
     * 修改商铺信息
     * @param shop 商铺数据
     * @return 商铺id
     */
    @Override
    @Transactional
    public Result update(Shop shop) {
        if(shop.getId()== null){
            return Result.fail("数据为空");
        }
        //1.修改数据库
        updateById(shop);
        //2.删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY+shop.getId());
        return Result.ok();
    }
}
