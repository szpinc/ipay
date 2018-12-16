package org.szpinc.pay.service;


import org.szpinc.pay.bean.Pay;

import java.util.List;

/**
 * @author GhostDog
 */
public interface PayService {
    /**
     * 获取支付列表
     *
     * @param status
     * @return
     */
    List<Pay> getPayList(Integer status);

    /**
     * 获得未支付列表
     *
     * @return
     */
    List<Pay> getNotPayList();


    /**
     * 获得支付
     *
     * @param id
     * @return
     */
    Pay getPay(String id);


    /**
     * 添加支付
     *
     * @param pay
     * @return
     */
    int addPay(Pay pay);

    /**
     * 更改支付
     *
     * @param pay
     * @return
     */
    int updatePay(Pay pay);

    /**
     * 更改支付状态
     *
     * @param id
     * @param status
     * @return
     */
    int changePayStatus(String id, Integer status);

    /**
     * 删除支付
     *
     * @param id
     * @return
     */
    int delPay(String id);

}
