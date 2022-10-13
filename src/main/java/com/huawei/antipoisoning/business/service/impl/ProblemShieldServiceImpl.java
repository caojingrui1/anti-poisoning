/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.service.impl;

import com.huawei.antipoisoning.business.entity.ResultEntity;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.entity.shield.ParamModel;
import com.huawei.antipoisoning.business.entity.shield.PoisonReportModel;
import com.huawei.antipoisoning.business.entity.shield.Referral;
import com.huawei.antipoisoning.business.entity.shield.Revision;
import com.huawei.antipoisoning.business.operation.ScanResultDetailOperation;
import com.huawei.antipoisoning.business.operation.ShieldResultDetailOperation;
import com.huawei.antipoisoning.business.service.ProblemShieldService;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhangshengnian zWX1067200
 * @since 2022/09/23 15:47
 */
@Component
public class ProblemShieldServiceImpl implements ProblemShieldService {
    @Autowired
    private ShieldResultDetailOperation shieldResultDetailOperation;
    @Autowired
    private ScanResultDetailOperation scanResultDetailOperation;

    /**
     * 查询我的申请和待我审批数量
     *
     * @param userId 用户id
     * @param scanId 扫描id
     * @return
     */
    @Override
    public MultiResponse applyAndAuditNumber(String userId, String scanId) {
        Map<String, Long> result = shieldResultDetailOperation.applyAndAuditNumber(userId, scanId);
        return new MultiResponse().code(200).result(result);
    }

    /**
     * 审核通过问题撤销
     *
     * @param paramModel 待撤销问题列表
     * @return MultiResponse
     */
    @Override
    public MultiResponse auditPassRevoke(ParamModel paramModel) {
        revoke(paramModel.getDetailsId(), "4");
        return new MultiResponse().code(200).message("success");
    }

    /**
     * 查询防投毒问题详情
     *
     * @param scanId     唯一id
     * @param userId     用户id
     * @param paramModel 查询参数体
     * @return MultiResponse
     */
    @Override
    public MultiResponse getResultDetail(String scanId, String userId, ParamModel paramModel) {
        List<ResultEntity> resultEntities = scanResultDetailOperation.getResultDetail(scanId, userId, paramModel);
        paramModel.setPageNum(null);
        paramModel.setPageSize(null);
        int count = scanResultDetailOperation.getResultDetail(scanId, userId, paramModel).size();
        Map<String, Object> result = new HashMap<>(2);
        result.put("count", count);
        result.put("data", resultEntities);
        return new MultiResponse().code(200).result(result);
    }

    @Override
    public MultiResponse getScanReport(String userId, ParamModel paramModel) {
        List<PoisonReportModel> resultEntityList = scanResultDetailOperation.getGroupResult(userId, paramModel);
        Map<String, Integer> fileNameReport = new LinkedHashMap<>(); // 文件
        Map<String, Integer> ruleNameReport = new LinkedHashMap<>(); // 规则
        Map<String, Integer> statusReport = new HashMap();
        // 根据状态分类统计
        statusReport = resultEntityList.stream().collect(Collectors.groupingBy(PoisonReportModel::getStatus,
                Collectors.summingInt(PoisonReportModel::getTotal)));
        // 根据文件名统计
        resultEntityList.stream().collect(Collectors.groupingBy(PoisonReportModel::getFileName,
                Collectors.summingInt(PoisonReportModel::getTotal)))
                .entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(value -> fileNameReport.put(value.getKey(), value.getValue()));
        // 根据规则统计
        resultEntityList.stream().collect(Collectors.groupingBy(PoisonReportModel::getRuleName,
                Collectors.summingInt(PoisonReportModel::getTotal)))
                .entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(value -> ruleNameReport.put(value.getKey(), value.getValue()));
        HashMap<String, Integer> ruleReport = new LinkedHashMap<>();
        Set<String> rule = ruleNameReport.keySet();
        for (String ruleName : rule) {
            Map map = scanResultDetailOperation.findRuleByName(ruleName);
            map.get("rule_desc");
            ruleReport.put(map.get("rule_desc").toString(), ruleNameReport.get(ruleName));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("statusReport", statusReport);
        result.put("fileNameReport", fileNameReport);
        result.put("ruleNameReport", ruleReport);
        return new MultiResponse().code(200).result(result);
    }

    /**
     * 查询防投毒所有扫描情况
     *
     * @param paramModel 参数体
     * @return MultiResponse
     */
    @Override
    public MultiResponse getScanResult(ParamModel paramModel) {
        List<TaskEntity> taskEntityList = scanResultDetailOperation.getScanResult(paramModel);
        paramModel.setPageNum(null);
        paramModel.setPageSize(null);
        int count = scanResultDetailOperation.getScanResult(paramModel).size();
        Map<String, Object> result = new HashMap<>(2);
        result.put("count", count);
        result.put("data", taskEntityList);
        return new MultiResponse().code(200).result(result);
    }

    /**
     * 防投毒问题屏蔽申请
     *
     * @param userId     码云id
     * @param login      码云唯一标识
     * @param paramModel 屏蔽参数体
     * @return MultiResponse
     */
    @Override
    public MultiResponse poisonApply(String userId, String login, ParamModel paramModel) {
        List<ResultEntity> resultEntities =
                scanResultDetailOperation.findScanResultDetailById(paramModel.getDetailsId());
        String[] name = paramModel.getUser().split(" ");
        Revision revision = new Revision();
        revision.setReviewerName(name[0]);
        revision.setReviewerId(name[1]);
        revision.setUserName(login);
        revision.setUserId(userId);
        // 1:有风险、2:无风险、3:未确认、4:误报
        revision.setShieldType(paramModel.getShieldType());
        revision.setReason(paramModel.getReason());
        revision.setApplyDate(LocalDateTime.now());
        revision.setReviewerStatus("1");
        for (ResultEntity resultEntity : resultEntities) {
            resultEntity.setStatus("1");
            scanResultDetailOperation.saveScanResultDetail(resultEntity);
            resultEntity.setRevision(revision);
            resultEntity.setDetailId(resultEntity.getId());
            resultEntity.setId(null);
        }
        shieldResultDetailOperation.saveShieldResult(resultEntities);
        return new MultiResponse().code(200).result(resultEntities.get(0));
    }

    /**
     * 待审核问题的审批
     *
     * @param paramModel 审批参数体
     * @return MultiResponse
     */
    @Override
    public MultiResponse problemAudit(ParamModel paramModel) {
        MultiResponse multiResponse = null;
        if ("fail".equals(paramModel.getAuditResult())) {
            multiResponse = shieldResultDetailOperation.problemAudit(paramModel, "0");
        } else {
            multiResponse = shieldResultDetailOperation.problemAudit(paramModel, "2");
        }
        return multiResponse;
    }

    /**
     * 撤销审核申请数据
     *
     * @param paramModel 撤销审核申请数据参数
     * @return MultiResponse
     */
    @Override
    public MultiResponse problemRevoke(ParamModel paramModel) {
        revoke(paramModel.getDetailsId(), "3");
        return new MultiResponse().code(200).message("success");
    }

    @Override
    public MultiResponse shieldReferral(ParamModel paramModel) {
        List<ResultEntity> resultEntityList = shieldResultDetailOperation.getShieldById(paramModel.getDetailsId());
        String[] names = paramModel.getUser().split(" ");
        for (ResultEntity defectVo : resultEntityList) {
            Revision revision = defectVo.getRevision();
            List<Referral> referrals = revision.getReferrals();
            if (referrals == null) {
                referrals = new ArrayList<>();
            }
            Referral referral = new Referral();
            referral.setUserName(revision.getReviewerName());
            referral.setUserId(revision.getReviewerId());
            referral.setDateTime(LocalDateTime.now());
            referrals.add(referral);
            revision.setReferrals(referrals);
            revision.setReviewerName(names[0]);
            revision.setReviewerId(names[1]);
            shieldResultDetailOperation.save(defectVo);
        }
        return new MultiResponse().code(200).result(resultEntityList.get(0));
    }

    /**
     * 提供下拉列表
     *
     * @return MultiResponse
     */
    @Override
    public MultiResponse getPoisoningSelect() {
        return scanResultDetailOperation.getPoisoningSelect();
    }

    /**
     * 问题撤销
     *
     * @param ids            id列表
     * @param reviewerStatus 审核状态
     */
    public void revoke(List<String> ids, String reviewerStatus) {
        List<ResultEntity> resultEntityList = shieldResultDetailOperation.getShieldById(ids);
        // 更新屏蔽表的数据
        for (ResultEntity resultEntity : resultEntityList) {
            resultEntity.setStatus("0");
            resultEntity.getRevision().setReviewerStatus(reviewerStatus);
            shieldResultDetailOperation.save(resultEntity);
        }
        // 更新数据详情表的数据
        List<String> idList = resultEntityList.stream().map(ResultEntity::getDetailId).collect(Collectors.toList());
        List<ResultEntity> resultEntities = scanResultDetailOperation.findScanResultDetailById(idList);
        for (ResultEntity resultEntity : resultEntities) {
            resultEntity.setStatus("0");
            scanResultDetailOperation.saveScanResultDetail(resultEntity);
        }
        // 更新版本扫描表数据
        TaskEntity taskEntity = scanResultDetailOperation.getScanResultByScanId(resultEntities.get(0).getScanId());
        Integer solveCount = taskEntity.getSolveCount() == null ? 0 : taskEntity.getSolveCount();
        taskEntity.setSolveCount(solveCount - resultEntities.size());
        taskEntity.setIssueCount(taskEntity.getResultCount() - taskEntity.getSolveCount());
        scanResultDetailOperation.saveTaskEntity(taskEntity);
    }
}
