package com.huawei.antipoisoning.business.entity;

import lombok.Data;

/**
 * @author zhangshengjie
 */
@Data
public class RepoInfo {
    /**
     * 社区
     */
    private String community;

    /**
     * 仓名
     */
    private String repoName;

    /**
     * 分支
     */
    private String branch;

    /**
     * 仓的地址
     */
    private String repoUrl;

    /**
     * 语言
     */
    private String language;

    /**
     * 规则集名称
     */
    private String rulesName;

    /**
     * 一页的数量
     */
    private Integer pageSize = null;

    /**
     * 当前第几页
     */
    private Integer currentPage = null;
}
