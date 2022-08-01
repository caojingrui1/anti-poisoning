package com.huawei.antipoisoning.business.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
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
    @Field("status")
    private Boolean status;

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
    private String language;

    /**
     * 分支
     */
    private String branch;

    /**
     * 地址
     */
    @Field("repo_url")
    private String repoUrl;

    /**
     * 问题数
     */
    @Field("result_count")
    private Integer resultCount;

    /**
     * 问题详情列表
     */
    @Field("scan_result")
    private List<ResultEntity> scanResult;
}
