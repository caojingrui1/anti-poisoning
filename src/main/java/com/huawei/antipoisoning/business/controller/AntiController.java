package com.huawei.antipoisoning.business.controller;

import com.huawei.antipoisoning.business.service.AntiService;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 漏洞控制类
 *
 * @since: 2022/5/31 15:55
 */
@RestController
@RequestMapping("/vulnController")
public class AntiController {
    @Autowired
    AntiService antiService;

    /**
     * 执行漏洞
     *
     * @return MultiResponse
     */
    @RequestMapping("/executionVuln")
    public MultiResponse executionVuln(){
        antiService.executionVuln();
        return MultiResponse.success(200,"success");
    }

}
