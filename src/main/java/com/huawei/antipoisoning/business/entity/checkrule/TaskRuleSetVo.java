/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.entity.checkrule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

/**
 * 语言实体
 *
 * @author zsn
 * @since 2021-11-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRuleSetVo {
    @MongoId
    private ObjectId id;

    @Field("repo_name_en")
    private String repoNameEn;

    @Field("project_name")
    private String projectName;

    @Field("anti_check_rules")
    private List<CheckRuleSet> antiCheckRules;
}
