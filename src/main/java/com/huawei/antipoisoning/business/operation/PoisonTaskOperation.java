/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.operation;

import com.huawei.antipoisoning.business.enmu.CollectionTableName;
import com.huawei.antipoisoning.business.enmu.ConstantsArgs;
import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.entity.pr.PRAntiEntity;
import com.huawei.antipoisoning.business.entity.pr.PRTaskEntity;
import com.huawei.antipoisoning.business.entity.vo.PageVo;
import com.huawei.antipoisoning.business.entity.vo.PoisonInspectionVo;
import com.mongodb.client.result.UpdateResult;
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
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 扫描结果包裹数据存档
 *
 * @since: 2022/5/31 17:01
 */
@Component
public class PoisonTaskOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(PoisonTaskOperation.class);

    @Autowired
    @Qualifier("poisonMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * 保存扫描结果
     *
     * @param antiEntity    扫描数据
     * @param newtaskEntity 新参数
     */
    public void insertTaskResult(AntiEntity antiEntity, TaskEntity newtaskEntity) {
        if (ObjectUtils.isEmpty(antiEntity)) {
            return;
        }
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setTaskId(newtaskEntity.getTaskId());
        taskEntity.setScanId(antiEntity.getScanId());
        taskEntity.setBranch(antiEntity.getBranch());
        taskEntity.setRepoUrl(antiEntity.getRepoUrl());
        taskEntity.setRepoName(antiEntity.getRepoName());
        taskEntity.setCreateTime(antiEntity.getCreateTime());
        taskEntity.setLanguage(antiEntity.getLanguage());
        taskEntity.setIsScan(antiEntity.getIsScan());
        taskEntity.setProjectName(antiEntity.getProjectName());
        taskEntity.setRulesName(antiEntity.getRulesName());
        taskEntity.setExecutorId(antiEntity.getExecutorId());
        taskEntity.setExecutorName(antiEntity.getExecutorName());
        taskEntity.setExecutionStatus(newtaskEntity.getExecutionStatus());
        mongoTemplate.insert(taskEntity, CollectionTableName.POISON_VERSION_TASK);
    }

    /**
     * 保存门禁扫描结果
     *
     * @param antiEntity    扫描数据
     * @param newtaskEntity 新参数
     */
    public void insertPRTaskResult(PRAntiEntity antiEntity, PRTaskEntity newtaskEntity) {
        if (ObjectUtils.isEmpty(antiEntity)) {
            return;
        }
        PRTaskEntity taskEntity = new PRTaskEntity();
        taskEntity.setTaskId(newtaskEntity.getTaskId());
        taskEntity.setScanId(antiEntity.getScanId());
        taskEntity.setBranch(antiEntity.getBranch());
        taskEntity.setRepoUrl(antiEntity.getRepoUrl());
        taskEntity.setRepoName(antiEntity.getRepoName());
        taskEntity.setCreateTime(antiEntity.getCreateTime());
        taskEntity.setLanguage(antiEntity.getLanguage());
        taskEntity.setIsScan(antiEntity.getIsScan());
        taskEntity.setPrNumber(antiEntity.getPrNumber());
        taskEntity.setPrUrl(antiEntity.getPrUrl());
        taskEntity.setProjectName(antiEntity.getProjectName());
        taskEntity.setRulesName(antiEntity.getRulesName());
        taskEntity.setExecutorId(antiEntity.getExecutorId());
        taskEntity.setExecutorName(antiEntity.getExecutorName());
        taskEntity.setExecutionStatus(newtaskEntity.getExecutionStatus());
        mongoTemplate.insert(taskEntity, CollectionTableName.POISON_PR_TASK);
    }

    /**
     * 保存扫描结果
     *
     * @param antiEntity 扫描数据
     * @param taskId     任务ID
     */
    public long updateTaskResult(AntiEntity antiEntity, String taskId) {
        if (ObjectUtils.isEmpty(antiEntity)) {
            return Long.valueOf(null);
        }
        Update update = new Update();
        if (antiEntity.getBranch() != null) {
            update.set("branch", antiEntity.getBranch());
        }
        if (antiEntity.getRepoName() != null) {
            update.set("repo_name", antiEntity.getRepoName());
        }
        if (antiEntity.getRepoUrl() != null) {
            update.set("repo_url", antiEntity.getRepoUrl());
        }
        if (antiEntity.getScanId() != null) {
            update.set("scan_id", antiEntity.getScanId());
        }
        if (antiEntity.getProjectName() != null) {
            update.set("project_name", antiEntity.getProjectName());
        }
        if (antiEntity.getProjectName() != null) {
            update.set("create_time", antiEntity.getCreateTime());
        }
        Query query = Query.query(Criteria.where("task_id").is(taskId));
        return mongoTemplate.updateFirst(query, update, CollectionTableName.POISON_VERSION_TASK).getModifiedCount();
    }

    /**
     * 下载完更新扫描结果
     *
     * @param taskEntity 参数
     * @@return 结果
     */
    public Long updateTaskDownloadTime(TaskEntity taskEntity) {
        Update update = new Update();
        if (taskEntity.getDownloadConsuming() != null) {
            update.set("download_consuming", taskEntity.getDownloadConsuming());
        }
        Query query = Query.query(Criteria.where("task_id").is(taskEntity.getTaskId()));
        return mongoTemplate.updateFirst(query, update, CollectionTableName.POISON_VERSION_TASK).getModifiedCount();
    }

    /**
     * 下载完更新扫描结果
     *
     * @param taskEntity 参数
     * @@return 结果
     */
    public Long updatePRTaskDownloadTime(PRTaskEntity taskEntity) {
        Update update = new Update();
        if (taskEntity.getDownloadConsuming() != null) {
            update.set("download_consuming", taskEntity.getDownloadConsuming());
        }
        Query query = Query.query(Criteria.where("scan_id").is(taskEntity.getScanId()));
        return mongoTemplate.updateFirst(query, update, CollectionTableName.POISON_PR_TASK).getModifiedCount();
    }

    /**
     * 下载完更新扫描结果
     *
     * @param antiEntity 参数
     * @param taskEntity 参数
     * @@return 结果
     */
    public Long updateTaskDownload(AntiEntity antiEntity, TaskEntity taskEntity) {
        Update update = new Update();
        if (antiEntity.getScanId() != null) {
            update.set("scan_id", antiEntity.getScanId());
        }
        if (antiEntity.getBranchRepositoryId() != null) {
            update.set("branch_repository_id", antiEntity.getBranchRepositoryId());
        }
        if (antiEntity.getLanguage() != null) {
            update.set("language", antiEntity.getLanguage());
        }
        if (antiEntity.getCreateTime() != null) {
            update.set("create_time", antiEntity.getCreateTime());
        }
        if (taskEntity.getExecutionStatus() != null) {
            update.set("execution_status", taskEntity.getExecutionStatus());
        }
        if (taskEntity.getTaskId() != null) {
            update.set("task_id", taskEntity.getTaskId());
        }
        //初始化总耗时
        if (taskEntity.getTimeConsuming() != null) {
            update.set("time_consuming", "");
        }
        Query query = Query.query(Criteria.where("task_id").is(taskEntity.getTaskId()));
        return mongoTemplate.updateFirst(query, update, CollectionTableName.POISON_VERSION_TASK).getModifiedCount();
    }

    /**
     * PR增量文件下载完更新扫描结果
     *
     * @param antiEntity 参数
     * @param taskEntity 参数
     * @@return 结果
     */
    public Long updatePRTaskDownload(AntiEntity antiEntity, TaskEntity taskEntity) {
        Update update = new Update();
        if (antiEntity.getScanId() != null) {
            update.set("scan_id", antiEntity.getScanId());
        }
        if (antiEntity.getLanguage() != null) {
            update.set("language", antiEntity.getLanguage());
        }
        if (antiEntity.getCreateTime() != null) {
            update.set("create_time", antiEntity.getCreateTime());
        }
        if (taskEntity.getExecutionStatus() != null) {
            update.set("execution_status", taskEntity.getExecutionStatus());
        }
        if (taskEntity.getTaskId() != null) {
            update.set("task_id", taskEntity.getTaskId());
        }
        //初始化总耗时
        if (taskEntity.getTimeConsuming() != null) {
            update.set("time_consuming", "");
        }
        Query query = Query.query(Criteria.where("task_id").is(taskEntity.getTaskId()));
        return mongoTemplate.updateFirst(query, update, CollectionTableName.POISON_VERSION_TASK).getModifiedCount();
    }

    /**
     * ID更新扫描结果
     *
     * @param antiEntity 参数
     * @param taskEntity 参数
     * @@return 结果
     */
    public Long updateTask(AntiEntity antiEntity, TaskEntity taskEntity) {
        Update update = new Update();
        if (taskEntity.getExecuteStartTime() != null) {
            update.set("execute_start_time", taskEntity.getExecuteStartTime());
        }
        if (taskEntity.getExecuteEndTime() != null) {
            update.set("execute_end_time", taskEntity.getExecuteEndTime());
        }
        if (taskEntity.getTaskConsuming() != null) {
            update.set("task_consuming", taskEntity.getTaskConsuming());
        }
        if (taskEntity.getTimeConsuming() != null) {
            update.set("time_consuming", taskEntity.getTimeConsuming());
        }
        if (antiEntity.getIsSuccess() != null) {
            update.set("is_success", antiEntity.getIsSuccess());
        }
        if (antiEntity.getIsPass() != null) {
            update.set("is_pass", antiEntity.getIsPass());
        }
        if (antiEntity.getResultCount() != null) {
            update.set("result_count", antiEntity.getResultCount());
        }
        if (antiEntity.getTips() != null) {
            update.set("tips", antiEntity.getTips());
        }
        if (antiEntity.getBranchRepositoryId() != null) {
            update.set("branch_repository_id", antiEntity.getBranchRepositoryId());
        }
        if (taskEntity.getLogs() != null) {
            update.set("logs", taskEntity.getLogs());
        }
        if (taskEntity.getExecutionStatus() != null) {
            update.set("execution_status", taskEntity.getExecutionStatus());
        }
        if (antiEntity.getSolveCount() != null) {
            update.set("solve_Count", antiEntity.getSolveCount());
        }
        if (antiEntity.getIssueCount() != null) {
            update.set("issue_count", antiEntity.getIssueCount());
        }
        Query query = Query.query(Criteria.where("task_id").is(taskEntity.getTaskId()));
        return mongoTemplate.updateFirst(query, update, CollectionTableName.POISON_VERSION_TASK).getModifiedCount();
    }

    /**
     * ID更新门禁扫描结果
     *
     * @param antiEntity 参数
     * @param taskEntity 参数
     * @@return 结果
     */
    public Long updatePRTask(PRAntiEntity antiEntity, PRTaskEntity taskEntity) {
        Update update = new Update();
        if (taskEntity.getExecuteStartTime() != null) {
            update.set("execute_start_time", taskEntity.getExecuteStartTime());
        }
        if (taskEntity.getExecuteEndTime() != null) {
            update.set("execute_end_time", taskEntity.getExecuteEndTime());
        }
        if (taskEntity.getTaskConsuming() != null) {
            update.set("task_consuming", taskEntity.getTaskConsuming());
        }
        if (taskEntity.getTimeConsuming() != null) {
            update.set("time_consuming", taskEntity.getTimeConsuming());
        }
        if (antiEntity.getIsSuccess() != null) {
            update.set("is_success", antiEntity.getIsSuccess());
        }
        if (antiEntity.getIsPass() != null) {
            update.set("is_pass", antiEntity.getIsPass());
        }
        if (antiEntity.getResultCount() != null) {
            update.set("result_count", antiEntity.getResultCount());
        }
        if (antiEntity.getTips() != null) {
            update.set("tips", antiEntity.getTips());
        }
        if (taskEntity.getLogs() != null) {
            update.set("logs", taskEntity.getLogs());
        }
        if (taskEntity.getExecutionStatus() != null) {
            update.set("execution_status", taskEntity.getExecutionStatus());
        }
        if (antiEntity.getSolveCount() != null) {
            update.set("solve_Count", antiEntity.getSolveCount());
        }
        if (antiEntity.getIssueCount() != null) {
            update.set("issue_count", antiEntity.getIssueCount());
        }
        Query query = Query.query(Criteria.where("scan_id").is(taskEntity.getScanId()));
        return mongoTemplate.updateFirst(query, update, CollectionTableName.POISON_PR_TASK).getModifiedCount();
    }

    /**
     * 查询一条版本扫描任务结果
     *
     * @param uuid 扫描ID
     * @return AntiEntity
     */
    public TaskEntity queryTaskEntity(String uuid) {
        Query query = Query.query(new Criteria("scan_id").is(uuid));
        return mongoTemplate.findOne(query, TaskEntity.class, CollectionTableName.POISON_VERSION_TASK);
    }

    /**
     * 查询一条门禁扫描任务结果
     *
     * @param scanId 扫描ID
     * @return AntiEntity
     */
    public PRTaskEntity queryPRTaskEntity(String scanId) {
        Query query = Query.query(new Criteria("scan_id").is(scanId));
        return mongoTemplate.findOne(query, PRTaskEntity.class, CollectionTableName.POISON_PR_TASK);
    }

    /**
     * 查询taskId结果
     *
     * @param antiEntity 参数
     * @return AntiEntity
     */
    public List<TaskEntity> queryTaskId(AntiEntity antiEntity) {
        Query query = Query.query(new Criteria("repo_name").is(antiEntity.getRepoName()));
        query.addCriteria(new Criteria("branch").is(antiEntity.getBranch()));
        query.addCriteria(new Criteria("project_name").is(antiEntity.getProjectName()));
        return mongoTemplate.find(query, TaskEntity.class, CollectionTableName.POISON_VERSION_TASK);
    }

    /**
     * 查询检测中心相关
     *
     * @param taskEntity 查询参数
     * @return queryTaskInfo
     */
    public PageVo queryTaskInfo(TaskEntity taskEntity) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotBlank(taskEntity.getProjectName())) {
            criteria.and("project_name").is(taskEntity.getProjectName());
        }
        if (StringUtils.isNotBlank(taskEntity.getRepoName())) {
            criteria.and("repo_name").is(taskEntity.getRepoName());
        }
        if (StringUtils.isNotBlank(taskEntity.getBranch())) {
            criteria.and("branch").is(taskEntity.getBranch());
        }
        if (taskEntity.getExecutionStatus() != null
                && StringUtils.isNotEmpty(String.valueOf(taskEntity.getExecutionStatus()))) {
            criteria.and("execution_status").is(taskEntity.getExecutionStatus());
        }
        Query query = Query.query(criteria);
        // 总数量
        query.with(Sort.by(Sort.Direction.DESC, "_id"));
        long count = mongoTemplate.count(query, TaskEntity.class, CollectionTableName.POISON_VERSION_TASK);
        List<TaskEntity> list = mongoTemplate.find(query, TaskEntity.class, CollectionTableName.POISON_VERSION_TASK);
        return new PageVo(count, list);
    }

    /**
     * 删除防投毒任务信息
     *
     * @param scanId 唯一参数值
     */
    public void delTask(String scanId) {
        mongoTemplate.remove(Query.query(Criteria.where("scan_id").is(scanId)), CollectionTableName.POISON_VERSION_TASK);
    }

    /**
     * 修改同社区统仓的语言
     *
     * @param antiEntity 扫描数据
     * @param language   语言
     * @return UpdateResult
     */
    public UpdateResult updateTaskLanguage(AntiEntity antiEntity, String language) {
        if (ObjectUtils.isEmpty(antiEntity)) {
            return null;
        }
        Query query = Query.query(Criteria.where("project_name").is(antiEntity.getProjectName())
                .and("repo_name").is(antiEntity.getRepoName()));
        Update update = new Update();
        if (StringUtils.isNotBlank(language)) {
            update.set("language", language);
        }
        return mongoTemplate.updateMulti(query, update, CollectionTableName.POISON_VERSION_TASK);
    }

    /**
     * 修改同社区统仓的语言
     *
     * @param prTaskEntity 扫描数据
     * @param language     语言
     * @return UpdateResult
     */
    public UpdateResult updatePRTaskLanguage(PRTaskEntity prTaskEntity, String language) {
        if (ObjectUtils.isEmpty(prTaskEntity)) {
            return null;
        }
        Query query = Query.query(Criteria.where("project_name").is(prTaskEntity.getProjectName())
                .and("repo_name").is(prTaskEntity.getRepoName()));
        Update update = new Update();
        if (StringUtils.isNotBlank(language)) {
            update.set("language", language);
        }
        return mongoTemplate.updateMulti(query, update, CollectionTableName.POISON_PR_TASK);
    }


    /**
     * @param tableName       表名称
     * @param repoUrlList     仓库地址列表
     * @return List<PoisonInspectionVo>
     */
    public List<PoisonInspectionVo> getPoisonTaskSummary(String tableName, List<String> repoUrlList) {
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("project_name").nin(ConstantsArgs.OPEN_MAJUN)));
        if (!CollectionUtils.isEmpty(repoUrlList)) {
            operations.add(Aggregation.match(Criteria.where("repo_url").in(repoUrlList)));
        }
        operations.add(Aggregation.sort(Sort.Direction.DESC, "create_time"));
        operations.add(Aggregation.group("project_name", "repo_name", "branch")
                .first("project_name").as("projectName")
                .first("repo_name").as("repoName")
                .first("branch").as("branch")
                .first("issue_count").as("issueCount")
                .first("solve_Count").as("solveCount"));
        List<PoisonInspectionVo> mappedResults = mongoTemplate.aggregate(Aggregation.newAggregation(operations), tableName, PoisonInspectionVo.class)
                .getMappedResults();
        return mappedResults;
    }
}
