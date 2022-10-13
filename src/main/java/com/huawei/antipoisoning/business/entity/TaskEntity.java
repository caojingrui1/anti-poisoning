/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.entity;

import com.huawei.antipoisoning.business.enmu.CollectionTableName;
import com.huawei.antipoisoning.business.entity.checkRule.TaskRuleSetVo;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author zhangshengjie
 */
@Data
@ToString
@Document(collection = CollectionTableName.POISON_VERSION_TASK)
public class TaskEntity {
    /**
     * id
     */
    private String id;
    /**
     * 任务id
     */
    @Field("task_id")
    private String taskId;

    /**
     * 唯一性id
     */
    @Field("scan_id")
    private String scanId;

    /**
     * 社区名称
     */
    @Field("project_name")
    private String projectName;

    /**
     * 仓库
     */
    @Field("repo_name")
    private String repoName;

    /**
     * 分支
     */
    @Field("branch")
    private String branch;

    /**
     * 代码路径
     */
    @Field("repo_url")
    private String repoUrl;

    /**
     * 开始时间
     */
    @Field("execute_start_time")
    private String executeStartTime;

    /**
     * 结束时间
     */
    @Field("execute_end_time")
    private String executeEndTime;

    /**
     * 创建时间
     */
    @Field("create_time")
    private String createTime;

    /**
     * 扫描文件名
     */
    @Field("report_id")
    private String reportId;

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
     * 提示信息
     */
    @Field("tips")
    private String tips;

    /**
     * 语言
     */
    @Field("language")
    private String language;

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
     * 总耗时
     */
    @Field("time_consuming")
    private String timeConsuming;

    /**
     * 任务耗时
     */
    @Field("task_consuming")
    private String taskConsuming;

    /**
     * 下载耗时
     */
    @Field("download_consuming")
    private String downloadConsuming;

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

    /**
     * 该任务所用的规则集参数体
     */
    @Transient
    private TaskRuleSetVo taskRuleSetVo;

    /**
     * 仓库id
     */
    @Transient
    private String branchRepositoryId;

    /**
     * 本次执行日志
     */
    @Field("logs")
    private String logs;

    /**
     * 执行状态
     */
    @Field("execution_status")
    private Integer executionStatus;

    @Transient
    private Integer pageNum;

    @Transient
    private Integer pageSize;
}
