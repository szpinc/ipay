package org.szpinc.pay.common.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.szpinc.pay.controller.PayController;

import java.util.concurrent.TimeUnit;

/**
 * @author GhostDog
 */
@Component
public class RedisUtils {

    private final Logger LOG = LoggerFactory.getLogger(RedisUtils.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    public String get(String key) {
        String temp = null;
        try {
            temp = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("redis取值异常" , e);
            }
        }
        return temp;
    }

    public void set (String key, String value, Long time, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key,value,time,timeUnit);
        }catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("redis异常",e);
            }
        }
    }

}
