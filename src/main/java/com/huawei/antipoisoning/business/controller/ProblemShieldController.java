/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.controller;

import com.huawei.antipoisoning.business.entity.shield.ParamModel;
import com.huawei.antipoisoning.business.service.ProblemShieldService;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 防投毒问题查询/屏蔽接口
 *
 * @author zhangshengnian zWX1067200
 * @since 2022/09/23 14:37
 */

@RestController
@RequestMapping("/shield")
public class ProblemShieldController {
    @Autowired
    private ProblemShieldService problemShieldService;

    /**
     * 获取我的申请/待我审批数量
     *
     * @param userId 用户id
     * @param scanId 扫描id
     * @return MultiResponse
     */
    @GetMapping("/applyAndAuditNumber")
    public MultiResponse applyAndAuditNumber(@RequestParam("userId") String userId,
            @RequestParam("scanId") String scanId) {
        return problemShieldService.applyAndAuditNumber(userId, scanId);
    }

    /**
     * 获取我的申请/待我审批门禁问题数量
     *
     * @param userId 用户id
     * @param scanId 扫描id
     * @return MultiResponse
     */
    @GetMapping("/apply-audit-pr-number")
    public MultiResponse applyAndAuditPRNumber(@RequestParam("userId") String userId,
            @RequestParam("scanId") String scanId) {
        return problemShieldService.applyAndAuditPRNumber(userId, scanId);
    }

    /**
     * 审核通过问题撤销
     *
     * @param paramModel 待撤销问题列表
     * @return MultiResponse
     */
    @PostMapping("/auditPassRevoke")
    public MultiResponse auditPassRevoke(@RequestBody ParamModel paramModel) {
        return problemShieldService.auditPassRevoke(paramModel);
    }

    /**
     * 门禁审核通过问题撤销
     *
     * @param paramModel 待撤销问题列表
     * @return MultiResponse
     */
    @PostMapping("/audit-pr-revoke")
    public MultiResponse auditPassPRRevoke(@RequestBody ParamModel paramModel) {
        return problemShieldService.auditPassPRRevoke(paramModel);
    }

    /**
     * 查询防投毒问题详情
     *
     * @param scanId     唯一id
     * @param userId     用户id
     * @param paramModel 查询参数体
     * @return MultiResponse
     */
    @PostMapping("/getResultDetail")
    public MultiResponse getResultDetail(@RequestParam String scanId, @RequestParam String userId,
            @RequestBody ParamModel paramModel) {
        return problemShieldService.getResultDetail(scanId, userId, paramModel);
    }

    /**
     * 查询防投毒问题详情
     *
     * @param scanId     唯一id
     * @param userId     用户id
     * @param paramModel 查询参数体
     * @return MultiResponse
     */
    @PostMapping("/get-pr-result-details")
    public MultiResponse getPRResultDetail(@RequestParam String scanId, @RequestParam String userId,
                                         @RequestBody ParamModel paramModel) {
        return problemShieldService.getPRResultDetail(scanId, userId, paramModel);
    }

    /**
     * 差选scanDetail左侧的筛选列
     *
     * @param userId     用户id
     * @param paramModel 参数体
     * @return MultiResponse
     */
    @PostMapping("/getScanReport")
    public MultiResponse getScanReport(@RequestParam String userId, @RequestBody ParamModel paramModel) {
        return problemShieldService.getScanReport(userId, null, paramModel);
    }


    /**
     * 查询门禁scanDetail左侧的筛选列
     *
     * @param userId     用户id
     * @param paramModel 参数体
     * @return MultiResponse
     */
    @PostMapping("/get-scan-pr-report")
    public MultiResponse getScanPRReport(@RequestParam String userId, @RequestBody ParamModel paramModel) {
        return problemShieldService.getScanReport(userId, "pr", paramModel);
    }

    /**
     * 查询防投毒所有扫描情况
     *
     * @param paramModel 参数体
     * @return MultiResponse
     */
    @PostMapping("/getScanResult")
    public MultiResponse getScanResult(@RequestBody ParamModel paramModel) {
        return problemShieldService.getScanResult(paramModel);
    }

    /**
     * 查询防投毒门禁扫描情况
     *
     * @param paramModel 参数体
     * @return MultiResponse
     */
    @PostMapping("/get-scan-pr-result")
    public MultiResponse getScanPRResult(@RequestBody ParamModel paramModel) {
        return problemShieldService.getScanPRResult(paramModel);
    }

    /**
     * 查询防投毒门禁扫描情况(根据社区PR分组）
     *
     * @param paramModel 参数体
     * @return MultiResponse
     */
    @PostMapping("/get-scan-pr-result-group")
    public MultiResponse getScanPRResultGroup(@RequestBody ParamModel paramModel) {
        return problemShieldService.getScanPRResultGroup(paramModel);
    }

    /**
     * 防投毒问题屏蔽申请
     *
     * @param userId     码云id
     * @param login      码云唯一标识
     * @param paramModel 屏蔽参数体
     * @return MultiResponse
     */
    @PostMapping("/poisonApply")
    public MultiResponse poisonApply(@RequestParam String userId,
            @RequestParam String login, @RequestBody ParamModel paramModel) {
        return problemShieldService.poisonApply(userId, login, paramModel);
    }

    /**
     * 防投毒门禁问题屏蔽申请
     *
     * @param userId     码云id
     * @param login      码云唯一标识
     * @param paramModel 屏蔽参数体
     * @return MultiResponse
     */
    @PostMapping("/poison-pr-apply")
    public MultiResponse poisonPRApply(@RequestParam String userId,
            @RequestParam String login, @RequestBody ParamModel paramModel) {
        return problemShieldService.poisonPRApply(userId, login, paramModel);
    }

    /**
     * 待审核问题的审批
     *
     * @param paramModel 审批参数体
     * @return MultiResponse
     */
    @PostMapping("/problemAudit")
    public MultiResponse problemAudit(@RequestBody ParamModel paramModel) {
        return problemShieldService.problemAudit(paramModel);
    }

    /**
     * 待审核门禁问题的审批
     *
     * @param paramModel 审批参数体
     * @return MultiResponse
     */
    @PostMapping("/problem-pr-audit")
    public MultiResponse problemPRAudit(@RequestBody ParamModel paramModel) {
        return problemShieldService.problemPRAudit(paramModel);
    }

    /**
     * 撤销审核申请数据
     *
     * @param paramModel 撤销审核申请数据参数
     * @return MultiResponse
     */
    @PostMapping("/problemRevoke")
    public MultiResponse problemRevoke(@RequestBody ParamModel paramModel) {
        return problemShieldService.problemRevoke(paramModel);
    }

    /**
     * 撤销门禁审核申请数据
     *
     * @param paramModel 撤销审核申请数据参数
     * @return MultiResponse
     */
    @PostMapping("/problem-pr-revoke")
    public MultiResponse problemPRRevoke(@RequestBody ParamModel paramModel) {
        return problemShieldService.problemPRRevoke(paramModel);
    }

    /**
     * 待审核问题转审
     *
     * @param paramModel 请求参数体
     * @return MultiResponse
     */
    @PostMapping("/shieldReferral")
    public MultiResponse shieldReferral(@RequestBody ParamModel paramModel) {
        return problemShieldService.shieldReferral(paramModel);
    }

    /**
     * 门禁待审核问题转审
     *
     * @param paramModel 请求参数体
     * @return MultiResponse
     */
    @PostMapping("/shield-pr-referral")
    public MultiResponse shieldPRReferral(@RequestBody ParamModel paramModel) {
        return problemShieldService.shieldPRReferral(paramModel);
    }

    /**
     * 提供下拉列表
     *
     * @return MultiResponse
     */
    @RequestMapping("/getPoisoningSelect")
    public MultiResponse getPoisoningSelect() {
        return problemShieldService.getPoisoningSelect();
    }

    /**
     * 提供门禁扫描下拉列表
     *
     * @return MultiResponse
     */
    @RequestMapping("/get-pr-poisoning-select")
    public MultiResponse getPRPoisoningSelect() {
        return problemShieldService.getPRPoisoningSelect();
    }
}
