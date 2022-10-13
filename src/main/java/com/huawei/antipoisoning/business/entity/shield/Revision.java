/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.entity.shield;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题修改记录
 *
 * @since 2021-8-2
 */
@Data
public class Revision {
    /**
     * 审核状态 1:待审批 2:已审批 3:未审核前已撤销 4:审核通过后撤销
     */
    private String reviewerStatus;

    /**
     * 新的状态
     */
    private String newStatus;

    /**
     * 修改前的状态
     */
    private String previousStatus;

    /**
     * 修改人id
     */
    private String userId;

    /**
     * 修改人账号
     */
    private String userName;

    /**
     * 审核人id
     */
    private String reviewerId;

    /**
     * 审核人账号
     */
    private String reviewerName;

    /**
     * 申请日期
     */
    private LocalDateTime applyDate;

    /**
     * 审核日期
     */
    private LocalDateTime auditDate;

    /**
     * 屏蔽类型
     */
    private String shieldType;

    /**
     * 申请理由
     */
    private String reason;

    /**
     * 转审人信息
     */
    private List<Referral> referrals;

    /**
     * 审核结果 pass：通过，fail:不通过
     */
    private String auditResult;

    /**
     * 审核意见
     */
    private String auditOpinion;

    /**
     * 解决时间
     */
    private String solutionTime;

    /**
     * 撤销时间
     */
    private LocalDateTime revokeTime;
}
