package com.kxpz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.kxpz.mapper")
@SpringBootApplication
public class KXPingZhanApplication {

    public static void main(String[] args) {
        SpringApplication.run(KXPingZhanApplication.class, args);
    }

}
