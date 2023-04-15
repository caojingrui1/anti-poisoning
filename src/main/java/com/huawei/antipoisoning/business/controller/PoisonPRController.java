/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.antipoisoning.business.enmu.ConstantsArgs;
import com.huawei.antipoisoning.business.entity.RepoInfo;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.entity.pr.*;
import com.huawei.antipoisoning.business.service.PoisonPRService;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import com.huawei.antipoisoning.common.util.SecurityUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
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
    private static final LinkedBlockingQueue<PullRequestInfo> BLOCKING_QUEUE = new LinkedBlockingQueue<>(200);
    private static final ThreadPoolExecutor THREAD_SCHEDULED_EXECUTOR =
            new ThreadPoolExecutor(10, 200, 0,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<>(200));

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
        // 判断是否有调用api的token许可
        if (StringUtils.isNotEmpty(info.getApiToken())) {
            // 根据传入的apiToken判断社区来源，进行操作日志记录
            if (poisonService.checkApiToken(info.getApiToken())) {
                return queuePRService(info);
            } else {
                return new MultiResponse().code(ConstantsArgs.CODE_FAILED)
                        .message("create task failed, the apiToken is wrong!");
            }
        }
        return new MultiResponse().code(ConstantsArgs.CODE_FAILED)
                .message("create task failed, the apiToken is null!");
    }

    /**
     * 启动gitlab PR门禁扫描任务
     *
     * @param info 检查PR分支信息
     * @return MultiResponse
     * @throws InterruptedException 中断异常
     * @throws ExecutionException 执行异常
     */
    @RequestMapping(value = "/sca-gitlab-pr",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.POST)
    public MultiResponse poisonGitlabPRScan(@RequestBody GitlabPRInfo info) throws InterruptedException, ExecutionException {
        return queueGitlabPRService(info);
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
     * 查询扫描结果状态。
     *
     * @param queryInfo 查询参数
     * @return MultiResponse
     */
    @RequestMapping(value = "/query-pr-results-status",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.POST)
    public MultiResponse queryPRResultsStatus(@RequestBody QueryInfo queryInfo) {
        if (StringUtils.isNotEmpty(queryInfo.getApiToken())) {
            if (poisonService.checkApiToken(queryInfo.getApiToken())) {
                return poisonService.queryPRResultsStatus(queryInfo.getScanId());
            } else {
                return new MultiResponse().code(ConstantsArgs.CODE_FAILED)
                        .message("query task status failed, the apiToken is wrong!");
            }
        }

        return new MultiResponse().code(ConstantsArgs.CODE_FAILED)
                .message("query task status failed, the apiToken is null!");
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
        PullRequestInfo pullRequestInfo = poisonService.getPRInfo(prRepoInfo);
        if (Objects.isNull(pullRequestInfo) || BLOCKING_QUEUE.remainingCapacity() <= 0) {
            LOGGER.error("Blocking queue is full.");
        }
        try {
            BLOCKING_QUEUE.put(pullRequestInfo);
        } catch (InterruptedException e) {
            LOGGER.error("{} Blocking queue put string failed.", e.getMessage());
        }
        Future future = THREAD_SCHEDULED_EXECUTOR.submit(() -> {
            PullRequestInfo take = BLOCKING_QUEUE.take();
            return poisonService.poisonPRScan(take, prRepoInfo, null);
        });
        ObjectMapper objectMapper = new ObjectMapper();
        MultiResponse response;
        Map<String, Object> responseResult = new HashMap<>();
        try {
            response = objectMapper.convertValue(future.get(3, TimeUnit.SECONDS), MultiResponse.class);
        } catch (TimeoutException e) {
            responseResult.put("scanId", pullRequestInfo.getScanId());
            return new MultiResponse().code(ConstantsArgs.CODE_SUCCESS)
                    .message("create task success!").result(responseResult);
        }
        return response;
    }

    /**
     * Gitlab门禁扫描队列
     *
     * @param prRepoInfo 任务实体类
     * @return MultiResponse
     * @throws InterruptedException 中断异常
     * @throws ExecutionException 执行异常
     */
    public MultiResponse queueGitlabPRService(GitlabPRInfo prRepoInfo) throws InterruptedException, ExecutionException {
        PullRequestInfo pullRequestInfo = poisonService.getGitlabPrInfo(prRepoInfo);
        if (Objects.isNull(pullRequestInfo) || BLOCKING_QUEUE.remainingCapacity() <= 0) {
            LOGGER.error("Blocking queue is full.");
        }
        try {
            BLOCKING_QUEUE.put(pullRequestInfo);
        } catch (InterruptedException e) {
            LOGGER.error("{} Blocking queue put string failed.", e.getMessage());
        }
        Future future = THREAD_SCHEDULED_EXECUTOR.submit(() -> {
            PullRequestInfo take = BLOCKING_QUEUE.take();
            return poisonService.poisonPRScan(take, null, prRepoInfo);
        });
        ObjectMapper objectMapper = new ObjectMapper();
        MultiResponse response;
        try {
            response = objectMapper.convertValue(future.get(3, TimeUnit.SECONDS), MultiResponse.class);
        } catch (TimeoutException e) {
            return new MultiResponse().code(ConstantsArgs.CODE_SUCCESS)
                    .message("create task success!").result(pullRequestInfo.getScanId());
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


