package com.kxpz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@MapperScan("com.kxpz.mapper")
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class KXPingZhanApplication {
    public static void main(String[] args) {
        SpringApplication.run(KXPingZhanApplication.class, args);
    }

}
