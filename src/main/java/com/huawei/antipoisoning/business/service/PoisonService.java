/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.service;

import com.alibaba.fastjson.JSONObject;
import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.pr.PullRequestInfo;
import com.huawei.antipoisoning.business.entity.RepoInfo;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.common.entity.MultiResponse;

public interface PoisonService {

    MultiResponse poisonScan(RepoInfo repoInfo);

    MultiResponse queryResults(RepoInfo repoInfo);

    MultiResponse queryResultsDetail(AntiEntity antiEntity);

    MultiResponse queryTaskInfo(JSONObject jsonObject);

    MultiResponse delTask(TaskEntity taskEntity);

    MultiResponse getPrDiff(PullRequestInfo info);
}
