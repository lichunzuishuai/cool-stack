package com.kxpz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kxpz.dto.LoginFormDTO;
import com.kxpz.dto.Result;
import com.kxpz.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 春深
 * @since  
 */
public interface IUserService extends IService<User> {
    /*
    发送验证码
     */
    Result sendCode(String phone, HttpSession session);
    /*
    登录功能
     */
    Result login(LoginFormDTO loginForm, HttpSession session);
    /*
    登出功能
     */
    Result logout(HttpServletRequest request);
}
