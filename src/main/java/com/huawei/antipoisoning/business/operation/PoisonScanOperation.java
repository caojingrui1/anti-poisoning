/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.operation;

import com.huawei.antipoisoning.business.enmu.CollectionTableName;
import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.RepoInfo;
import com.huawei.antipoisoning.business.entity.vo.PageVo;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 扫描操作。
 *
 * @author zyx
 * @since 2022-07-30
 */
@Component
public class PoisonScanOperation {
    @Resource
    private MongoTemplate mongoTemplate;


    public PageVo queryResults(RepoInfo repoInfo) {
        Criteria criteria = getCommonCriteria(repoInfo);
        Query commonQuery = Query.query(criteria);
        long count = mongoTemplate.count(commonQuery, AntiEntity.class,
                CollectionTableName.SCAN_RESULTS);
        commonQuery.with(Sort.by(Sort.Direction.DESC, "repo_name"));
        if (repoInfo.getPageSize() != null && repoInfo.getCurrentPage() != null) {
            commonQuery.skip((repoInfo.getCurrentPage() - 1) * repoInfo.getPageSize()).limit(repoInfo.getPageSize());
        }
        List<AntiEntity> summaryVos= mongoTemplate.find(commonQuery, AntiEntity.class, CollectionTableName.SCAN_RESULTS);

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
}
