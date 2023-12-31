/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.entity.checkrule;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 规则集返回实体类封装
 *
 * @author cqx
 * @since 2022-08-24
 */
@Data
@AllArgsConstructor
public class RuleResultDetailsVo {
    private int count;

    private int enableCount;

    private List<RuleModel> ruleModels;
}
