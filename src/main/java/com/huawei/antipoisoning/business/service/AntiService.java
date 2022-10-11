package com.huawei.antipoisoning.business.service;


import com.huawei.antipoisoning.business.entity.AntiEntity;
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
     * 测试
     *
     * @return MultiResponse
     */
    MultiResponse setEnv();

    /**
     * 下载仓库
     *
     * @param antiEntity 扫描任务实体
     * @param id 仓库id
     * @return MultiResponse
     */
    MultiResponse downloadRepo(AntiEntity antiEntity, String id);
}
