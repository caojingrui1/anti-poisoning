/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.entity.pr;

import lombok.Data;

/**
 * gitlab pr门禁信息，由jenkins任务传入。
 *
 * @author zyx
 * @since 2023-03-24
 */
@Data
public class GitlabPRInfo {
    private String prInfo;
    private String accessToken;
}
