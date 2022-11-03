/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.entity.pr;

import lombok.Data;

/**
 * pr门禁信息。
 *
 * @author zyx
 * @since 2022-09-28
 */
@Data
public class PullRequestInfo {
    private String projectName;
    private String repoName;
    private String pullNumber;
    private String accessToken;
    private String branch;
    private String target;
    private String pullInfo;
    private String user;
    private String password;
    private String version;
    private String gitUrl;
    private String mergeUrl;
    private String workspace;
    private String executorName;
    private String executorId;
}
