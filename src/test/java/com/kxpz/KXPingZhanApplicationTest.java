package com.kxpz;

import com.kxpz.dto.LoginFormDTO;
import com.kxpz.dto.Result;
import com.kxpz.entity.Shop;
import com.kxpz.entity.User;
import com.kxpz.mapper.UserMapper;
import com.kxpz.service.impl.ShopServiceImpl;
import com.kxpz.service.impl.UserServiceImpl;
import com.kxpz.utils.CacheClient;
import com.kxpz.utils.RedisIdWorker;

import com.kxpz.utils.UserHolder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.kxpz.utils.RedisConstants.CACHE_SHOP_KEY;


@SpringBootTest
class KXPingZhanApplicationTest {
    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private CacheClient cacheClient;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private UserServiceImpl userService;
    @Resource
    private UserMapper userMapper;

    private ExecutorService executor = Executors.newFixedThreadPool(500);

    @Test
    void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);

        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id = " + id);
            }
            latch.countDown();
        };
        long start = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            executor.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("耗时：" + (end - start));
    }
    @Test
    void testSaveShop() throws InterruptedException {
        Shop shop = shopService.getById(1L);
        cacheClient.setWithLogicalExpire(CACHE_SHOP_KEY+1L,shop,10L, TimeUnit.SECONDS);
    }

    @Test
    void login() throws IOException {
        try {
            FileOutputStream fos = new FileOutputStream("D:\\token.txt");
            for (int i = 1; i <=1000 ; i++) {
                User user = userMapper.selectById(i);
                LoginFormDTO loginForm = new LoginFormDTO();
                loginForm.setPhone(user.getPhone());
                loginForm.setPassword(user.getPassword());
                Result login = userService.login(loginForm, null);
                String token = (String) login.getData();
                fos.write((token+"\n").getBytes());
            }
        } catch (IOException e) {
                throw new RuntimeException(e);
        }


    }
}