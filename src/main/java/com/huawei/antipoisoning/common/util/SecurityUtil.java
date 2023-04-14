/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.common.util;

import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.context.annotation.Configuration;

/**
 * 加密解密
 *
 * @since: 2022/6/1 13:56
 */
@Configuration
public class SecurityUtil {
    private static final String securityPass =
            System.getenv("jasypt.encryptor.password");

    /**
     * 解密
     *
     * @param data 待解密数据
     * @return 解密后的数据
     */
    public static String decrypt(String data){
        BasicTextEncryptor basicTextEncryptor=new BasicTextEncryptor();
        basicTextEncryptor.setPassword(securityPass);
        return basicTextEncryptor.decrypt(data);
    }

    /**
     * 加密
     *
     * @param data 待加密数据
     * @return 加密后的数据
     */
    public static String encrypt(String data){
        BasicTextEncryptor basicTextEncryptor=new BasicTextEncryptor();
        basicTextEncryptor.setPassword(securityPass);
        return basicTextEncryptor.encrypt(data);
    }
}
