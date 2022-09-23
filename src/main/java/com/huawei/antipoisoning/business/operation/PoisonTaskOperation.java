package com.huawei.antipoisoning.business.operation;

import com.huawei.antipoisoning.business.enmu.CollectionTableName;
import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.entity.vo.PageVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 扫描结果包裹数据存档
 *
 * @since: 2022/5/31 17:01
 */
@Component
public class PoisonTaskOperation {
    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 保存扫描结果
     *
     * @param antiEntity 扫描数据
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
     * 保存扫描结果
     *
     * @param antiEntity 扫描数据
     */
    public long updateTaskResult(AntiEntity antiEntity, String taskId) {
        if (ObjectUtils.isEmpty(antiEntity)) {
            return Long.valueOf(null);
        }
        Query query = Query.query(Criteria.where("task_id").is(taskId));
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
        return mongoTemplate.updateFirst(query, update, CollectionTableName.POISON_VERSION_TASK).getModifiedCount();
    }

    /**
     * 下载完更新扫描结果
     *
     * @param taskEntity 参数
     * @@return 结果
     */
    public Long updateTaskDownloadTime(TaskEntity taskEntity) {
        Query query = Query.query(Criteria.where("task_id").is(taskEntity.getTaskId()));
        Update update = new Update();
        if (taskEntity.getDownloadConsuming() != null) {
            update.set("download_consuming", taskEntity.getDownloadConsuming());
        }
        return mongoTemplate.updateFirst(query, update, CollectionTableName.POISON_VERSION_TASK).getModifiedCount();
    }

    /**
     * 下载完更新扫描结果
     *
     * @param taskEntity 参数
     * @@return 结果
     */
    public Long updateTaskDownload(AntiEntity antiEntity, TaskEntity taskEntity) {
        Query query = Query.query(Criteria.where("task_id").is(taskEntity.getTaskId()));
        Update update = new Update();
        if (antiEntity.getScanId() != null) {
            update.set("scan_id", antiEntity.getScanId());
        }
        if (antiEntity.getCreateTime() != null) {
            update.set("create_time", antiEntity.getCreateTime());
        }
        if (taskEntity.getExecutionStatus() != null) {
            update.set("execution_status", taskEntity.getExecutionStatus());
        }
        return mongoTemplate.updateFirst(query, update, CollectionTableName.POISON_VERSION_TASK).getModifiedCount();
    }

    /**
     * ID更新扫描结果
     *
     * @param taskEntity 参数
     * @@return 结果
     */
    public Long updateTask(AntiEntity antiEntity, TaskEntity taskEntity) {
        Query query = Query.query(Criteria.where("task_id").is(taskEntity.getTaskId()));
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
        return mongoTemplate.updateFirst(query, update, CollectionTableName.POISON_VERSION_TASK).getModifiedCount();
    }

    /**
     * 查询一条结果
     *
     * @return AntiEntity
     */
    public TaskEntity queryTaskEntity(String uuid) {
        Query query = Query.query(new Criteria("scan_id").is(uuid));
        return mongoTemplate.findOne(query, TaskEntity.class, CollectionTableName.POISON_VERSION_TASK);
    }

    /**
     * 查询taskId结果
     *
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
        if (Objects.nonNull(taskEntity.getIsSuccess())) {
            criteria.and("is_success").is(taskEntity.getIsSuccess());
        }
        Query query = Query.query(criteria);
        // 总数量
        long count = mongoTemplate.count(query, TaskEntity.class, CollectionTableName.POISON_VERSION_TASK);
//        if (taskEntity.getPageNum() != null && taskEntity.getPageSize() != null && count > 0) {
//            query.skip((long) (taskEntity.getPageNum() - 1) * taskEntity.getPageSize());
//            query.limit(taskEntity.getPageSize());
//        }
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
}
