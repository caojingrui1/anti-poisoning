/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.entity.checkrule;

import com.huawei.antipoisoning.common.util.annocation.ExcelAttribute;
import com.huawei.antipoisoning.common.util.annocation.ExcelColumnType;
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

    // 规则文件名称
    @Field("rule_name")
    private String ruleName;

    // 规则语言
    @ExcelAttribute(columnIndex = 0, columnType = ExcelColumnType.STRING)
    @Field("rule_language")
    private String ruleLanguage;

    // 规则id
    @ExcelAttribute(columnIndex = 1, columnType = ExcelColumnType.STRING)
    @Field("rule_id")
    private String ruleId;

    // 规则描述
    @ExcelAttribute(columnIndex = 2, columnType = ExcelColumnType.STRING)
    @Field("rule_desc")
    private String ruleDesc;

    // 可扫描的文件后缀
    @ExcelAttribute(columnIndex = 3, columnType = ExcelColumnType.STRING)
    @Field("file_ids")
    private String fileIds;

    // 正确示例
    @ExcelAttribute(columnIndex = 4, columnType = ExcelColumnType.STRING)
    @Field("right_example")
    private String rightExample;

    // 错误示例
    @ExcelAttribute(columnIndex = 5, columnType = ExcelColumnType.STRING)
    @Field("error_example")
    private String errorExample;

    // 规则建议
    @ExcelAttribute(columnIndex = 6, columnType = ExcelColumnType.STRING)
    @Field("revise_opinion")
    private String reviseOpinion;

    // 规则状态 0停用 1启用
    @ExcelAttribute(columnIndex = 7, columnType = ExcelColumnType.STRING)
    @Field("status")
    private String status;

    // 标签，区分规则大类
    @ExcelAttribute(columnIndex = 8, columnType = ExcelColumnType.STRING)
    @Field("tag")
    private String tag;

    // 对应修复指南章节名称
    @Field("link_name")
    private String linkName;

    /**
     * 更新时间
     */
    @Field("update_time")
    private String updateTime;

    /**
     * 创建时间
     */
    @Field("create_time")
    private String createTime;


    private Integer pageSize;

    private Integer pageNum;

    private long count;

    // 该条规则是否在规则集启用
    private String isUsed;
}
