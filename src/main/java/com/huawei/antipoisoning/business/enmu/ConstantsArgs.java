/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.enmu;

import java.util.Arrays;
import java.util.List;

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
    public static final int CODE_SUCCESS = 200;

    /**
     * 错误码。
     */
    public static final int CODE_FAILED = 400;

    /**
     * openMajun projectName
     */
    public static final String OPEN_MAJUN = "openMajun";

    /**
     * 三大特殊社区
     */
    public static final List<String> SPECIAL_PRO = Arrays.asList("openeuler", "opengauss", "mindspore");

    /**
     * 错误输出线程名称
     */
    public static final String ERR_CONSUMER = "errConsumer";

    /**
     * 正常输出线程名称
     */
    public static final String OUTPUT_CONSUMER = "outputConsumer";

    /**
     * 增量标识
     */
    public static final String HARMONY_INC = "inc";

    /**
     * 全量标识
     */
    public static final String HARMONY_FULL = "full";
}
