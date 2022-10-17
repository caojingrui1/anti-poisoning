/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.entity.shield;

import lombok.Data;

/**
 * 查询report筛选栏实体类
 *
 * @author zhangshengnian zWX1067200
 * @since 2022/09/06 19:25
 */
@Data
public class PoisonReportModel {
    private String status;
    private String fileName;
    private String ruleName;
    private Integer total;
}
