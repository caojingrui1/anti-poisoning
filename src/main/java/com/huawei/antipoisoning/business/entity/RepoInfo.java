package com.huawei.antipoisoning.business.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author zhangshengjie
 */
@Data
public class RepoInfo {
    private String id;

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
    @Field("repo_branch_name")
    private String repoBranchName;

    /**
     * 仓的地址
     */
    @Field("repo_url")
    private String repoUrl;

    /**
     * 语言
     */
    private String language;

    /**
     * 一页的数量
     */
    private Integer pageSize = null;

    /**
     * 当前第几页
     */
    private Integer currentPage = null;
}