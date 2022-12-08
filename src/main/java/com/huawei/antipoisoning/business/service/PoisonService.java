/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.service;

import com.alibaba.fastjson.JSONObject;
import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.pr.PullRequestInfo;
import com.huawei.antipoisoning.business.entity.RepoInfo;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.entity.vo.AntiPoisonRunStatusModel;
import com.huawei.antipoisoning.common.entity.MultiResponse;

public interface PoisonService {
    /**
     * 启动扫扫描任务
     *
     * @param repoInfo 仓库主键id
     * @return poisonScan
     */
    MultiResponse poisonScan(RepoInfo repoInfo);

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
     * @param antiEntity 参数
     * @return MultiResponse
     */
    MultiResponse queryResultsDetail(AntiEntity antiEntity);

    /**
     * 检测中心主界面
     *
     * @param jsonObject 查询参数
     * @return queryTaskInfo
     */
    MultiResponse queryTaskInfo(JSONObject jsonObject);

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
     * 运维看板防投毒统计数据
      * @param runStatusModel 防投毒运维看板数据数据统计查询实体类
     * @return
     */
    MultiResponse poisonRunstatusData(AntiPoisonRunStatusModel runStatusModel);
}
