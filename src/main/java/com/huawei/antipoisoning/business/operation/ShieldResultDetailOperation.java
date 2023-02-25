/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.operation;

import com.huawei.antipoisoning.business.enmu.CollectionTableName;
import com.huawei.antipoisoning.business.enmu.ConstantsArgs;
import com.huawei.antipoisoning.business.enmu.ReviewerStatus;
import com.huawei.antipoisoning.business.enmu.ShieldStatus;
import com.huawei.antipoisoning.business.entity.ResultEntity;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.entity.pr.PRResultEntity;
import com.huawei.antipoisoning.business.entity.pr.PRTaskEntity;
import com.huawei.antipoisoning.business.entity.shield.ParamModel;
import com.huawei.antipoisoning.business.entity.shield.QueryShieldModel;
import com.huawei.antipoisoning.business.entity.shield.Revision;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ShieldResultDetailOperation.class);

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
        scanResultDetailOperation.updatePRStatus(detailsId, status);
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
     * @param type   查询类型
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

    public List<Map> shieldDetail(QueryShieldModel queryShieldModel) {
        List<AggregationOperation> operations = new ArrayList<>();
        boolean isFull = ConstantsArgs.HARMONY_FULL.equals(queryShieldModel.getType());
        Criteria criteria = querySummaryCriteria(queryShieldModel);
        Criteria criteria1 = queryShieldCriteria(queryShieldModel);
        operations.add(Aggregation.match(criteria1));
        operations.add(Aggregation.project("project_name", "repo_name", "branch", "revision.shieldType",
                "suspicious_file_name", "rule_name", "revision.userName", "revision.reason", "revision.applyDate", "revision.reviewerName")
                .andExpression("{$substrCP:{'$revision.auditDate',0,24}}").as("auditDate"));
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.sort(Sort.Direction.DESC, "auditDate"));
        if (queryShieldModel.getPageNum() != null && queryShieldModel.getPageSize() != null) {
            operations.add(Aggregation.skip((long) (queryShieldModel.getPageNum() - 1)
                    * queryShieldModel.getPageSize()));
            operations.add(Aggregation.limit(queryShieldModel.getPageSize()));
        }
        String tableName = isFull ? CollectionTableName
                .SHIELD_RESULT_DETAIL : CollectionTableName.SHIELD_PR_RESULT_DETAIL;
        LOGGER.info("shieldDetailOperations------------++”：{}",tableName);
        LOGGER.info("shieldDetailOperations------------++”：{}",Aggregation.newAggregation(operations));
        return mongoTemplate.aggregate(Aggregation.newAggregation(operations), tableName, Map.class).getMappedResults();
    }

    private Criteria querySummaryCriteria(QueryShieldModel queryShieldModel) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotBlank(queryShieldModel.getProjectName())) {
            criteria.and("link.projectName").is(queryShieldModel.getProjectName());
        }
        if (StringUtils.isNotBlank(queryShieldModel.getRepoName())) {
            criteria.and("link.repoNameEn").is(queryShieldModel.getRepoName());
        }
        if (StringUtils.isNotBlank(queryShieldModel.getStartTime()) && StringUtils.isNotBlank(queryShieldModel.getEndTime())) {
            criteria.and("auditDate").gte(queryShieldModel.getStartTime())
                    .lte(queryShieldModel.getEndTime());
        }
        return criteria;
    }

    private Criteria queryShieldCriteria(QueryShieldModel query) {
        Criteria criteria = Criteria.where("revision.reviewerStatus").is(ReviewerStatus.TWO.getCode()).and("status").is(ShieldStatus.TWO.getCode());
        if (StringUtils.isNotBlank(query.getDefectCheckerName())) {
            criteria.and("rule_name").is(query.getDefectCheckerName());
        }
        if (StringUtils.isNotBlank(query.getShieldType())) {
            criteria.and("revision.shieldType").is(query.getShieldType());
        }
        if (StringUtils.isNotBlank(query.getApplyUser())) {
            criteria.and("revision.userName").is(query.getApplyUser());
        }
        if (StringUtils.isNotBlank(query.getAuditUser())) {
            criteria.and("revision.reviewerName").is(query.getAuditUser());
        }
        if (StringUtils.isNotBlank(query.getReason())) {
            criteria.and("revision.reason").is(query.getReason());
        }
        return criteria;
    }


    public List<Map> shieldTypeMap(QueryShieldModel queryShieldModel) {
        boolean isFull = ConstantsArgs.HARMONY_FULL.equals(queryShieldModel.getType());
        Criteria criteria = querySummaryCriteria(queryShieldModel);
        Criteria criteria1 = queryShieldCriteria(queryShieldModel);
        Aggregation aggregation = Aggregation.newAggregation(
                // 主表条件
                Aggregation.match(criteria1),
                Aggregation.project("revision.shieldType", "_id").andExpression("{$substrCP:{'$revision.auditDate',0,24}}").as("auditDate"),
                Aggregation.match(criteria),
                Aggregation.group("shieldType").count().as("count")
        );
        String tableName = isFull ? CollectionTableName
                .SHIELD_RESULT_DETAIL : CollectionTableName.SHIELD_PR_RESULT_DETAIL;
        return mongoTemplate.aggregate(aggregation, tableName, Map.class).getMappedResults();
    }

    /**
     * 获取排名前十五的防投毒屏蔽规则
     *
     * @param queryShieldModel 方法参数请求体
     * @return MultiResponse
     */
    public List<Map> getPoisonTopFifteen(QueryShieldModel queryShieldModel) {
        boolean isFull = ConstantsArgs.HARMONY_FULL.equals(queryShieldModel.getType());
        Criteria criteria = querySummaryCriteria(queryShieldModel);
        Criteria criteria1 = queryShieldCriteria(queryShieldModel);
        Aggregation aggregation = Aggregation.newAggregation(
                // 主表条件
                Aggregation.match(criteria1),
                Aggregation.project("rule_name", "revision.shieldType", "_id")
                        .andExpression("{$substrCP:{'$revision.auditDate',0,24}}").as("auditDate"),
                Aggregation.match(criteria),
                Aggregation.group("rule_name").count().as("count")
                        .first("shieldType").as("shieldType"),
                Aggregation.sort(Sort.Direction.DESC, "count"),
                Aggregation.limit(15)
        );
        String tableName = isFull ? CollectionTableName
                .SHIELD_RESULT_DETAIL : CollectionTableName.SHIELD_PR_RESULT_DETAIL;
        return mongoTemplate.aggregate(aggregation, tableName, Map.class).getMappedResults();
    }

    /**
     * 屏蔽详情数量
     *
     * @param queryShieldModel 请求参数
     * @return int
     */
    public int countShieldDetail(QueryShieldModel queryShieldModel) {
        List<AggregationOperation> operations = new ArrayList<>();
        boolean isFull = ConstantsArgs.HARMONY_FULL.equals(queryShieldModel.getType());
        Criteria criteria1 = queryShieldCriteria(queryShieldModel);
        operations.add(Aggregation.match(criteria1));
        operations.add(Aggregation.project("_id").andExpression("{$substrCP:{'$revision.auditDate',0,24}}").as("auditDate"));
        Criteria criteria = querySummaryCriteria(queryShieldModel);
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.count().as("count"));
        String tableName = isFull ? CollectionTableName
                .SHIELD_RESULT_DETAIL : CollectionTableName.SHIELD_PR_RESULT_DETAIL;
        List<Map> list =
                mongoTemplate.aggregate(Aggregation.newAggregation(operations), tableName, Map.class)
                        .getMappedResults();
        int count = 0;
        if (list.size() > 0) {
            count = Integer.parseInt(list.get(0).get("count").toString());
        }
        return count;
    }

}
