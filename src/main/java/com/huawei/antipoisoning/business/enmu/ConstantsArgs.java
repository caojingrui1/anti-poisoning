/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.enmu;

/**
 * 常量参数。
 *
 * @since 2022-10-08
 * @author zyx
 */
public class ConstantsArgs {
    /**
     * majun访问url
     */
    public static final String MAJUN_URL = "https://majun.osinfra.cn";

    /**
     * majun-beta访问url
     */
    public static final String MAJUN_BETA_URL = "https://majun-beta.osinfra.cn";

    /**
     * mongodb 环境
     */
    public static final String DB_ENV = System.getenv("spring.data.mongodb.env");
}
