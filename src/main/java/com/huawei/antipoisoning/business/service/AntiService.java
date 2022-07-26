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
     * 查询外部源漏洞
     *
     * @param vmsQueryInfoDTO 查询主体
     * @return MultiResponse<List<VmsExternalCveSourceDTO>>
     */
    MultiResponse<List<AntiEntity>> getSourceOut(AntiEntity vmsQueryInfoDTO);
    MultiResponse executionVuln();
}
