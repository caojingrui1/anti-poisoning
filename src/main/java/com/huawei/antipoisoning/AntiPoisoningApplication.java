/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 防投毒服务启动类。
 *
 * @since 2022-07-30
 * @author zyx
 */
@SpringBootApplication
@EnableScheduling
public class AntiPoisoningApplication {
    /**
     * 主方法。
     *
     * @param args 参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AntiPoisoningApplication.class, args);
    }
}
