package com.kxpz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kxpz.entity.User;
import com.kxpz.mapper.UserMapper;
import com.kxpz.service.IUserService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 春深
 * @since  
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
