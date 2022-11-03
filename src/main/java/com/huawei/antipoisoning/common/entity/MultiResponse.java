/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.common.entity;

import org.apache.commons.lang.StringUtils;

/**
 * 接口返回响应类。
 *
 * @since 2022-07-30
 * @author zyx
 */
public class MultiResponse {
    /**
     * 编码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private Object result;

    public MultiResponse result(Object result) {
        this.result = result;
        return this;
    }

    public MultiResponse message(String message) {
        this.message = message;
        return this;
    }

    public MultiResponse code(Integer code) {
        this.code = code;
        return this;
    }


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

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "MultiResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
    }

    /**
     * 成功
     *
     * @param code    编码
     * @param message 信息
     * @param result  返回值
     * @param <T>     类型
     * @return <T>
     */
    public static <T> MultiResponse success(Integer code, String message, T result) {
        String msg = StringUtils.isBlank(message) ? "success" : message;
        return new MultiResponse().code(200).message(msg).result(result);
    }
    /**

     * 成功
     *
     * @param code    编码
     * @param message 信息
     * @return MultiResponse
     */
    public static MultiResponse success(Integer code, String message) {
        String msg = StringUtils.isBlank(message) ? "success" : message;
        return new MultiResponse().code(200).message(msg);
    }

    /**
     * 失败
     *
     * @param code    编码
     * @param message 信息
     * @return MultiResponse
     */
    public static MultiResponse error(Integer code, String message) {
        String msg = StringUtils.isBlank(message) ? "error" : message;
        return new MultiResponse().code(code).message(msg);
    }
}

