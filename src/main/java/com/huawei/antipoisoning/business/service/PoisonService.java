package com.huawei.antipoisoning.business.service;


import com.huawei.antipoisoning.business.entity.RepoInfo;
import com.huawei.antipoisoning.common.entity.MultiResponse;

public interface PoisonService {

    MultiResponse poisonScan(RepoInfo repoInfo);

    MultiResponse queryResults(RepoInfo repoInfo);
}
