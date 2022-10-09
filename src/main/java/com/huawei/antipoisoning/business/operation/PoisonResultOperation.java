package com.huawei.antipoisoning.business.operation;

import com.huawei.antipoisoning.business.enmu.CollectionTableName;
import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.ResultEntity;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 扫描结果包裹数据存档
 *
 * @since: 2022/5/31 17:01
 */
@Component
public class PoisonResultOperation {
    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 保存扫描结果
     *
     * @param resultEntity 扫描数据
     */
    public void insertResultDetails(ResultEntity resultEntity) {
        if (ObjectUtils.isEmpty(resultEntity)) {
            return;
        }
        mongoTemplate.insert(resultEntity, CollectionTableName.SCAN_RESULT_DETAILS);
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
        if (antiEntity.getCreateTime() != null) {
            update.set("create_time", antiEntity.getCreateTime());
        }
        return mongoTemplate.updateFirst(query, update, CollectionTableName.SCAN_RESULT_DETAILS).getModifiedCount();
    }

    /**
     * 保存扫描结果
     *
     * @param query 查询参数
     */
    public AntiEntity queryScanResult(Query query) { //查询入库结果
        return mongoTemplate.findOne(query, AntiEntity.class, CollectionTableName.SCAN_RESULT_DETAILS);
    }

    /**
     * ID更新扫描结果
     *
     * @param taskEntity 参数
     * @@return 结果
     */
    public long updateTask(TaskEntity taskEntity) {
        Query query = Query.query(Criteria.where("scan_id").is(taskEntity.getTaskId()));
        Update update = new Update();
        if (taskEntity.getExecuteStartTime() != null) {
            update.set("execute_start_time", taskEntity.getExecuteStartTime());
        }
        if (taskEntity.getExecuteEndTime() != null) {
            update.set("execute_end_time", taskEntity.getExecuteEndTime());
        }
        return mongoTemplate.updateFirst(query, update, CollectionTableName.SCAN_RESULT_DETAILS).getModifiedCount();
    }

    /**
     * 查询一条结果
     *
     * @return AntiEntity
     */
    public List<ResultEntity> queryResultEntity(String uuid) {
        Query query = Query.query(new Criteria("scan_id").is(uuid));
        return mongoTemplate.find(query, ResultEntity.class, CollectionTableName.SCAN_RESULT_DETAILS);
    }

    /**
     * 查询taskId结果
     *
     * @return AntiEntity
     */
    public List<TaskEntity> queryTaskId(AntiEntity antiEntity) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotBlank(antiEntity.getRepoName())) {
            criteria.and("repo_name").is(antiEntity.getRepoName());
        }
        if (StringUtils.isNotBlank(antiEntity.getBranch())) {
            criteria.and("branch").is(antiEntity.getBranch());
        }
        if (StringUtils.isNotBlank(antiEntity.getProjectName())) {
            criteria.and("project_name").is(antiEntity.getProjectName());
        }
        return mongoTemplate.find(Query.query(criteria), TaskEntity.class, CollectionTableName.POISON_VERSION_TASK);
    }

    /**
     * 根据hash获取屏蔽数据量
     *
     * @param hash 问题的唯一hash值
     */
    public int getResultDetailByHash(String hash, String taskId) {
        Criteria criteria = Criteria.where("hash").is(hash).and("status").is("2");
        criteria.and("task_id").is(taskId);
        return mongoTemplate.find(Query.query(criteria), ResultEntity.class,
                CollectionTableName.SCAN_RESULT_DETAILS).size();
    }

    /**
     * 查询状态为已屏蔽的问题数
     *
     * @param status 屏蔽状态
     */
    public int getCountByStatus(String status, String scanId) {
        Criteria criteria = Criteria.where("status").is(status).and("scan_id").is(scanId);
        return mongoTemplate.find(Query.query(criteria), ResultEntity.class, CollectionTableName.SCAN_RESULT_DETAILS)
                .size();
    }
}
