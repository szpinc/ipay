package org.szpinc.pay.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtils {

    private Logger logger;

    private LogUtils (Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public static LogUtils getLogger (Class<?> clazz) {
        return new LogUtils(clazz);
    }

    public void info (String msg) {
        if (logger.isInfoEnabled()) {
            logger.info(msg);
        }
    }




}
