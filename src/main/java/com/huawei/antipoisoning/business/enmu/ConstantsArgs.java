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
     * 防投毒门禁详情访问地址。
     */
    public static final String MAJUN_POISON_INC = "/increment/antipoisoning/poisoningresult/";

    /**
     * mongodb 环境
     */
    public static final String DB_ENV = System.getenv("spring.data.mongodb.env");

    /**
     * 成功码。
     */
    public static int CODE_SUCCESS = 200;

    /**
     * 错误码。
     */
    public static int CODE_FAILED = 400;

    /**
     * openMajun projectName
     */
    public static final String OPEN_MAJUN = "openMajun";
}
