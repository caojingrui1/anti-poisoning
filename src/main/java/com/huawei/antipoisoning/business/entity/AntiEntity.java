/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * vms:外部漏洞源-漏洞修复版本
 *
 * @author zhangshengjie
 * @since: 2022-05-20 11:35
 */
@Data
@ToString
public class AntiEntity {
    /**
     * 扫描任务唯一代码
     */
    @Field("scan_id")
    private String scanId;

    /**
     * 是否下载
     */
    @Field("is_downloaded")
    private Boolean isDownloaded;

    /**
     * 是否扫描
     */
    @Field("is_scan")
    private Boolean isScan;

    /**
     * 扫描是否成功状态
     */
    @Field("is_success")
    private Boolean isSuccess;

    /**
     * 扫描是否通过状态
     */
    @Field("is_pass")
    private Boolean isPass;

    /**
     * 提示信息
     */
    @Field("tips")
    private String tips;

    /**
     * 仓库名
     */
    @Field("repo_name")
    private String repoName;

    /**
     * 语言
     */
    @Field("language")
    private String language;

    /**
     * 分支
     */
    @Field("branch")
    private String branch;

    /**
     * 地址
     */
    @Field("repo_url")
    private String repoUrl;

    /**
     * 仓库id
     */
    @Field("branch_repository_id")
    private String branchRepositoryId;

    /**
     * 问题数
     */
    @Field("result_count")
    private Integer resultCount;

    /**
     * 已解决问题数
     */
    @Field("solve_Count")
    private Integer solveCount;

    /**
     * 未解决问题数
     */
    @Field("issue_count")
    private Integer issueCount;

    /**
     * 问题详情列表
     */
    @Field("scan_result")
    private List<ResultEntity> scanResult;

    /**
     * 社区
     */
    @Field("project_name")
    private String projectName;

    /**
     * 社区
     */
    @Field("creator")
    private String creator;

    /**
     * 社区
     */
    @Field("create_time")
    private String createTime;

    /**
     * 耗时
     */
    @Field("time_consuming")
    private String timeConsuming;

    /**
     * 规则集名称
     */
    @Field("rules_name")
    private String rulesName;

    /**
     * 执行人id（最后一次执行人）
     */
    @Field("executor_id")
    private String executorId;

    /**
     * 执行人名称
     */
    @Field("executor_name")
    private String executorName;
}
