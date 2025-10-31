package com.kxpz.service;

import com.kxpz.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 春深
 * @since  
 */
public interface IShopTypeService extends IService<ShopType> {

    /*
    查询所有商铺类型
     */
    List<ShopType> listWithCache();
}

