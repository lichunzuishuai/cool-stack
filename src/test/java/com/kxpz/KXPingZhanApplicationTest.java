package com.kxpz;

import com.kxpz.entity.Shop;
import com.kxpz.service.impl.ShopServiceImpl;
import com.kxpz.utils.CacheClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;

import static com.kxpz.utils.RedisConstants.CACHE_SHOP_KEY;


@SpringBootTest
class KXPingZhanApplicationTest {
    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private CacheClient cacheClient;
    @Test
    void testSaveShop() throws InterruptedException {
        Shop shop = shopService.getById(1L);
        cacheClient.setWithLogicalExpire(CACHE_SHOP_KEY+1L,shop,10L, TimeUnit.SECONDS);
    }
}