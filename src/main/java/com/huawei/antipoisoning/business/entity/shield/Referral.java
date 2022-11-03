/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.entity.shield;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 转审人信息
 *
 * @author zhangshengnian
 * @since 2022/03/11 15:02
 */
@Data
public class Referral {
    /**
     * 码云唯一标识
     */
    private String userName;

    /**
     * 码云id
     */
    private String userId;

    /**
     * 转审时间
     */
    private LocalDateTime dateTime;
}
