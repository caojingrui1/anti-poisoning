/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.service;

import com.huawei.antipoisoning.business.entity.shield.ParamModel;
import com.huawei.antipoisoning.common.entity.MultiResponse;

/**
 * @author zhangshengnian zWX1067200
 * @since 2022/09/23 15:46
 */
public interface ProblemShieldService {
    /**
     * 查询我的申请和待我审批数量
     *
     * @param userId 用户id
     * @param scanId 扫描id
     * @return
     */
    MultiResponse applyAndAuditNumber(String userId, String scanId);

    /**
     * 审核通过问题撤销
     *
     * @param paramModel 待撤销问题列表
     * @return MultiResponse
     */
    MultiResponse auditPassRevoke(ParamModel paramModel);

    /**
     * 查询防投毒问题详情
     *
     * @param scanId     唯一id
     * @param userId     用户id
     * @param paramModel 查询参数体
     * @return MultiResponse
     */
    MultiResponse getResultDetail(String scanId, String userId, ParamModel paramModel);

    /**
     * 获取扫描结果报告。
     *
     * @param userId 用户ID
     * @param paramModel 参数
     * @return MultiResponse
     */
    MultiResponse getScanReport(String userId, ParamModel paramModel);

    /**
     * 查询防投毒所有扫描情况
     *
     * @param paramModel 参数体
     * @return MultiResponse
     */
    MultiResponse getScanResult(ParamModel paramModel);

    /**
     * 防投毒问题屏蔽申请
     *
     * @param userId     码云id
     * @param login      码云唯一标识
     * @param paramModel 屏蔽参数体
     * @return MultiResponse
     */
    MultiResponse poisonApply(String userId, String login, ParamModel paramModel);

    /**
     * 待审核问题的审批
     *
     * @param paramModel 审批参数体
     * @return MultiResponse
     */
    MultiResponse problemAudit(ParamModel paramModel);

    /**
     * 撤销审核申请数据
     *
     * @param paramModel 撤销审核申请数据参数
     * @return MultiResponse
     */
    MultiResponse problemRevoke(ParamModel paramModel);

    /**
     * 屏蔽。
     *
     * @param paramModel 参数
     * @return MultiResponse
     */
    MultiResponse shieldReferral(ParamModel paramModel);

    /**
     * 提供下拉列表
     *
     * @return MultiResponse
     */
    MultiResponse getPoisoningSelect();
}
