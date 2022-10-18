/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.entity.checkrule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * 新增自定义规则集实体类
 *
 * @author cqx
 * @since 2022/08/16 20:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleSetResult {
    @Field("_id")
    private String id;

    // 所属社区
    @Field("project_name")
    private String projectName;

    // 规则集id
    @Transient
    private String ruleSetId;

    // 规则集名称
    @Field("template_name")
    private String templateName;

    // 规则集语言
    @Field("language")
    private String language;

    // 是否基于（基于的规则集id）
    @Field("default_template_id")
    private String defaultTemplateId;

    // 规则id
    @Field("rule_ids")
    private List<String> ruleIds;

    // 创建时间
    @Field("template_create_time")
    private String templateCreateTime;

    // 创建人
    @Field("template_create_id")
    private String templateCreateId;

    // 创建人
    @Field("template_create_name")
    private String templateCreateName;

    // 是否默认规则集（自定义与默认 0:系统默认  1：自定义）
    @Field("default_template")
    private Integer defaultTemplate;

    @Transient
    private int ruleCount;

    // 是否正在使用
    @Transient
    private boolean isUsed = false;

    // 查询启用状态
    @Transient
    private String type;

    private Integer pageSize;

    private Integer pageNum;

    @Transient
    private long count;

    private String ruleName;
}

