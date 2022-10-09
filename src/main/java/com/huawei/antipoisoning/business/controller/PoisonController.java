package com.huawei.antipoisoning.business.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.pr.PullRequestInfo;
import com.huawei.antipoisoning.business.entity.RepoInfo;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.service.PoisonService;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author zhangshengjie
 */
@RestController
@RequestMapping(value = "/releasepoison")
public class PoisonController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PoisonController.class);
    private static final LinkedBlockingQueue<RepoInfo> BLOCKING_QUEUE = new LinkedBlockingQueue<>(200);
    private static final ThreadPoolExecutor THREAD_SCHEDULED_EXECUTOR =
            new ThreadPoolExecutor(10, 200, 0,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<>(200));

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
    public MultiResponse poisonScan(@RequestBody RepoInfo repoInfo) throws InterruptedException, ExecutionException {
        return queueService(repoInfo);
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
    public MultiResponse queueService(RepoInfo repoInfo) throws InterruptedException, ExecutionException {
        if (Objects.isNull(repoInfo) || BLOCKING_QUEUE.remainingCapacity() <= 0) {
            LOGGER.error("Blocking queue is full.");
        }
        try {
            BLOCKING_QUEUE.put(repoInfo);
        } catch (InterruptedException e) {
            LOGGER.error("{} Blocking queue put string failed." + e.getMessage());
        }
        Future future = THREAD_SCHEDULED_EXECUTOR.submit(() -> {
            RepoInfo take = BLOCKING_QUEUE.take();
            poisonService.poisonScan(take);
            return poisonService.poisonScan(take);
        });
        ObjectMapper objectMapper = new ObjectMapper();
        MultiResponse response;
        try {
            response = objectMapper.convertValue(future.get(3, TimeUnit.SECONDS), MultiResponse.class);
        }catch (Exception e){
            return new MultiResponse().code(200).message("success");
        }
        return response;
    }

    /**
     * 启动扫描任务
     *
     * @param info pullRequest 信息
     * @return MultiResponse
     */
    @RequestMapping(value = "/pr-diff",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.POST)
    public MultiResponse getPrDiff(@RequestBody PullRequestInfo info) throws InterruptedException {
        return poisonService.getPrDiff(info);
    }
}


