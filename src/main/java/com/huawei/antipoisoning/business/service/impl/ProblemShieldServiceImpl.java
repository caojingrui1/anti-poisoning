/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.service.impl;

import com.huawei.antipoisoning.business.entity.ResultEntity;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.entity.pr.PRResultEntity;
import com.huawei.antipoisoning.business.entity.pr.PRTaskEntity;
import com.huawei.antipoisoning.business.entity.shield.ParamModel;
import com.huawei.antipoisoning.business.entity.shield.PoisonReportModel;
import com.huawei.antipoisoning.business.entity.shield.QueryShieldModel;
import com.huawei.antipoisoning.business.entity.shield.Referral;
import com.huawei.antipoisoning.business.entity.shield.Revision;
import com.huawei.antipoisoning.business.operation.ScanResultDetailOperation;
import com.huawei.antipoisoning.business.operation.ShieldResultDetailOperation;
import com.huawei.antipoisoning.business.service.ProblemShieldService;
import com.huawei.antipoisoning.business.util.AntiConstants;
import com.huawei.antipoisoning.business.util.YamlUtil;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ProblemShieldServiceImpl.class);

    /**
     * 查询我的申请和待我审批数量
     *
     * @param userId 用户id
     * @param scanId 扫描id
     * @return
     */
    @Override
    public MultiResponse applyAndAuditNumber(String userId, String scanId) {
        Map<String, Long> result = shieldResultDetailOperation.applyAndAuditNumber(userId, null, scanId);
        return new MultiResponse().code(200).result(result);
    }

    /**
     * 查询我的申请和待我审批门禁问题数量
     *
     * @param userId 用户id
     * @param scanId 扫描id
     * @return
     */
    @Override
    public MultiResponse applyAndAuditPRNumber(String userId, String scanId) {
        Map<String, Long> result = shieldResultDetailOperation.applyAndAuditNumber(userId, "pr", scanId);
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
     * 门禁审核通过问题撤销
     *
     * @param paramModel 待撤销问题列表
     * @return MultiResponse
     */
    @Override
    public MultiResponse auditPassPRRevoke(ParamModel paramModel) {
        prRevoke(paramModel.getDetailsId(), "4");
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
        resultEntities.forEach(resultEntity -> {
            String relativeFileName = resultEntity.getSuspiciousFileName().replace(
                    YamlUtil.getToolPath() + AntiConstants.REPOPATH +
                            resultEntity.getRepoName() + "-" + resultEntity.getBranch(), "");
            resultEntity.setSuspiciousFileName(relativeFileName);
            resultEntity.setFileUrl(resultEntity.getScanResult().getRepoUrl() + AntiConstants.PATH_TREE + resultEntity.getScanResult().getBranch() + relativeFileName);
        });
        result.put("data", resultEntities);
        return new MultiResponse().code(200).result(result);
    }

    /**
     * 查询防投毒门禁扫描问题详情
     *
     * @param scanId     唯一id
     * @param userId     用户id
     * @param paramModel 查询参数体
     * @return MultiResponse
     */
    @Override
    public MultiResponse getPRResultDetail(String scanId, String userId, ParamModel paramModel) {
        List<PRResultEntity> resultEntities = scanResultDetailOperation.getPRResultDetail(scanId, userId, paramModel);
        paramModel.setPageNum(null);
        paramModel.setPageSize(null);
        int count = scanResultDetailOperation.getPRResultDetail(scanId, userId, paramModel).size();
        Map<String, Object> result = new HashMap<>(2);
        resultEntities.forEach(resultEntity -> {
            String relativeFileName = resultEntity.getSuspiciousFileName().replace(
                    YamlUtil.getToolPath() + AntiConstants.REPOPATH +
                            resultEntity.getRepoName() + "-" + resultEntity.getBranch(), "");
            resultEntity.setSuspiciousFileName(relativeFileName);
            resultEntity.setFileUrl(resultEntity.getPrScanResult().getRepoUrl() + AntiConstants.PATH_TREE + resultEntity.getPrScanResult().getBranch() + relativeFileName);
        });
        result.put("count", count);
        result.put("data", resultEntities);
        return new MultiResponse().code(200).result(result);
    }

    /**
     * 获取扫描结果报告。
     *
     * @param userId     用户ID
     * @param type       查询类型
     * @param paramModel 参数
     * @return MultiResponse
     */
    @Override
    public MultiResponse getScanReport(String userId, String type, ParamModel paramModel) {
        List<PoisonReportModel> resultEntityList = scanResultDetailOperation.getGroupResult(userId, type, paramModel);
        Map<String, Integer> fileNameReport = new LinkedHashMap<>(); // 文件
        Map<String, Integer> ruleNameReport = new LinkedHashMap<>(); // 规则
        Map<String, Integer> statusReport = new HashMap();
        // 根据状态分类统计
        statusReport = resultEntityList.stream().collect(Collectors.groupingBy(PoisonReportModel::getStatus,
                Collectors.summingInt(PoisonReportModel::getTotal)));
        // 根据文件名统计
        String replaceStr = "";
        if (StringUtils.isEmptyOrNull(type)) {
            replaceStr = YamlUtil.getToolPath() + AntiConstants.REPOPATH;
        } else {
            replaceStr = YamlUtil.getToolPath() + AntiConstants.PR_REPOPATH;
        }
        final String pathStr = replaceStr;
        resultEntityList.stream().collect(Collectors.groupingBy(PoisonReportModel::getFileName,
                Collectors.summingInt(PoisonReportModel::getTotal)))
                .entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(value -> {
                    fileNameReport.put(value.getKey()
                            .replace(pathStr, ""), value.getValue());
                });
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
     * 查询防投毒门禁扫描情况(根据社区PR分组）
     *
     * @param paramModel 参数体
     * @return MultiResponse
     */
    @Override
    public MultiResponse getScanPRResultGroup(ParamModel paramModel) {
        List<PRTaskEntity> taskEntityList = scanResultDetailOperation.getScanPRResultGroup(paramModel);
        paramModel.setPageNum(null);
        paramModel.setPageSize(null);
        int count = scanResultDetailOperation.getScanPRResultGroup(paramModel).size();
        Map<String, Object> result = new HashMap<>(2);
        result.put("count", count);
        result.put("data", taskEntityList);
        return new MultiResponse().code(200).result(result);
    }

    /**
     * 查询防投毒门禁扫描情况(根据社区PR分组）
     *
     * @param paramModel 参数体
     * @return MultiResponse
     */
    @Override
    public MultiResponse getScanPRResult(ParamModel paramModel) {
        List<PRTaskEntity> taskEntityList = scanResultDetailOperation.getScanPRResult(paramModel);
        paramModel.setPageNum(null);
        paramModel.setPageSize(null);
        int count = scanResultDetailOperation.getScanPRResult(paramModel).size();
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
     * 防投毒门禁问题屏蔽申请
     *
     * @param userId     码云id
     * @param login      码云唯一标识
     * @param paramModel 屏蔽参数体
     * @return MultiResponse
     */
    @Override
    public MultiResponse poisonPRApply(String userId, String login, ParamModel paramModel) {
        List<PRResultEntity> resultEntities =
                scanResultDetailOperation.findPRScanResultDetailById(paramModel.getDetailsId());
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
        for (PRResultEntity resultEntity : resultEntities) {
            resultEntity.setStatus("1");
            scanResultDetailOperation.savePRScanResultDetail(resultEntity);
            resultEntity.setRevision(revision);
            resultEntity.setDetailId(resultEntity.getId());
            resultEntity.setId(null);
        }
        shieldResultDetailOperation.savePRShieldResult(resultEntities);
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
     * 门禁待审核问题的审批
     *
     * @param paramModel 审批参数体
     * @return MultiResponse
     */
    @Override
    public MultiResponse problemPRAudit(ParamModel paramModel) {
        MultiResponse multiResponse = null;
        if ("fail".equals(paramModel.getAuditResult())) {
            multiResponse = shieldResultDetailOperation.problemPRAudit(paramModel, "0");
        } else {
            multiResponse = shieldResultDetailOperation.problemPRAudit(paramModel, "2");
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

    /**
     * 撤销审核申请数据
     *
     * @param paramModel 撤销审核申请数据参数
     * @return MultiResponse
     */
    @Override
    public MultiResponse problemPRRevoke(ParamModel paramModel) {
        prRevoke(paramModel.getDetailsId(), "3");
        return new MultiResponse().code(200).message("success");
    }

    /**
     * 屏蔽。
     *
     * @param paramModel 参数
     * @return MultiResponse
     */
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
     * 门禁问题屏蔽。
     *
     * @param paramModel 参数
     * @return MultiResponse
     */
    @Override
    public MultiResponse shieldPRReferral(ParamModel paramModel) {
        List<PRResultEntity> resultEntityList = shieldResultDetailOperation.getPRShieldById(paramModel.getDetailsId());
        String[] names = paramModel.getUser().split(" ");
        for (PRResultEntity defectVo : resultEntityList) {
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
            shieldResultDetailOperation.savePR(defectVo);
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
     * 提供门禁扫描下拉列表
     *
     * @return MultiResponse
     */
    @Override
    public MultiResponse getPRPoisoningSelect() {
        return scanResultDetailOperation.getPRPoisoningSelect();
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

    /**
     * 门禁问题撤销
     *
     * @param ids            id列表
     * @param reviewerStatus 审核状态
     */
    public void prRevoke(List<String> ids, String reviewerStatus) {
        List<PRResultEntity> resultEntityList = shieldResultDetailOperation.getPRShieldById(ids);
        // 更新屏蔽表的数据
        for (PRResultEntity resultEntity : resultEntityList) {
            resultEntity.setStatus("0");
            resultEntity.getRevision().setReviewerStatus(reviewerStatus);
            shieldResultDetailOperation.savePR(resultEntity);
        }
        // 更新数据详情表的数据
        List<String> idList = resultEntityList.stream().map(PRResultEntity::getDetailId).collect(Collectors.toList());
        List<PRResultEntity> resultEntities = scanResultDetailOperation.findPRScanResultDetailById(idList);
        for (PRResultEntity resultEntity : resultEntities) {
            resultEntity.setStatus("0");
            scanResultDetailOperation.savePRScanResultDetail(resultEntity);
        }
        // 更新版本扫描表数据
        PRTaskEntity taskEntity = scanResultDetailOperation.getPRScanResultByScanId(resultEntities.get(0).getScanId());
        Integer solveCount = taskEntity.getSolveCount() == null ? 0 : taskEntity.getSolveCount();
        taskEntity.setSolveCount(solveCount - resultEntities.size());
        taskEntity.setIssueCount(taskEntity.getResultCount() - taskEntity.getSolveCount());
        scanResultDetailOperation.savePRTaskEntity(taskEntity);
    }


    /**
     * 获取排名前十五的防投毒屏蔽规则
     *
     * @param queryShieldModel 方法参数请求体
     * @return MultiResponse
     */
    @Override
    public MultiResponse getPoisonTopFifteen(QueryShieldModel queryShieldModel) {
        List<Map> list = shieldResultDetailOperation.getPoisonTopFifteen(queryShieldModel);
        return new MultiResponse().code(200).result(list);
    }

    /**
     * 根据scanId防投毒版本检查信息
     *
     * @param scanId   版本扫描Id
     * @return MultiResponse
     */
    @Override
    public MultiResponse getScanResultByScanId(String scanId) {
        return new MultiResponse().code(200).result(scanResultDetailOperation.getScanResultByScanId(scanId));
    }

    /**
     * 根据scanId防投毒pr检查信息
     *
     * @param scanId   版本扫描Id
     * @return MultiResponse
     */
    @Override
    public MultiResponse getPRScanResultByScanId(String scanId) {
        return new MultiResponse().code(200).result(scanResultDetailOperation.getPRScanResultByScanId(scanId));
    }

    /**
     * 获取防投毒屏蔽详情查询
     *
     * @param queryShieldModel 查询参数体
     * @return MultiResponse
     */
    @Override
    public MultiResponse poisonShieldDetail(QueryShieldModel queryShieldModel) {
        List<Map> list = shieldResultDetailOperation.shieldDetail(queryShieldModel);
        List<Map> mapList = list.stream().map(map -> {
            String url = "https://gitee.com/" + map.get("project_name") + "/" + map.get("repo_name") + ".git";
            map.put("gitUrl", url);
            String suspicious_file_name =map.get("suspicious_file_name").toString().replace(
                    YamlUtil.getToolPath() + AntiConstants.REPOPATH +
                            map.get("project_name") + "-" + map.get("repo_name"),"");
            map.put("file_name", suspicious_file_name);
            return map;
        }).collect(Collectors.toList());
        int count = shieldResultDetailOperation.countShieldDetail(queryShieldModel);
        HashMap<String, Object> result = new HashMap<>(2);
        result.put("shieldDetail", mapList);
        result.put("count", count);
        return new MultiResponse().code(200).result(result);
    }

    /**
     * 防投毒屏蔽类型分布
     *
     * @param queryShieldModel 方法参数请求体
     * @return MultiResponse
     */
    @Override
    public MultiResponse poisonShieldTypeMap(QueryShieldModel queryShieldModel) {
        List<Map> list = shieldResultDetailOperation.shieldTypeMap(queryShieldModel);
        Map<String, Integer> result = new HashMap<>();
        for (Map map : list) {
            if (map.get("count") instanceof Integer && map.get("_id") instanceof String) {
                result.put(map.get("_id").toString(), (Integer) (map.get("count")));
            }
        }
        return new MultiResponse().code(200).result(result);
    }
}
