package com.huawei.antipoisoning.business.controller;


import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.RepoInfo;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.service.PoisonService;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author zhangshengjie
 */
@RestController
@RequestMapping(value = "/releasepoison")
public class PoisonController {

    private static final LinkedBlockingQueue<RepoInfo> BLOCKING_QUEUE = new LinkedBlockingQueue<>(200);
    private static final ThreadPoolExecutor  THREAD_SCHEDULED_EXECUTOR = new ThreadPoolExecutor(1, 200, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(200));


    @Autowired(required = false)
    private PoisonService poisonService;

    /**
     * 启动扫描任务
     *
     * @param repoInfo 检查仓库
     * @return MultiResponse
     */
    @RequestMapping(value = "/poisonScan",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.POST)
    public MultiResponse poisonScan(@RequestBody RepoInfo repoInfo) throws InterruptedException {
        queueService(repoInfo);
        return new MultiResponse().result("success");
    }

    @RequestMapping(value = "/query-results",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.POST)
    public MultiResponse queryResults(@RequestBody RepoInfo repoInfo){
        return poisonService.queryResults(repoInfo);
    }

    @RequestMapping(value = "/query-results-detail",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.POST)
    public MultiResponse queryResultsDetail(@RequestBody AntiEntity antiEntity) {
        return poisonService.queryResultsDetail(antiEntity);
    }

    @RequestMapping(value = "/selectLog",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.POST)
    public MultiResponse selectLog(@RequestBody AntiEntity antiEntity) throws IOException {
        return poisonService.selectLog(antiEntity);
    }

    /**
     * 获取检测中心（主界面）
     *
     * @param taskEntity 查询参数
     * @return queryRuleSet
     */
    @PostMapping(value = "poison/query/task")
    public MultiResponse queryTaskInfo(@RequestBody TaskEntity taskEntity) {
        return poisonService.queryTaskInfo(taskEntity);
    }

    /**
     * 删除任务
     *
     * @param taskEntity 任务实体类
     * @return createRuleSet
     */
    @PostMapping(value = "/del/task")
    public MultiResponse delRuleSet(@RequestBody TaskEntity taskEntity) {
        return poisonService.delTask(taskEntity);
    }

    /**
     * 队列
     *
     * @param repoInfo 任务实体类
     * @return MultiResponse
     */
    public void queueService(RepoInfo repoInfo) throws InterruptedException {
        if (Objects.isNull(repoInfo) || BLOCKING_QUEUE.remainingCapacity() <= 0) {
            System.err.println("Blocking queue is full.");
        }

        try {
            BLOCKING_QUEUE.put(repoInfo);
        } catch (InterruptedException e) {
            System.err.println("Blocking queue put string failed.");
            e.printStackTrace();
        }

        THREAD_SCHEDULED_EXECUTOR.submit(new Thread(() -> {
            while (BLOCKING_QUEUE.size() > 0) {
                try {
                    RepoInfo take = BLOCKING_QUEUE.take();
                    MultiResponse multiResponse = poisonService.poisonScan(take);
                    System.out.println(multiResponse);
                    Thread.sleep(1000);
                    System.out.println("The task had complete.");
                    System.out.println("-------------------------------------------");
                } catch (InterruptedException e) {
                    System.err.println("Blocking queue take string failed.");
                    e.printStackTrace();
                }
            }
        }));
    }
}


