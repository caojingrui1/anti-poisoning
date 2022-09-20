package com.huawei.antipoisoning.business.controller;

import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.service.AntiService;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 漏洞控制类
 *
 * @since: 2022/5/31 15:55
 */
@RestController
@RequestMapping("/antiPoisoning")
public class AntiController {
    @Autowired
    AntiService antiService;

    /**
     * 扫描已下载的仓库
     *
     * @return MultiResponse
     */
    @RequestMapping("/scanRepo/{uuid}")
    public MultiResponse scanRepo(@PathVariable("uuid") String uuid) {
        MultiResponse multiResponse =  antiService.scanRepo(uuid);
        return multiResponse;
    }

    /**
     * 下载仓库
     * @param  antiEntity 参数
     * @return MultiResponse
     */
    @RequestMapping(value = "/downloadRepo", method = RequestMethod.POST)
    public MultiResponse downloadRepo(@RequestBody AntiEntity antiEntity) {
        if (null == antiEntity.getScanId() || "".equals(antiEntity.getScanId())) {
            return MultiResponse.error(400,"error: no scanId detected!");
        }
        if (null == antiEntity.getRepoUrl() || "".equals(antiEntity.getRepoUrl())) {
            return MultiResponse.error(400,"error: no repoUrl detected!");
        }
        if (null == antiEntity.getLanguage() || "".equals(antiEntity.getLanguage())) {
            return MultiResponse.error(400,"error: no language detected!");
        }
        MultiResponse multiResponse =  antiService.downloadRepo(antiEntity);
        return multiResponse;
    }

    /**
     * 测试
     *
     * @return MultiResponse@PathVariable("id") String id
     */
    @RequestMapping(value = "/setEnv")
    public MultiResponse scanRepo1() {
        MultiResponse multiResponse =  antiService.setEnv();
        return multiResponse;
    }
}
