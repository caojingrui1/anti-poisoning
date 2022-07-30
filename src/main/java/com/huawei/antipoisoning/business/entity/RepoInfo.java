package com.huawei.antipoisoning.business.entity;

import lombok.Data;

@Data
public class RepoInfo {
    private String community;
    private String repoName;
    private String branch;
    private String repoUrl;
    private String language;
}
