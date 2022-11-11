/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.service;

import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.RepoInfo;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.entity.pr.PRAntiEntity;
import com.huawei.antipoisoning.business.entity.pr.PRInfo;
import com.huawei.antipoisoning.business.entity.pr.PullRequestInfo;
import com.huawei.antipoisoning.common.entity.MultiResponse;

public interface PoisonPRService {
    /**
     * 启动扫扫描任务
     *
     * @param pullRequestInfo pr详情信息
     * @param info pr详情信息
     * @return poisonScan
     */
    MultiResponse poisonPRScan(PullRequestInfo pullRequestInfo, PRInfo info);

    /**
     * 查询版本扫描任务列表信息。
     *
     * @param repoInfo 参数
     * @return MultiResponse
     */
    MultiResponse queryResults(RepoInfo repoInfo);

    /**
     * 查询版本扫描任务结果详情信息。
     *
     * @param prAntiEntity 参数
     * @return MultiResponse
     */
    MultiResponse queryPRResultsDetail(PRAntiEntity prAntiEntity);

    /**
     * 删除防投毒任务以及相关规则集
     *
     * @param taskEntity 删除参数体
     * @return MultiResponse
     */
    MultiResponse delTask(TaskEntity taskEntity);

    /**
     * 获取PR增量文件信息。
     *
     * @param info pr信息
     * @return MultiResponse
     */
    MultiResponse getPrDiff(PullRequestInfo info);

    /**
     * 获取PR信息。
     *
     * @param info pr信息
     * @return MultiResponse
     */
    PullRequestInfo getPRInfo(PRInfo info);
}
