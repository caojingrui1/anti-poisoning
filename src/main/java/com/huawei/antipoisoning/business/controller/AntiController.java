/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.controller;

import com.huawei.antipoisoning.business.enmu.ConstantsArgs;
import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.service.AntiService;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 下载仓库、扫描
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
     * @param uuid 扫描任务编号
     * @return MultiResponse
     */
    @RequestMapping("/scanRepo/{uuid}")
    public MultiResponse scanRepo(@PathVariable("uuid") String uuid) {
        return antiService.scanRepo(uuid);
    }

    /**
     * 下载仓库
     *
     * @param  antiEntity 参数
     * @param  id 扫描任务id
     * @return MultiResponse
     */
    @RequestMapping(value = "/downloadRepo", method = RequestMethod.POST)
    public MultiResponse downloadRepo(@RequestBody AntiEntity antiEntity, String id) {
        if (StringUtils.isEmpty(antiEntity.getScanId())) {
            return MultiResponse.error(ConstantsArgs.CODE_FAILED, "error: no scanId detected!");
        }
        if (StringUtils.isEmpty(antiEntity.getRepoUrl())) {
            return MultiResponse.error(ConstantsArgs.CODE_FAILED, "error: no repoUrl detected!");
        }
        if (StringUtils.isEmpty(antiEntity.getLanguage())) {
            return MultiResponse.error(ConstantsArgs.CODE_FAILED, "error: no language detected!");
        }
        return antiService.downloadRepo(antiEntity, id);
    }
}
