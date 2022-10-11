package com.huawei.antipoisoning.business.operation;

import com.huawei.antipoisoning.business.enmu.CollectionTableName;
import com.huawei.antipoisoning.business.entity.ResultEntity;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.entity.shield.ParamModel;
import com.huawei.antipoisoning.business.entity.shield.PoisonReportModel;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 防投毒扫描问题操作
 *
 * @author zhangshengnian zWX1067200
 * @since 2022/08/18 20:15
 */
@Component
public class ScanResultDetailOperation {
    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 根据id获取问题详情
     *
     * @param detailsId 数据库生成id
     * @return List<ResultEntity>
     */
    public List<ResultEntity> findScanResultDetailById(List<String> detailsId) {
        Criteria criteria = Criteria.where("_id").in(detailsId);
        Query query = Query.query(criteria);
        return mongoTemplate.find(query, ResultEntity.class);
    }

    /**
     * 保存/更新问题详情数据
     *
     * @param resultEntity 待保存数据
     */
    public void saveScanResultDetail(ResultEntity resultEntity) {
        mongoTemplate.save(resultEntity);
    }

    /**
     * 根据scan_id查询问题详情列表
     *
     * @param scanId       扫描生成id
     * @param userId       用户id
     * @param resultDetail 结果详情
     * @return List<ResultEntity>
     */
    public List<ResultEntity> getResultDetail(String scanId, String userId, ParamModel resultDetail) {
        String tableName = CollectionTableName.SCAN_RESULT_DETAILS;
        Criteria criteria = Criteria.where("scan_id").is(scanId);
        if (StringUtils.isNotBlank(resultDetail.getStatus())) {
            criteria.and("status").is(resultDetail.getStatus());
        }
        if (StringUtils.isNotBlank(resultDetail.getFileName())) {
            criteria.and("suspicious_file_name").is(resultDetail.getFileName());
        }
        if (StringUtils.isNotBlank(resultDetail.getReviewerStatus())) {
            tableName = CollectionTableName.SHIELD_RESULT_DETAIL;
            criteria.and("revision.reviewerStatus").is(resultDetail.getReviewerStatus());
            if ("1".equals(resultDetail.getReviewerStatus())) {
                if (StringUtils.isNotBlank(resultDetail.getApplicant())) {
                    criteria.and("revision.userId").is(userId);
                } else {
                    criteria.and("revision.reviewerId").is(userId);
                }
            }
            if ("2".equals(resultDetail.getReviewerStatus())) {
                criteria.orOperator(Criteria.where("revision.userId").is(userId),
                        Criteria.where("revision.reviewerId").is(userId));
            }
            if ("4".equals(resultDetail.getReviewerStatus())) {
                criteria.orOperator(Criteria.where("revision.userId").is(userId),
                        Criteria.where("revision.reviewerId").is(userId));
            }
        }
        Criteria antiCriteria = new Criteria();
        if (StringUtils.isNotBlank(resultDetail.getRuleName())) {
            antiCriteria.and("link.rule_desc").is(resultDetail.getRuleName());
        }
        List<AggregationOperation> operations = new ArrayList<>();
        LookupOperation lookup = getLookup();
        operations.add(Aggregation.match(criteria));
        operations.add(lookup);
        operations.add(Aggregation.unwind("link"));
        operations.add(Aggregation.match(antiCriteria));
        if (resultDetail.getPageNum() != null && resultDetail.getPageSize() != null) {
            operations.add(Aggregation.skip((long) (resultDetail.getPageNum() - 1)
                    * resultDetail.getPageSize()));
            operations.add(Aggregation.limit(resultDetail.getPageSize()));
        }
        return mongoTemplate.aggregate(Aggregation.newAggregation(operations), tableName, ResultEntity.class)
                .getMappedResults();
    }

    private LookupOperation getLookup() {
        return LookupOperation.newLookup()
                .from(CollectionTableName.ANTI_CHECK_RULE)
                .localField("rule_name")
                .foreignField("rule_name")
                .as("link");
    }

    /**
     * 查询防投毒所有扫描情况
     *
     * @param resultModel 参数体
     * @return List<AntiEntity>
     */
    public List<TaskEntity> getScanResult(ParamModel resultModel) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotBlank(resultModel.getProjectName())) {
            criteria.and("project_name").is(resultModel.getProjectName());
        }
        if (StringUtils.isNotBlank(resultModel.getRepoName())) {
            criteria.and("repo_name").is(resultModel.getRepoName());
        }
        if (StringUtils.isNotBlank(resultModel.getBranch())) {
            criteria.and("branch").is(resultModel.getBranch());
        }
        if (StringUtils.isNotBlank(resultModel.getStartTime()) && StringUtils.isNotBlank(resultModel.getEndTime())) {
            criteria.and("create_time").gte(resultModel.getStartTime()).lte(resultModel.getEndTime());
        }
        Query query = Query.query(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "result_count"));
        if (resultModel.getPageNum() != null && resultModel.getPageSize() != null) {
            query.skip((resultModel.getPageNum() - 1) * resultModel.getPageSize());
            query.limit(resultModel.getPageSize());
        }
        return mongoTemplate.find(query, TaskEntity.class);
    }

    /**
     * 修改问题状态
     *
     * @param detailsId 问题id列表
     * @param status    状态
     */
    public void updateStatus(List<String> detailsId, String status) {
        Criteria criteria = Criteria.where("_id").in(detailsId);
        Query query = Query.query(criteria);
        Update update = Update.update("status", status);
        mongoTemplate.updateMulti(query, update, CollectionTableName.SCAN_RESULT_DETAILS);
    }

    /**
     * 根据条件查询result
     *
     * @param userId      用户id
     * @param reportModel 参数体
     * @return List<PoisonReportModel>
     */
    public List<PoisonReportModel> getGroupResult(String userId, ParamModel reportModel) {
        String tableName = CollectionTableName.SCAN_RESULT_DETAILS;
        Criteria criteria = Criteria.where("scan_id").is(reportModel.getScanId());
        if (StringUtils.isNotBlank(reportModel.getStatus())) {
            criteria.and("status").is(reportModel.getStatus());
        }
        if (StringUtils.isNotBlank(reportModel.getReviewerStatus())) {
            tableName = CollectionTableName.SHIELD_RESULT_DETAIL;
            criteria.and("revision.reviewerStatus").is(reportModel.getReviewerStatus());
            if ("1".equals(reportModel.getReviewerStatus())) {
                if (StringUtils.isNotBlank(reportModel.getApplicant())) {
                    criteria.and("revision.userId").is(userId);
                } else {
                    criteria.and("revision.reviewerId").is(userId);
                }
            }
            if ("2".equals(reportModel.getReviewerStatus())) {
                criteria.orOperator(Criteria.where("revision.userId").is(userId),
                        Criteria.where("revision.reviewerId").is(userId));
            }
            if ("4".equals(reportModel.getReviewerStatus())) {
                criteria.orOperator(Criteria.where("revision.userId").is(userId),
                        Criteria.where("revision.reviewerId").is(userId));
            }
        }
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.group("status", "suspicious_file_name", "rule_name")
                .first("status").as("status")
                .first("suspicious_file_name").as("fileName")
                .first("rule_name").as("ruleName")
                .count().as("total"));
        return mongoTemplate.aggregate(Aggregation.newAggregation(operations),
                tableName, PoisonReportModel.class).getMappedResults();
    }

    /**
     * 根据扫描id获取扫描数据
     *
     * @param scanId 扫描id
     * @return TaskEntity
     */
    public TaskEntity getScanResultByScanId(String scanId) {
        Criteria criteria = Criteria.where("scan_id").is(scanId);
        return mongoTemplate.findOne(Query.query(criteria), TaskEntity.class);
    }

    /**
     * 保存扫描数据
     *
     * @param taskEntity 更新实体类
     */
    public void saveTaskEntity(TaskEntity taskEntity) {
        mongoTemplate.save(taskEntity);
    }

    /**
     * 根据规则名称查询规则
     *
     * @param ruleName 规则名称
     * @return Map
     */
    public Map findRuleByName(String ruleName) {
        Criteria criteria = Criteria.where("rule_name").is(ruleName);
        return mongoTemplate.findOne(Query.query(criteria), Map.class, CollectionTableName.ANTI_CHECK_RULE);
    }

    /**
     * 提供下拉列表
     *
     * @return MultiResponse
     */
    public MultiResponse getPoisoningSelect() {
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.group("project_name", "repo_name", "branch")
                .first("project_name").as("project_name")
                .first("repo_name").as("repo_name")
                .first("branch").as("branch"));
        List<TaskEntity> projectRepo = mongoTemplate.aggregate(Aggregation.newAggregation(operations),
                CollectionTableName.POISON_VERSION_TASK, TaskEntity.class).getMappedResults();
        return new MultiResponse().code(200).result(projectRepo);
    }
}
