package com.kxpz.config;

import com.kxpz.utils.LoginInterceptor;
import com.kxpz.utils.RefreshTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
      // 登录拦截器
      registry.addInterceptor(new LoginInterceptor())
              .excludePathPatterns(
                      "/user/code",
                      "/user/login",
                      "/shop/**",
                      "/blog/hot",
                      "/shop-type/**",
                      "/upload/**",
                      "/voucher/**",
                      "/doc.html",           // 添加文档页面
                      "/swagger-ui.html",    // 添加Swagger页面
                      "/v2/api-docs/**" ,
                      "/webjars/**" ,   // 添加API文档路径
                      "/swagger-resources/**"
              ).order(1);
      //刷新token
      registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate)).addPathPatterns("/**").order(0);
    }
}
