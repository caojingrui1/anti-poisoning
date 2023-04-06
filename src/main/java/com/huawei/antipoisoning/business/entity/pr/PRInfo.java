/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.entity.pr;

import lombok.Data;

/**
 * pr门禁信息，由jenkins任务传入。
 *
 * @author zyx
 * @since 2022-09-28
 */
@Data
public class PRInfo {
    private String projectName;
    private String repoName;
    private String pullNumber;
    private String accessToken;
    private String apiToken;
}
