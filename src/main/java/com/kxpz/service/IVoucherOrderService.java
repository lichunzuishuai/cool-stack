package com.kxpz.service;

import com.kxpz.dto.Result;
import com.kxpz.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 春深
 * @since  
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    //秒杀券
    Result seckillVoucher(Long voucherId);

    void getResult(VoucherOrder voucherOrder);
}
