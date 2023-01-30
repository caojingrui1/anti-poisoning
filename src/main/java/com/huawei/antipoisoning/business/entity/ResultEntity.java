/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.entity;

import com.huawei.antipoisoning.business.enmu.CollectionTableName;
import com.huawei.antipoisoning.business.entity.shield.Revision;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

/**
 * 扫描任务列表实体类。
 *
 * @since 2022-09-03
 * @author zhangshengjie
 */
@Data
@ToString
@Document(collection = CollectionTableName.SCAN_RESULT_DETAILS)
public class ResultEntity {
    /**
     * id
     */
    private String id;

    /**
     * 任务ID
     */
    @Field("task_id")
    private String taskId;

    /**
     * 社区
     */
    @Field("project_name")
    private String projectName;

    /**
     * 仓名
     */
    @Field("repo_name")
    private String repoName;

    /**
     * 分支
     */
    @Field("branch")
    private String branch;

    /**
     * 问题状态（0：未屏蔽 1：屏蔽中 2：已屏蔽）
     */
    @Field("status")
    private String status;

    /**
     * id
     */
    @Field("scan_id")
    private String scanId;

    /**
     * 扫描文件名
     */
    @Field("suspicious_file_name")
    private String suspiciousFileName;

    /**
     * 规则名称
     */
    @Field("rule_name")
    private String ruleName;

    /**
     * 检查代码行+原文
     */
    @Field("check_result")
    private String checkResult;

    /**
     * 代码上下文+问题行高亮
     */
    @Field("check_result_v2")
    private String checkResultV2;

    /**
     * 问题关键字
     */
    @Field("key_log_info")
    private String keyLogInfo;

    /**
     * 问题内容
     */
    @Field("suggestion")
    private String suggestion;

    /**
     * 文件hash
     */
    @Field("hash")
    private String hash;

    private Revision revision;

    /**
     * 关联问题详情id
     */
    @Field("detail_id")
    private String detailId;

    private Map link;

    /**
     * 防投毒扫描结果实体类
     */
    private AntiEntity scanResult;

    /**
     * 文件相对路径
     */
    private String fileUrl;
}
