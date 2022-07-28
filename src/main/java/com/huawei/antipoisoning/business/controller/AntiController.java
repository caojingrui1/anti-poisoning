package com.huawei.antipoisoning.business.controller;

import com.huawei.antipoisoning.business.service.AntiService;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * 执行漏洞
     *
     * @return MultiResponse@PathVariable("id") String id
     */
    @RequestMapping("/scanRepo/{repoName}/{language}")
    public MultiResponse scanRepo(@PathVariable("repoName") String repoUrl, @PathVariable("language") String language){
        antiService.scanRepo(repoUrl, language);
        return MultiResponse.success(200,"success");
    }

}
