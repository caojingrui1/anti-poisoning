/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.common.util;

import org.jasypt.util.text.BasicTextEncryptor;

/**
 * 加密解密
 *
 * @since: 2022/6/1 13:56
 */
public class SecurityUtil {

    /**
     * 解密
     *
     * @param data 待解密数据
     * @return 解密后的数据
     */
    public static String decrypt(String data){
        BasicTextEncryptor basicTextEncryptor=new BasicTextEncryptor();
        basicTextEncryptor.setPassword(System.getProperty("jasypt.encryptor.password"));
        String decryptData=basicTextEncryptor.decrypt(data);
        return decryptData;
    };

    /**
     * 加密
     *
     * @param data 待加密数据
     * @return 加密后的数据
     */
    public static String encrypt(String data){
        BasicTextEncryptor basicTextEncryptor=new BasicTextEncryptor();
        basicTextEncryptor.setPassword(System.getProperty("jasypt.encryptor.password"));
        String encryptData=basicTextEncryptor.encrypt(data);
        return encryptData;
    }
}
