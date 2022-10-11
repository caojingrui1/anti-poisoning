package com.huawei.antipoisoning.business.service;


import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.common.entity.MultiResponse;

import java.util.List;

/**
 * vms接口服务
 *
 * @since: 2022/5/30 16:22
 */
public interface AntiService {
    /**
     * 扫描仓库
     *
     * @param uuid 任务id
     * @return MultiResponse<List<VmsExternalCveSourceDTO>>
     */
    MultiResponse scanRepo(String uuid);

    /**
     * 查询外部源漏洞
     *
     * @return MultiResponse<List<VmsExternalCveSourceDTO>>
     */
    MultiResponse setEnv();

    /**
     * 下载仓库
     *
     * @param antiEntity 扫描任务实体
     * @return MultiResponse<List<VmsExternalCveSourceDTO>>
     */
    MultiResponse downloadRepo(AntiEntity antiEntity, String id);
}
