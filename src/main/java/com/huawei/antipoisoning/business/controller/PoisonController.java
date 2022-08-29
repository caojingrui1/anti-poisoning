package com.huawei.antipoisoning.business.controller;


import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.RepoInfo;
import com.huawei.antipoisoning.business.service.PoisonService;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author zhangshengjie
 */
@RestController
@RequestMapping(value = "/releasepoison")
public class PoisonController {

    @Autowired(required = false)
    private PoisonService poisonService;

    /**
     * 启动扫描任务
     *
     * @param repoInfo 检查仓库
     * @return MultiResponse
     */
    @RequestMapping(value = "/poisonScan",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.POST)
    public MultiResponse poisonScan(@RequestBody RepoInfo repoInfo){
        return poisonService.poisonScan(repoInfo);
    }

    @RequestMapping(value = "/query-results",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.POST)
    public MultiResponse queryResults(@RequestBody RepoInfo repoInfo){
        return poisonService.queryResults(repoInfo);
    }

    @RequestMapping(value = "/query-results-detail",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.POST)
    public MultiResponse queryResultsDetail(@RequestBody AntiEntity antiEntity){
        return poisonService.queryResultsDetail(antiEntity);
    }

    @RequestMapping(value = "/selectLog",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.POST)
    public MultiResponse selectLog(@RequestBody AntiEntity antiEntity) throws IOException {
        return poisonService.selectLog(antiEntity);
    }
}
