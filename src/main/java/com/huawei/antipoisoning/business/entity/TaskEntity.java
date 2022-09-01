package com.huawei.antipoisoning.business.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author zhangshengjie
 */
@Data
@ToString
public class TaskEntity {
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
     * 创建者
     */
    @Field("creator")
    private String creator;

    /**
     * 扫描文件名
     */
    @Field("report_id")
    private String reportId;

    /**
     * 上一次开始执行时间
     */
    @Field("last_execute_start_time")
    private String lastExecuteStartTime;

    /**
     * 上一次结束执行时间
     */
    @Field("last_execute_end_time")
    private String lastExecuteEndTime;

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
    @Field("status")
    private Boolean status;

    /**
     * 提示信息
     */
    @Field("tips")
    private String tips;

    /**
     * 语言
     */
    private String language;

    /**
     * 问题数
     */
    @Field("result_count")
    private Integer resultCount;

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
}
