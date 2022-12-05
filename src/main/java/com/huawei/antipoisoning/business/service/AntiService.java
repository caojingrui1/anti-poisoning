/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.service;

import com.alibaba.fastjson.JSONArray;
import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.pr.PRAntiEntity;
import com.huawei.antipoisoning.business.entity.pr.PullRequestInfo;
import com.huawei.antipoisoning.common.entity.MultiResponse;


/**
 * 下载仓库、扫描
 *
 * @since: 2022/5/30 16:22
 */
public interface AntiService {
    /**
     * 扫描仓库
     *
     * @param uuid 任务id
     * @return MultiResponse
     */
    MultiResponse scanRepo(String uuid);

    /**
     * 门禁扫描文件
     *
     * @param scanId 任务id
     * @param info pr信息
     * @return MultiResponse<List<VmsExternalCveSourceDTO>>
     */
    MultiResponse scanPRFile(String scanId, PullRequestInfo info);

    /**
     * 下载仓库
     *
     * @param antiEntity 扫描任务实体
     * @param id 仓库id
     * @return MultiResponse
     */
    MultiResponse downloadRepo(AntiEntity antiEntity, String id);

    /**
     * 下载PR增量文件
     *
     * @param antiEntity 扫描任务实体
     * @param info pr信息
     * @param fileArray 差异文件数
     * @return MultiResponse<List<VmsExternalCveSourceDTO>>
     */
    MultiResponse downloadPRRepoFile(PRAntiEntity antiEntity, PullRequestInfo info, JSONArray fileArray);
}
