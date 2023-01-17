/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 线程实现类。
 *
 * @since 2022-07-28
 * @author prk
 */
class InputStreamRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputStreamRunnable.class);
    BufferedReader bReader = null;

    /**
     * 实现方法。
     *
     * @param is 输入
     * @param _type 类型
     */
    public InputStreamRunnable(InputStream is, String _type) {
        try {
            bReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(is), "UTF-8"));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    @Override
    public void run() {
        String line;
        int num = 0;
        try {
            while ((line = bReader.readLine()) != null) {
                LOGGER.info("---->{}", String.format("%02d", num++) + line);
            }
            bReader.close();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
    }
}
