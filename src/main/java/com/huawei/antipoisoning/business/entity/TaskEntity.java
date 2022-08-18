package com.huawei.antipoisoning.business.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * @author zhangshengjie
 */
@Data
@ToString
public class TaskEntity {
    /**
     * 扫描文件名
     */
    @Field("task_id")
    private String taskId;

    /**
     * 扫描文件名
     */
    @Field("scan_id")
    private String scanId;

    /**
     * 扫描文件名
     */
    @Field("community")
    private String community;

    /**
     * 扫描文件名
     */
    @Field("repo_name")
    private String repoName;

    /**
     * 扫描文件名
     */
    @Field("branch")
    private String branch;

    /**
     * 扫描文件名
     */
    @Field("repo_url")
    private String repoUrl;

    /**
     * 扫描文件名
     */
    @Field("execute_start_time")
    private String executeStartTime;

    /**
     * 扫描文件名
     */
    @Field("execute_end_time")
    private String executeEndTime;

    /**
     * 扫描文件名
     */
    @Field("create_time")
    private String createTime;

    /**
     * 扫描文件名
     */
    @Field("creator")
    private String creator;

    /**
     * 扫描文件名
     */
    @Field("report_id")
    private String reportId;

    /**
     * 扫描文件名
     */
    @Field("last_execute_start_time")
    private String lastExecuteStartTime;

    /**
     * 扫描文件名
     */
    @Field("last_execute_end_time")
    private String lastExecuteEndTime;
}
