/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.entity.checkrule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 规则实体类
 *
 * @author cqx
 * @since 2022/08/16 20:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleModel {
    private String id;

    // 规则id
    @Field("rule_id")
    private String ruleId;

    // 规则文件名称
    @Field("rule_name")
    private String ruleName;

    // 规则语言
    @Field("rule_language")
    private String ruleLanguage;

    // 规则建议
    @Field("revise_opinion")
    private String reviseOpinion;

    // 可扫描的文件后缀
    @Field("file_ids")
    private String fileIds;

    // 正确示例
    @Field("right_example")
    private String rightExample;

    // 错误示例
    @Field("error_example")
    private String errorExample;

    // 规则描述
    @Field("rule_desc")
    private String ruleDesc;

    private Integer pageSize;

    private Integer pageNum;

    private long count;

    // 该条规则是否在规则集启用
    private String isUsed;
}
