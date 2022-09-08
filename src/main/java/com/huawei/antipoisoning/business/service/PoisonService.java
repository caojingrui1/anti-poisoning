package com.huawei.antipoisoning.business.service;


import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.RepoInfo;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.common.entity.MultiResponse;

import java.io.IOException;

public interface PoisonService {

    MultiResponse poisonScan(RepoInfo repoInfo);

    MultiResponse queryResults(RepoInfo repoInfo);

    MultiResponse queryResultsDetail(AntiEntity antiEntity);

    MultiResponse selectLog(AntiEntity antiEntity) throws IOException;

    MultiResponse queryTaskInfo(TaskEntity taskEntity);

    MultiResponse delTask(TaskEntity taskEntity);
}
