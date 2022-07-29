package com.huawei.antipoisoning.business.controller;

import com.huawei.releasepoison.entity.RepoInfo;
import com.huawei.releasepoison.service.PoisonService;
import com.huawei.releasepoison.utils.MultiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhangshengjie
 */
@RestController
@RequestMapping(value = "/releasepoison")
public class PoisonController {

    @Autowired(required = false)
    private PoisonService poisonService;

    @RequestMapping(value = "/poisonScan",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.POST)
    public MultiResponse poisonScan(@RequestBody RepoInfo repoInfo){
        return poisonService.poisonScan(repoInfo);
    }
}
