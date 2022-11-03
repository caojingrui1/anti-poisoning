/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.operation;

import com.huawei.antipoisoning.business.enmu.CollectionTableName;
import com.huawei.antipoisoning.business.entity.ResultEntity;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.entity.pr.PRResultEntity;
import com.huawei.antipoisoning.business.entity.pr.PRTaskEntity;
import com.huawei.antipoisoning.business.entity.shield.ParamModel;
import com.huawei.antipoisoning.business.entity.shield.Revision;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 防投毒屏蔽详情表操作
 *
 * @author zhangshengnian zWX1067200
 * @since 2022/08/18 16:54
 */
@Component
public class ShieldResultDetailOperation {
    @Autowired
    @Qualifier("poisonMongoTemplate")
    private MongoTemplate mongoTemplate;

    @Autowired
    private ScanResultDetailOperation scanResultDetailOperation;

    /**
     * 插入屏蔽申请数据
     *
     * @param resultEntities 屏蔽申请数据
     */
    public void saveShieldResult(List<ResultEntity> resultEntities) {
        mongoTemplate.insert(resultEntities, CollectionTableName.SHIELD_RESULT_DETAIL);
    }

    /**
     * 插入门禁屏蔽申请数据
     *
     * @param resultEntities 屏蔽申请数据
     */
    public void savePRShieldResult(List<PRResultEntity> resultEntities) {
        mongoTemplate.insert(resultEntities, CollectionTableName.SHIELD_PR_RESULT_DETAIL);
    }

    /**
     * 根据id 获取屏蔽结果详情
     *
     * @param detailsId id数组
     * @return List<ResultEntity>
     */
    public List<ResultEntity> getShieldById(List<String> detailsId) {
        Criteria criteria = Criteria.where("_id").in(detailsId);
        return mongoTemplate.find(Query.query(criteria), ResultEntity.class, CollectionTableName.SHIELD_RESULT_DETAIL);
    }

    /**
     * 根据id 获取门禁屏蔽结果详情
     *
     * @param detailsId id数组
     * @return List<ResultEntity>
     */
    public List<PRResultEntity> getPRShieldById(List<String> detailsId) {
        Criteria criteria = Criteria.where("_id").in(detailsId);
        return mongoTemplate.find(Query.query(criteria), PRResultEntity.class,
                CollectionTableName.SHIELD_PR_RESULT_DETAIL);
    }

    /**
     * 更新/保存屏蔽申请数据
     *
     * @param resultEntity 屏蔽申请数据
     */
    public void save(ResultEntity resultEntity) {
        mongoTemplate.save(resultEntity, CollectionTableName.SHIELD_RESULT_DETAIL);
    }

    /**
     * 更新/保存门禁屏蔽申请数据
     *
     * @param resultEntity 屏蔽申请数据
     */
    public void savePR(PRResultEntity resultEntity) {
        mongoTemplate.save(resultEntity, CollectionTableName.SHIELD_PR_RESULT_DETAIL);
    }

    /**
     * 问题审核
     *
     * @param auditModel 审核参数
     * @param status     状态
     */
    public MultiResponse problemAudit(ParamModel auditModel, String status) {
        List<ResultEntity> resultEntityList = getShieldById(auditModel.getDetailsId());
        for (ResultEntity resultEntity : resultEntityList) {
            Revision revision = resultEntity.getRevision();
            resultEntity.setStatus(status);
            revision.setReviewerStatus("2");
            revision.setAuditResult(auditModel.getAuditResult());
            revision.setAuditOpinion(auditModel.getAuditOpinion());
            revision.setAuditDate(LocalDateTime.now());
            save(resultEntity);
        }
        List<String> detailsId = resultEntityList.stream().map(ResultEntity::getDetailId).collect(Collectors.toList());
        scanResultDetailOperation.updateStatus(detailsId, status);
        // 修改扫描结果表中解决问题数
        if ("2".equals(status)) {
            TaskEntity taskEntity =
                    scanResultDetailOperation.getScanResultByScanId(resultEntityList.get(0).getScanId());
            Integer solveCount = taskEntity.getSolveCount() == null ? 0 : taskEntity.getSolveCount();
            taskEntity.setSolveCount(solveCount + resultEntityList.size());
            taskEntity.setIssueCount(taskEntity.getResultCount() - taskEntity.getSolveCount());
            scanResultDetailOperation.saveTaskEntity(taskEntity);
        }
        return new MultiResponse().code(200).result(resultEntityList);
    }

    /**
     * 门禁问题审核
     *
     * @param auditModel 审核参数
     * @param status     状态
     */
    public MultiResponse problemPRAudit(ParamModel auditModel, String status) {
        List<PRResultEntity> resultEntityList = getPRShieldById(auditModel.getDetailsId());
        for (PRResultEntity resultEntity : resultEntityList) {
            Revision revision = resultEntity.getRevision();
            resultEntity.setStatus(status);
            revision.setReviewerStatus("2");
            revision.setAuditResult(auditModel.getAuditResult());
            revision.setAuditOpinion(auditModel.getAuditOpinion());
            revision.setAuditDate(LocalDateTime.now());
            savePR(resultEntity);
        }
        List<String> detailsId = resultEntityList.stream().map(PRResultEntity::getDetailId).collect(Collectors.toList());
        scanResultDetailOperation.updateStatus(detailsId, status);
        // 修改扫描结果表中解决问题数
        if ("2".equals(status)) {
            PRTaskEntity taskEntity =
                    scanResultDetailOperation.getPRScanResultByScanId(resultEntityList.get(0).getScanId());
            Integer solveCount = taskEntity.getSolveCount() == null ? 0 : taskEntity.getSolveCount();
            taskEntity.setSolveCount(solveCount + resultEntityList.size());
            taskEntity.setIssueCount(taskEntity.getResultCount() - taskEntity.getSolveCount());
            scanResultDetailOperation.savePRTaskEntity(taskEntity);
        }
        return new MultiResponse().code(200).result(resultEntityList);
    }

    /**
     * 获取我的申请/待我审批数量
     *
     * @param userId 用户id
     * @param type 查询类型
     * @param scanId 扫描id
     * @return Map<String, Long>
     */
    public Map<String, Long> applyAndAuditNumber(String userId, String type, String scanId) {
        String tableName = !"pr".equals(type) ? CollectionTableName.SHIELD_RESULT_DETAIL
                : CollectionTableName.SHIELD_PR_RESULT_DETAIL;
        Map<String, Long> result = new HashMap<>(2);
        Criteria applyCriteria = queryNumberCriteria(userId, scanId, "apply");
        long applyNumber = mongoTemplate.count(Query.query(applyCriteria), tableName);
        Criteria auditCriteria = queryNumberCriteria(userId, scanId, "audit");
        long auditNumber = mongoTemplate.count(Query.query(auditCriteria), tableName);
        result.put("apply", applyNumber);
        result.put("audit", auditNumber);
        return result;
    }

    private Criteria queryNumberCriteria(String userId, String scanId, String apply) {
        Criteria criteria = Criteria.where("scan_id").is(scanId);
        criteria.and("revision.reviewerStatus").is("1");
        if (apply.equals("apply")) {
            criteria.and("revision.userId").is(userId);
        } else {
            criteria.and("revision.reviewerId").is(userId);
        }
        return criteria;
    }
}
