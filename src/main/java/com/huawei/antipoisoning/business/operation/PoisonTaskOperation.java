package com.huawei.antipoisoning.business.operation;

import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 扫描结果包裹数据存档
 *
 * @since: 2022/5/31 17:01
 */
@Component
public class PoisonTaskOperation {
    /**
     * 外部源接口返回任务数据
     */
    private static final String POISON_VERSION_TASK = "poison_version_task";

    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 保存扫描结果
     *
     * @param antiScan 扫描数据
     */
    public void insertTaskResult(AntiEntity antiScan, String taskId) {
        if (ObjectUtils.isEmpty(antiScan)) {
            return;
        }
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setTaskId(taskId);
        taskEntity.setBranch(antiScan.getBranch());
        taskEntity.setRepoName(antiScan.getRepoName());
        taskEntity.setRepoUrl(antiScan.getRepoUrl());
        taskEntity.setScanId(antiScan.getScanId());
        taskEntity.setProjectName(antiScan.getProjectName());
        taskEntity.setCreateTime(antiScan.getCreateTime());
        mongoTemplate.insert(taskEntity, POISON_VERSION_TASK);
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
        return mongoTemplate.updateFirst(query, update, POISON_VERSION_TASK).getModifiedCount();
    }

    /**
     * 保存扫描结果
     *
     * @param query 查询参数
     */
    public AntiEntity queryScanResult(Query query) { //查询入库结果
        return mongoTemplate.findOne(query, AntiEntity.class, POISON_VERSION_TASK);
    }

    /**
     * ID更新扫描结果
     *
     * @param taskEntity 参数
     * @@return 结果
     */
    public long updateTask(AntiEntity antiEntity, TaskEntity taskEntity) {
        Query query = Query.query(Criteria.where("scan_id").is(taskEntity.getTaskId()));
        Update update = new Update();
        if (antiEntity.getIsScan() != null) {
            update.set("is_scan", antiEntity.getIsScan());
        }
        if (antiEntity.getCreateTime() != null) {
            update.set("create_time", antiEntity.getCreateTime());
        }
        if (antiEntity.getRulesName() != null) {
            update.set("rules_name", antiEntity.getRulesName());
        }
        if (antiEntity.getIsScan() != null) {
            update.set("is_scan", antiEntity.getIsScan());
        }
        if (antiEntity.getIsDownloaded() != null) {
            update.set("is_download", antiEntity.getIsDownloaded());
        }
        if (antiEntity.getResultCount() != null) {
            update.set("result_count", antiEntity.getResultCount());
        }
        if (antiEntity.getTimeConsuming() != null) {
            update.set("time_consuming", antiEntity.getTimeConsuming());
        }
        if (taskEntity.getExecuteStartTime() != null) {
            update.set("execute_start_time", taskEntity.getExecuteStartTime());
        }
        if (taskEntity.getExecuteEndTime() != null) {
            update.set("execute_end_time", taskEntity.getExecuteEndTime());
        }
        return mongoTemplate.updateFirst(query, update, POISON_VERSION_TASK).getModifiedCount();
    }

    /**
     * 查询一条结果
     *
     * @return AntiEntity
     */
    public TaskEntity queryTaskEntity(String uuid) {
        Query query = Query.query(new Criteria("scan_id").is(uuid));
        return mongoTemplate.findOne(query, TaskEntity.class, POISON_VERSION_TASK);
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
        return mongoTemplate.find(query, TaskEntity.class, POISON_VERSION_TASK);
    }
}
