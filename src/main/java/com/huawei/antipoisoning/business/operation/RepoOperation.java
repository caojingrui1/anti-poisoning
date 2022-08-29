package com.huawei.antipoisoning.business.operation;

import com.huawei.antipoisoning.business.enmu.CollectionTableName;
import com.huawei.antipoisoning.business.entity.RepoInfo;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 仓库相关查询类
 *
 * @author cqx
 * @since 2022/8/2510:44
 */
@Component
public class RepoOperation {
    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * @param branchId 仓库主键id
     * @return getById
     */
    public RepoInfo getById(String branchId) {
        return mongoTemplate.findOne(Query.query(Criteria.where("_id").is(branchId)), RepoInfo.class, CollectionTableName.BRANCH_REPO);
    }
}
