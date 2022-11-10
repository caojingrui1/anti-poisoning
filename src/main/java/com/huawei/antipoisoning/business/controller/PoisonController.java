/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.controller;

import com.alibaba.fastjson.JSONObject;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * 防投毒controller。
 *
 * @since 2022-09-12
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
     * @throws InterruptedException 中断异常
     * @throws ExecutionException 执行异常
     */
    @RequestMapping(value = "/poisonScan",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.POST)
    public MultiResponse poisonScan(@RequestBody RepoInfo repoInfo) throws InterruptedException, ExecutionException {
        return queueService(repoInfo);
    }

    /**
     * 查询任务列表。
     *
     * @param repoInfo 参数
     * @return MultiResponse
     */
    @RequestMapping(value = "/query-results",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.POST)
    public MultiResponse queryResults(@RequestBody RepoInfo repoInfo) {
        return poisonService.queryResults(repoInfo);
    }

    /**
     * 查询扫描结果详情信息。
     *
     * @param antiEntity 参数
     * @return MultiResponse
     */
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
     * @param jsonObject 查询参数
     * @return queryRuleSet
     */
    @PostMapping(value = "poison/query/task")
    public MultiResponse queryTaskInfo(@RequestBody JSONObject jsonObject) {
        return poisonService.queryTaskInfo(jsonObject);
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
     * @throws InterruptedException 中断异常
     * @throws ExecutionException 执行异常
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
            return poisonService.poisonScan(take);
        });
        ObjectMapper objectMapper = new ObjectMapper();
        MultiResponse response;
        try {
            response = objectMapper.convertValue(future.get(3, TimeUnit.SECONDS), MultiResponse.class);
        } catch (TimeoutException e) {
            return new MultiResponse().code(400).message("failed");
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
    public MultiResponse getPrDiff(@RequestBody PullRequestInfo info) {
        return poisonService.getPrDiff(info);
    }
}


