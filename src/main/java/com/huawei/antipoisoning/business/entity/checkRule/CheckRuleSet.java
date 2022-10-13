/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.entity.checkRule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务规则集查询参数
 *
 * @author cqx
 * @since 2022/8/2510:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckRuleSet {
    private String ruleSetId;
    private String language;
}
