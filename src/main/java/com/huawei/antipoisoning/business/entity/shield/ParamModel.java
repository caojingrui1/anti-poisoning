/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.entity.shield;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 屏蔽相关问题请求参数
 *
 * @author zhangshengnian zWX1067200
 * @since 2022/09/23 16:30
 */
@Data
@ToString
public class ParamModel {
    private List<String> detailsId;
    private String applicant;
    private String status;
    private String reviewerStatus;
    private String projectName;
    private String repoName;
    private String branch;
    private String prNumber;
    private String startTime;
    private String endTime;
    private String fileName;
    private String ruleName;
    private Integer pageNum;
    private Integer pageSize;
    private String scanId;
    private String user;
    private String shieldType;
    private String reason;
    private String auditResult;
    private String auditOpinion;
}
