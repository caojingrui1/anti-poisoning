package com.huawei.antipoisoning.business.operation;

import com.huawei.antipoisoning.business.enmu.CollectionTableName;
import com.huawei.antipoisoning.business.entity.RepoInfo;
import org.apache.commons.lang.StringUtils;
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
     * @param repoInfo 仓库参数
     * @return getById
     */
    public RepoInfo getById(RepoInfo repoInfo) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotBlank(repoInfo.getId())) {
            criteria.and("_id").is(repoInfo.getId());
        }
        if (StringUtils.isNotBlank(repoInfo.getRepoUrl())) {
            criteria.and("repo_url").is(repoInfo.getRepoUrl());
        }
        if (StringUtils.isNotBlank(repoInfo.getRepoBranchName())) {
            criteria.and("manifest_branch_name").is(repoInfo.getRepoBranchName());
        }
        return mongoTemplate.findOne(Query.query(criteria), RepoInfo.class, CollectionTableName.BRANCH_REPO);
    }
}
