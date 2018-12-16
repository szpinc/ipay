package org.szpinc.pay.bean.dto;

import java.io.Serializable;


/**
 * 前后端交互数据标准
 *
 * @author GhostDog
 * @date 2017/08/24
 */
public class Result<T> implements Serializable {
    //成功状态
    private boolean success;
    //错误信息
    private String errorMessage;
    //时间戳
    private long dateTime = System.currentTimeMillis();
    //结果对象
    private T data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
