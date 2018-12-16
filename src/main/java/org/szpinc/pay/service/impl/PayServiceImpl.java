package org.szpinc.pay.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.szpinc.pay.bean.Pay;
import org.szpinc.pay.common.utils.StringUtils;
import org.szpinc.pay.repository.PayRepository;
import org.szpinc.pay.service.PayService;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PayServiceImpl implements PayService {

    private final Logger LOG = LoggerFactory.getLogger(PayServiceImpl.class);

    @Autowired
    private PayRepository payRepository;


    @Override
    public List<Pay> getPayList(Integer status) {
        return payRepository.getByStatusIs(status);
    }

    @Override
    public List<Pay> getNotPayList() {
        return payRepository.getByStatusIsNotAndStatusIsNot(0, 1);
    }

    @Override
    public Pay getPay(String id) {
        Pay pay = payRepository.findById(id).get();
        if (pay != null) {
            pay.setTime(StringUtils.getTimeStamp(pay.getCreateDate()));
            return pay;
        }
        return null;
    }

    @Override
    public int addPay(Pay pay) {
        pay.setId(UUID.randomUUID().toString());
        pay.setCreateDate(new Date());
        pay.setStatus(0);
        payRepository.save(pay);
        return 1;
    }

    @Override
    public int updatePay(Pay pay) {
        pay.setUpdateDate(new Date());
        payRepository.saveAndFlush(pay);
        return 1;
    }

    @Override
    public int changePayStatus(String id, Integer status) {
        Pay pay = getPay(id);
        if (pay != null) {
            pay.setStatus(status);
            pay.setUpdateDate(new Date());
            payRepository.saveAndFlush(pay);
            return 1;
        }
        return 0;
    }

    @Override
    public int delPay(String id) {
        payRepository.deleteById(id);
        return 1;
    }
}
