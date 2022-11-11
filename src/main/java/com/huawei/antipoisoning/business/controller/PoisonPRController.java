/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.antipoisoning.business.enmu.ConstantsArgs;
import com.huawei.antipoisoning.business.entity.RepoInfo;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.entity.pr.PRAntiEntity;
import com.huawei.antipoisoning.business.entity.pr.PRInfo;
import com.huawei.antipoisoning.business.entity.pr.PullRequestInfo;
import com.huawei.antipoisoning.business.service.PoisonPRService;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * 防投毒controller。
 *
 * @since 2022-09-12
 * @author zhangshengjie
 */
@RestController
@RequestMapping(value = "/poison-pr")
public class PoisonPRController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PoisonPRController.class);
    private static final LinkedBlockingQueue<PRInfo> BLOCKING_QUEUE = new LinkedBlockingQueue<>(200);
    private static final ThreadPoolExecutor THREAD_SCHEDULED_EXECUTOR =
            new ThreadPoolExecutor(10, 200, 0,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<>(200));
    private static int CODE_SUCCESS = 200;
    private static int CODE_FAILED = 400;

    @Autowired(required = false)
    private PoisonPRService poisonService;

    /**
     * 启动PR门禁扫描任务
     *
     * @param info 检查PR分支信息
     * @return MultiResponse
     * @throws InterruptedException 中断异常
     * @throws ExecutionException 执行异常
     */
    @RequestMapping(value = "/sca-pr",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.POST)
    public MultiResponse poisonPRScan(@RequestBody PRInfo info) throws InterruptedException, ExecutionException {
        return queuePRService(info);
    }

    /**
     * 查询任务列表。
     *
     * @param repoInfo 参数
     * @return MultiResponse
     */
    @RequestMapping(value = "/query-pr-results",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.POST)
    public MultiResponse queryPRResults(@RequestBody RepoInfo repoInfo) {
        return poisonService.queryResults(repoInfo);
    }

    /**
     * 查询扫描结果详情信息。
     *
     * @param prAntiEntity 参数
     * @return MultiResponse
     */
    @RequestMapping(value = "/query-pr-results-detail",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.POST)
    public MultiResponse queryPRResultsDetail(@RequestBody PRAntiEntity prAntiEntity) {
        return poisonService.queryPRResultsDetail(prAntiEntity);
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
     * 门禁扫描队列
     *
     * @param prRepoInfo 任务实体类
     * @return MultiResponse
     * @throws InterruptedException 中断异常
     * @throws ExecutionException 执行异常
     */
    public MultiResponse queuePRService(PRInfo prRepoInfo) throws InterruptedException, ExecutionException {
        if (Objects.isNull(prRepoInfo) || BLOCKING_QUEUE.remainingCapacity() <= 0) {
            LOGGER.error("Blocking queue is full.");
        }
        try {
            BLOCKING_QUEUE.put(prRepoInfo);
        } catch (InterruptedException e) {
            LOGGER.error("{} Blocking queue put string failed.", e.getMessage());
        }
        Future future = THREAD_SCHEDULED_EXECUTOR.submit(() -> {
            PRInfo take = BLOCKING_QUEUE.take();
            return poisonService.poisonPRScan(take);
        });
        ObjectMapper objectMapper = new ObjectMapper();
        MultiResponse response;
        try {
            response = objectMapper.convertValue(future.get(3, TimeUnit.SECONDS), MultiResponse.class);
        } catch (TimeoutException e) {
            return new MultiResponse().code(ConstantsArgs.CODE_FAILED).message("create task failed!");
        }
        return response;
    }

    /**
     * 获取PR增量文件信息
     *
     * @param info pullRequest 信息
     * @return MultiResponse
     */
    @RequestMapping(value = "/pr-diff",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.POST)
    public MultiResponse getPrDiff(@RequestBody PRInfo info) {
        PullRequestInfo pullRequestInfo = poisonService.getPRInfo(info);
        return poisonService.getPrDiff(pullRequestInfo);
    }
}


