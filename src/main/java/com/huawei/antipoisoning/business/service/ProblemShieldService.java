/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.service;

import com.huawei.antipoisoning.business.entity.shield.ParamModel;
import com.huawei.antipoisoning.business.entity.shield.QueryShieldModel;
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
     * @ MultiResponse
     */
    MultiResponse applyAndAuditNumber(String userId, String scanId);

    /**
     * 查询我的申请和待我审批门禁问题数量
     *
     * @param userId 用户id
     * @param scanId 扫描id
     * @return MultiResponse
     */
    MultiResponse applyAndAuditPRNumber(String userId, String scanId);

    /**
     * 审核通过问题撤销
     *
     * @param paramModel 待撤销问题列表
     * @return MultiResponse
     */
    MultiResponse auditPassRevoke(ParamModel paramModel);

    /**
     * 门禁审核通过问题撤销
     *
     * @param paramModel 待撤销问题列表
     * @return MultiResponse
     */
    MultiResponse auditPassPRRevoke(ParamModel paramModel);

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
     * 查询防投毒门禁扫描问题详情
     *
     * @param scanId     唯一id
     * @param userId     用户id
     * @param paramModel 查询参数体
     * @return MultiResponse
     */
    MultiResponse getPRResultDetail(String scanId, String userId, ParamModel paramModel);

    /**
     * 获取扫描结果报告。
     *
     * @param userId 用户ID
     * @param type 查询类型
     * @param paramModel 参数
     * @return MultiResponse
     */
    MultiResponse getScanReport(String userId, String type, ParamModel paramModel);

    /**
     * 查询防投毒所有扫描情况
     *
     * @param paramModel 参数体
     * @return MultiResponse
     */
    MultiResponse getScanResult(ParamModel paramModel);

    /**
     * 查询防投毒门禁扫描情况
     *
     * @param paramModel 参数体
     * @return MultiResponse
     */
    MultiResponse getScanPRResult(ParamModel paramModel);

    /**
     * 查询防投毒门禁扫描情况(根据社区PR分组）
     *
     * @param paramModel 参数体
     * @return MultiResponse
     */
    MultiResponse getScanPRResultGroup(ParamModel paramModel);


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
     * 防投毒门禁问题屏蔽申请
     *
     * @param userId     码云id
     * @param login      码云唯一标识
     * @param paramModel 屏蔽参数体
     * @return MultiResponse
     */
    MultiResponse poisonPRApply(String userId, String login, ParamModel paramModel);

    /**
     * 待审核问题的审批
     *
     * @param paramModel 审批参数体
     * @return MultiResponse
     */
    MultiResponse problemAudit(ParamModel paramModel);

    /**
     * 门禁待审核问题的审批
     *
     * @param paramModel 审批参数体
     * @return MultiResponse
     */
    MultiResponse problemPRAudit(ParamModel paramModel);

    /**
     * 撤销审核申请数据
     *
     * @param paramModel 撤销审核申请数据参数
     * @return MultiResponse
     */
    MultiResponse problemRevoke(ParamModel paramModel);

    /**
     * 撤销门禁审核申请数据
     *
     * @param paramModel 撤销审核申请数据参数
     * @return MultiResponse
     */
    MultiResponse problemPRRevoke(ParamModel paramModel);

    /**
     * 屏蔽。
     *
     * @param paramModel 参数
     * @return MultiResponse
     */
    MultiResponse shieldReferral(ParamModel paramModel);

    /**
     * 屏蔽门禁扫描问题。
     *
     * @param paramModel 参数
     * @return MultiResponse
     */
    MultiResponse shieldPRReferral(ParamModel paramModel);

    /**
     * 提供下拉列表
     *
     * @return MultiResponse
     */
    MultiResponse getPoisoningSelect();

    /**
     * 提供门禁下拉列表
     *
     * @return MultiResponse
     */
    MultiResponse getPRPoisoningSelect();

    /**
     * 获取防投毒屏蔽详情查询
     *
     * @param queryShieldModel 查询参数体
     * @return MultiResponse
     */
    MultiResponse poisonShieldDetail(QueryShieldModel queryShieldModel);

    /**
     * 防投毒屏蔽类型分布
     *
     * @param queryShieldModel 方法参数请求体
     * @return MultiResponse
     */
    MultiResponse poisonShieldTypeMap(QueryShieldModel queryShieldModel);

    /**
     * 获取排名前十五的防投毒屏蔽规则
     *
     * @param queryShieldModel 方法参数请求体
     * @return MultiResponse
     */
    MultiResponse getPoisonTopFifteen(QueryShieldModel queryShieldModel);

    MultiResponse getScanResultByScanId(String scanId);

    MultiResponse getPRScanResultByScanId(String scanId);

}
