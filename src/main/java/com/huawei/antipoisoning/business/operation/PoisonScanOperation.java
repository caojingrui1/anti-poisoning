/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.operation;

import com.huawei.antipoisoning.business.enmu.CollectionTableName;
import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.RepoInfo;
import com.huawei.antipoisoning.business.entity.vo.PageVo;
import com.huawei.antipoisoning.business.entity.vo.PoisonInspectionVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 扫描操作。
 *
 * @author zyx
 * @since 2022-07-30
 */
@Component
public class PoisonScanOperation {
    @Autowired
    @Qualifier("poisonMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * 查询版本扫描任务。
     *
     * @param repoInfo 查询参数
     * @return PageVo
     */
    public PageVo queryResults(RepoInfo repoInfo) {
        Criteria criteria = getCommonCriteria(repoInfo);
        Query commonQuery = Query.query(criteria);
        long count = mongoTemplate.count(commonQuery, AntiEntity.class,
                CollectionTableName.SCAN_RESULTS);
        commonQuery.with(Sort.by(Sort.Direction.DESC, "repo_name"));
        if (repoInfo.getPageSize() != null && repoInfo.getCurrentPage() != null) {
            commonQuery.skip((repoInfo.getCurrentPage() - 1) * repoInfo.getPageSize()).limit(repoInfo.getPageSize());
        }
        List<AntiEntity> summaryVos = mongoTemplate.find(commonQuery, AntiEntity.class, CollectionTableName.SCAN_RESULTS);

        return new PageVo(count, summaryVos);
    }

    /**
     * 查询门禁扫描任务。
     *
     * @param repoInfo 查询参数
     * @return PageVo
     */
    public PageVo queryPRResults(RepoInfo repoInfo) {
        Criteria criteria = getCommonCriteria(repoInfo);
        Query commonQuery = Query.query(criteria);
        long count = mongoTemplate.count(commonQuery, AntiEntity.class,
                CollectionTableName.SCAN_RESULTS);
        commonQuery.with(Sort.by(Sort.Direction.DESC, "repo_name"));
        if (repoInfo.getPageSize() != null && repoInfo.getCurrentPage() != null) {
            commonQuery.skip((repoInfo.getCurrentPage() - 1) * repoInfo.getPageSize()).limit(repoInfo.getPageSize());
        }
        List<AntiEntity> summaryVos = mongoTemplate.find(commonQuery, AntiEntity.class, CollectionTableName.SCAN_PR_RESULTS);

        return new PageVo(count, summaryVos);
    }

    /**
     * 获取公共查询条件
     *
     * @return repoInfo 查询条件
     */
    private Criteria getCommonCriteria(RepoInfo repoInfo) {
        return new Criteria();
    }

    /**
     * 查询防投毒扫描结果
     * @param tableName
     * @param repoList
     * @param projectNameList
     * @return
     */
    public List<PoisonInspectionVo> getRepoSummary(String tableName, List<String> repoList, List<String> projectNameList) {

        List<AggregationOperation> operations = new ArrayList<>();
        if (repoList.size() != 0) {
            operations.add(Aggregation.match(Criteria.where("repo_name").in(repoList)));
            operations.add(Aggregation.match(Criteria.where("project_name").in(projectNameList)));
        }
        operations.add(Aggregation.sort(Sort.Direction.DESC, "create_time"));
        operations.add(Aggregation.group("project_name", "repo_name")
                .first("project_name").as("projectName")
                .first("repo_name").as("repoName"));

        List<PoisonInspectionVo> mappedResults = mongoTemplate.aggregate(Aggregation.newAggregation(operations), tableName, PoisonInspectionVo.class)
                .getMappedResults();

        return mappedResults;
    }
}
