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
    private String branch;
    private String target;
    private String pullInfo;
    private String user;
    private String password;
    private String version;
    private String gitUrl;
    private String workspace;
}
