package com.team.weup.util;

import androidx.annotation.NonNull;

/**
 * @author xieziwei99
 * 2020-02-08
 */
public class ReturnVO<T> {
    public static Integer OK = 200;
    public static Integer ERROR = 500;

    /**
     *用200表示成功，用500表示失败
     */
    private Integer code;

    private String message;

    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    @NonNull
    public String toString() {
        return "ReturnVO{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
