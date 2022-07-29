package com.huawei.antipoisoning.business.service;

import com.huawei.releasepoison.entity.RepoInfo;
import com.huawei.releasepoison.utils.MultiResponse;

public interface PoisonService {

    MultiResponse poisonScan(RepoInfo repoInfo);
}
