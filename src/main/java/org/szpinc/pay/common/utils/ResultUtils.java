package org.szpinc.pay.common.utils;


import org.springframework.stereotype.Component;
import org.szpinc.pay.bean.dto.Result;

/**
 * 前后端交互数据标准工具类
 *
 * @author GhostDog
 */
@Component
public class ResultUtils<T> {

    private Result<T> result;

    public ResultUtils() {
        result = new Result<T>();
        result.setSuccess(true);
        result.setErrorMessage(null);
    }

    public Result<T> setData(T data) {
        this.result.setData(data);
        return this.result;
    }

    public Result<T> setErrorMsg(String errorMsg) {
        this.result = new Result<T>();
        this.result.setSuccess(false);
        this.result.setErrorMessage(errorMsg);
        return this.result;
    }
}