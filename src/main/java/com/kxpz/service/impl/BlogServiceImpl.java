package com.kxpz.service.impl;

import com.kxpz.entity.Blog;
import com.kxpz.mapper.BlogMapper;
import com.kxpz.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 春深
 * @since  
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

}
