package com.kxpz.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kxpz.dto.LoginFormDTO;
import com.kxpz.dto.Result;
import com.kxpz.dto.UserDTO;
import com.kxpz.entity.User;
import com.kxpz.mapper.UserMapper;
import com.kxpz.service.IUserService;

import com.kxpz.utils.RegexUtils;
import com.kxpz.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.kxpz.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 春深
 * @since  
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /*
    发送验证码
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1. 校验手机号
        if (RegexUtils.isPhoneInvalid( phone)) {
            return Result.fail("手机号格式错误！");
        }
        //2. 生成验证码
        String code = RandomUtil.randomNumbers(6);
        //3. 保存验证码到 redis
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY +phone, code, 60, TimeUnit.MINUTES);
        //4. 发送验证码
        log.debug("发送短信验证码成功，验证码：{}", code);
        return Result.ok("发送验证码成功！");
    }
    /*
    登录功能
     */
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1. 校验手机号
        if (RegexUtils.isPhoneInvalid( loginForm.getPhone())) {
            return Result.fail("手机号格式错误！");
        }
        // 2.校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + loginForm.getPhone());
        String code = loginForm.getCode();
        if(cacheCode == null || !code.equals(cacheCode)){
            return Result.fail("验证码错误！");
        }
        // 3. 根据手机号查询用户
        User user = query().eq("phone", loginForm.getPhone()).one();
        // 4. 判断用户是否存在
        if(user== null){
            //5. 不存在，创建新用户并保存
            user=createUserWithPhone(loginForm.getPhone());
        }
        // 生成token,作为登录令牌
        String token = UUID.randomUUID().toString();
        // 7.保存用户信息到redis
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO);
        // 转换所有值为String类型
        Map<String, String> stringUserMap = userMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().toString()
                ));
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY+token, stringUserMap);
        // 8.设置token有效期
        stringRedisTemplate.expire(LOGIN_USER_KEY+token,LOGIN_USER_TTL,TimeUnit.SECONDS );
        // 9.返回token
        return Result.ok(token);
    }
    /*
    登出功能
     */
    @Override
    public Result logout(HttpServletRequest request) {
        //获取请求头中的token
        String token = request.getHeader("authorizfation");
        if(token == null){
            return Result.fail("未登录！");
        }
        //删除Redis中的token
        stringRedisTemplate.delete(LOGIN_USER_KEY+token);
        //清理ThreadLocal中的数据
        UserHolder.removeUser();
        return Result.ok("删除成功");
    }
    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone( phone);
        user.setNickName("user_"+RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
