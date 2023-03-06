/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huawei.antipoisoning.business.enmu.CollectionTableName;
import com.huawei.antipoisoning.business.enmu.CommonConstants;
import com.huawei.antipoisoning.business.enmu.ConstantsArgs;
import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.RepoInfo;
import com.huawei.antipoisoning.business.entity.ResultEntity;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.entity.checkrule.CheckRuleSet;
import com.huawei.antipoisoning.business.entity.checkrule.RuleModel;
import com.huawei.antipoisoning.business.entity.checkrule.RuleSetModel;
import com.huawei.antipoisoning.business.entity.checkrule.TaskRuleSetVo;
import com.huawei.antipoisoning.business.entity.pr.PullRequestInfo;
import com.huawei.antipoisoning.business.entity.vo.AntiPoisonChangeBoardModel;
import com.huawei.antipoisoning.business.entity.vo.AntiPoisonPrModel;
import com.huawei.antipoisoning.business.entity.vo.AntiPoisonRunStatusModel;
import com.huawei.antipoisoning.business.entity.vo.PageVo;
import com.huawei.antipoisoning.business.entity.vo.PoisonInspectionVo;
import com.huawei.antipoisoning.business.entity.vo.PoisonPrSummaryVo;
import com.huawei.antipoisoning.business.operation.CheckRuleOperation;
import com.huawei.antipoisoning.business.operation.PoisonResultOperation;
import com.huawei.antipoisoning.business.operation.PoisonScanOperation;
import com.huawei.antipoisoning.business.operation.PoisonTaskOperation;
import com.huawei.antipoisoning.business.service.AntiService;
import com.huawei.antipoisoning.business.service.PoisonService;
import com.huawei.antipoisoning.business.util.YamlUtil;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import com.huawei.antipoisoning.common.util.JGitUtil;
import com.huawei.antipoisoning.common.util.StreamConsumer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PoisonServiceImpl implements PoisonService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PoisonServiceImpl.class);

    @Autowired
    private AntiService antiService;

    @Autowired
    private PoisonScanOperation poisonScanOperation;

    @Autowired
    private PoisonResultOperation poisonResultOperation;

    @Autowired
    private CheckRuleOperation checkRuleOperation;

    @Autowired
    private PoisonTaskOperation poisonTaskOperation;

    /**
     * 启动扫扫描任务
     *
     * @param repoInfo 仓库主键id
     * @return poisonScan
     */
    @Override
    public MultiResponse poisonScan(RepoInfo repoInfo) {
        // 查询仓库语言和规则集
        List<TaskRuleSetVo> taskRuleSet = checkRuleOperation.getTaskRuleSet("", repoInfo.getProjectName(), repoInfo.getRepoName());
        Set<String> ruleIds = new LinkedHashSet<>();
        if (taskRuleSet.size() == 1) {
            for (CheckRuleSet checkRuleSet : taskRuleSet.get(0).getAntiCheckRules()) {
                RuleSetModel ruleSetModel = new RuleSetModel();
                ruleSetModel.setId(checkRuleSet.getRuleSetId());
                List<RuleSetModel> ruleSetModels = checkRuleOperation.queryRuleSet(ruleSetModel);
                if (ruleSetModels.size() > 0) {
                    ruleIds.addAll(ruleSetModels.get(0).getRuleIds());
                } else {
                    return new MultiResponse().code(400).message("ruleSet is error");
                }
            }
        } else {
            return new MultiResponse().code(400).message("taskRuleSet is error");
        }
        // 根据规则集查询规则详情
        PageVo allRules = checkRuleOperation.getAllRules(new RuleModel(), ruleIds);
        List<RuleModel> ruleModelList = allRules.getList();
        // 生成规则集yaml
        if (ruleModelList.size() == 0) {
            return new MultiResponse().code(400).message("rules is error");
        }
        // 加入通用规则
        RuleModel ruleModel = new RuleModel();
        ruleModel.setRuleLanguage("COMMON");
        PageVo commRules = checkRuleOperation.getAllRules(ruleModel, new LinkedHashSet<>());
        List<RuleModel> commList = commRules.getList();
        List<TaskRuleSetVo> ruleList = checkRuleOperation.getTaskRuleSet("", repoInfo.getProjectName(), repoInfo.getRepoName());
        List<String> languageList = new ArrayList<>();
        for (TaskRuleSetVo rule : ruleList) {
            List<CheckRuleSet> checkRuleSetList = rule.getAntiCheckRules();
            for (CheckRuleSet checkRuleSet : checkRuleSetList) {
                languageList.add(checkRuleSet.getLanguage());
            }
        }
        ruleModelList.addAll(commList);
        List<RuleModel> rulesMap = ruleModelList.stream().distinct().collect(Collectors.toList());
        String tableName = repoInfo.getProjectName() + "-" + repoInfo.getRepoName() + "-" + repoInfo.getRepoBranchName();
        if (YamlUtil.getRulesMap(rulesMap, tableName)) {
            //1.生成scanId
            String scanId = ScanIdGenerate(repoInfo.getProjectName(), repoInfo.getRepoName(), repoInfo.getRepoBranchName());
            //请求下载目标仓地址参数
            AntiEntity antiEntity = new AntiEntity();
            antiEntity.setScanId(scanId);
            antiEntity.setBranch(repoInfo.getRepoBranchName());
            antiEntity.setRepoUrl(repoInfo.getRepoUrl());
            antiEntity.setRepoName(repoInfo.getRepoName());
            antiEntity.setBranchRepositoryId(repoInfo.getId());
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < languageList.size(); i++) {
                stringBuffer.append(languageList.get(i));
                if (i < languageList.size() - 1) {
                    stringBuffer.append(" ");
                }
            }
            //同步同社区同仓库的语言配置
            poisonTaskOperation.updateTaskLanguage(antiEntity, stringBuffer.toString());
            antiEntity.setLanguage(stringBuffer.toString());
            antiEntity.setIsScan(true);
            antiEntity.setProjectName(repoInfo.getProjectName());
            antiEntity.setRulesName(tableName + ".yaml");
            antiEntity.setExecutorId(repoInfo.getExecutorId());
            antiEntity.setExecutorName(repoInfo.getExecutorName());
            // 下载目标仓库代码
            antiService.downloadRepo(antiEntity, repoInfo.getId());
            // 防投毒扫描
            antiService.scanRepo(scanId);
        } else {
            return new MultiResponse().code(400).message("create rule yaml is error");
        }
        return new MultiResponse().code(200).result("poisonScan start");
    }

    /**
     * 查询版本扫描任务列表信息。
     *
     * @param repoInfo 参数
     * @return MultiResponse
     */
    @Override
    public MultiResponse queryResults(RepoInfo repoInfo) {
        PageVo summaryVos = poisonScanOperation.queryResults(repoInfo);
        return new MultiResponse().code(200).result(summaryVos);
    }

    /**
     * 查询版本扫描任务结果详情信息。
     *
     * @param antiEntity 参数
     * @return MultiResponse
     */
    @Override
    public MultiResponse queryResultsDetail(AntiEntity antiEntity) {
        List<ResultEntity> resultEntity = poisonResultOperation.queryResultEntity(antiEntity.getScanId());
        return new MultiResponse().code(200).result(resultEntity);
    }

    /**
     * 随机码生成。
     *
     * @param community 社区名称
     * @param repoName  仓库名称
     * @param branch    分支名称
     * @return String 随机码
     */
    public String ScanIdGenerate(String community, String repoName, String branch) {
        long time = System.currentTimeMillis();
        return community + "-" + repoName + "-" + branch + "-" + time;
    }

    public String readUrl(String uuid) {
        String read;
        String readStr = "";
        try {
            URL url = new URL(YamlUtil.getToolPath() + "/tools/SoftwareSupplyChainSecurity-v1/poison_logs/" + uuid + ".txt");
            HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
            urlCon.setConnectTimeout(5000);
            urlCon.setReadTimeout(5000);
            BufferedReader br = new BufferedReader(new InputStreamReader(urlCon.getInputStream(), StandardCharsets.UTF_8));
            while ((read = br.readLine()) != null) {
                readStr = readStr + read;
            }
            br.close();
        } catch (IOException e) {
            readStr = e.toString();
        }
        return readStr;
    }

    /**
     * 检测中心主界面
     *
     * @param jsonObject 查询参数
     * @return queryTaskInfo
     */
    @Override
    public MultiResponse queryTaskInfo(JSONObject jsonObject) {
        Map<String, String> params = (Map<String, String>) jsonObject.get("antiModel");
        JSONObject json = new JSONObject();
        json.putAll(params);
        TaskEntity taskEntity = JSONObject.toJavaObject(json, TaskEntity.class);
        PageVo pageVo = poisonTaskOperation.queryTaskInfo(taskEntity);
        List<TaskEntity> taskEntities = pageVo.getList();
        //获取所有仓库信息
        // 查询任务所用的规则集信息
        List<RepoInfo> repoInfos = JSONObject.parseArray(
                JSON.toJSONString(jsonObject.get("repoInfos")), RepoInfo.class);
        //给所有已启动过的任务匹配一个仓库信息，以便检测中心启动
        for (TaskEntity task : taskEntities) {
            List<TaskRuleSetVo> taskRuleSet = checkRuleOperation.getTaskRuleSet("", task.getProjectName(), task.getRepoName());
            if (taskRuleSet.size() == CommonConstants.CommonNumber.NUMBER_ONE) {
                task.setTaskRuleSetVo(taskRuleSet.get(0));
                for (RepoInfo repoInfo : repoInfos) {
                    if (StringUtils.isNotBlank(repoInfo.getPoisonTaskId()) && repoInfo.getPoisonTaskId().equals(task.getTaskId())) {
                        task.setBranchRepositoryId(repoInfo.getId());
                    }
                }
                List<CheckRuleSet> checkRuleSet = taskRuleSet.get(0).getAntiCheckRules();
                List<String> language = new ArrayList<>();
                for (CheckRuleSet checkRuleSet1 : checkRuleSet) {
                    language.add(checkRuleSet1.getLanguage());
                }
                task.setLanguage(language.toString());
            }
        }
        if (taskEntity.getExecutionStatus() != null && taskEntity.getExecutionStatus() != 0) {
            return new MultiResponse().code(200).result(
                    new PageVo(Long.valueOf(taskEntities.size()), manualPaging(taskEntities, taskEntity)));
        }
        List<TaskEntity> result = new ArrayList<>();
        outer:
        for (RepoInfo repoInfo : repoInfos) {
            if (taskEntities.size() == 0 && StringUtils.isNotBlank(repoInfo.getPoisonTaskId())) {
                continue outer;
            }
            for (TaskEntity taskEntity1 : taskEntities) {
                //筛选出没跑过任务的仓库信息，赋予初始值
                if (taskEntity1.getProjectName().equals(repoInfo.getProjectName())
                        && taskEntity1.getRepoName().equals(repoInfo.getRepoName())
                        && taskEntity1.getBranch().equals(repoInfo.getRepoBranchName())) {
                    result.add(taskEntity1);
                    continue outer;
                }
            }
            TaskEntity taskEntityNew = new TaskEntity();
            taskEntityNew.setProjectName(repoInfo.getProjectName());
            taskEntityNew.setRepoName(repoInfo.getRepoName());
            taskEntityNew.setBranch(repoInfo.getRepoBranchName());
            taskEntityNew.setExecutionStatus(0);
            taskEntityNew.setBranchRepositoryId(repoInfo.getId());
            result.add(taskEntityNew);
        }
        return new MultiResponse().code(200).result(
                new PageVo(Long.valueOf(result.size()), manualPaging(result, taskEntity)));
    }

    /**
     * 删除防投毒任务以及相关规则集
     *
     * @param taskEntity 删除参数体
     * @return MultiResponse
     */
    @Override
    public MultiResponse delTask(TaskEntity taskEntity) {
        // 根据scanId查询出任务信息
        TaskEntity entity = poisonTaskOperation.queryTaskEntity(taskEntity.getScanId());
        if (Objects.nonNull(entity)) {
            // 删除版本级任务信息
            poisonTaskOperation.delTask(taskEntity.getScanId());
            // 查询该仓库是否有别的分支在检测
            TaskEntity task = new TaskEntity();
            task.setProjectName(entity.getProjectName());
            task.setRepoName(entity.getRepoName());
            List<TaskEntity> taskEntities = poisonTaskOperation.queryTaskInfo(task).getList();
            if (taskEntities.size() == CommonConstants.CommonNumber.NUMBER) {
                // 删除该任务所选相关规则集
                checkRuleOperation.delTaskRuleSet(task);
            }
        }
        return new MultiResponse().code(200).message("success");
    }


    /**
     * 查询日志
     * @param taskEntity
     * @return
     */
    @Override
    public MultiResponse queryTaskLog(TaskEntity taskEntity) {
        TaskEntity entity = poisonTaskOperation.queryTaskEntity(taskEntity.getScanId());
        return new MultiResponse().code(200).message("success").result(entity);
    }

    /**
     * 获取PR增量文件信息。
     *
     * @param info pr信息
     * @return MultiResponse
     */
    @Override
    public MultiResponse getPrDiff(PullRequestInfo info) {
        JGitUtil jGitUtil = new JGitUtil(info.getPullInfo(), info.getUser(), info.getPassword(),
                info.getBranch(), info.getVersion(), info.getWorkspace());
        jGitUtil.pullPr(info.getGitUrl());
        StringBuffer sb = jGitUtil.cmdOfPullRequest(info.getWorkspace(), info.getTarget());
        List<String> strList = new ArrayList<String>();
        try {
            LOGGER.info("get diff tree start!");
            Process proc = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", sb.toString()}, null, null);
            StreamConsumer errConsumer = new StreamConsumer(proc.getErrorStream(), strList,ConstantsArgs.ERR_CONSUMER);
            StreamConsumer outputConsumer = new StreamConsumer(proc.getInputStream(), strList,ConstantsArgs.OUTPUT_CONSUMER);
            errConsumer.start();
            outputConsumer.start();
            proc.waitFor();
            errConsumer.join();
            outputConsumer.join();
            LOGGER.info("get diff tree end!");
        } catch (IOException | InterruptedException e) {
            LOGGER.error("errInfo is {}", e.getMessage());
        }
        return MultiResponse.success(200, "success");
    }

    /**
     * 运维看板防投毒统计数据
     *
     * @param runStatusModel 防投毒运维看板数据数据统计查询实体类
     * @return
     */
    @Override
    public MultiResponse poisonRunstatusData(AntiPoisonRunStatusModel runStatusModel) {
        Map<String, Map<String, Long>> longResult = new HashMap<>();
        //查询防投毒任务表
        List<PoisonInspectionVo> poisonTaskSummary = poisonTaskOperation.getPoisonTaskSummary(CollectionTableName
                .POISON_VERSION_TASK, runStatusModel.getRepoUrlList());
        //版本级防投毒问题数
        Map<String, Long> poisonIssueCount = poisonTaskSummary.stream().collect(Collectors
                .groupingBy(PoisonInspectionVo::getProjectName, Collectors.summingLong(PoisonInspectionVo::getIssueCount)));
        longResult.put("poisonIssueCount", poisonIssueCount);
        //版本级防投毒已解决问题数
        Map<String, Long> poisonSolveCount = poisonTaskSummary.stream().collect(Collectors
                .groupingBy(PoisonInspectionVo::getProjectName, Collectors.summingLong(PoisonInspectionVo::getSolveCount)));
        longResult.put("poisonSolveCount", poisonSolveCount);
        //防投毒版本级仓库数
        List<PoisonInspectionVo> repoPosionSummary = poisonScanOperation.getRepoSummary(CollectionTableName
                .SCAN_RESULTS, runStatusModel.getRepoUrlList());
        Map<String, Long> poisonRepoCount = repoPosionSummary.stream().collect(Collectors.groupingBy(PoisonInspectionVo::getProjectName,
                Collectors.counting()));
        longResult.put("poisonRepoCount", poisonRepoCount);
        //防投毒门禁级仓库数
        List<PoisonInspectionVo> prPoisonSummary = poisonScanOperation.getRepoSummary(CollectionTableName
                .SCAN_PR_RESULTS, runStatusModel.getRepoUrlList());
        Map<String, Long> prPoisonRepoCount = prPoisonSummary.stream().collect(Collectors.groupingBy(PoisonInspectionVo::getProjectName,
                Collectors.counting()));
        longResult.put("prPoisonRepoCount", prPoisonRepoCount);
        return new MultiResponse().code(200).result(longResult);
    }

    /**
     * 防投毒运营数据统计
     *
     * @param changeBoardModel 查询实体
     * @return
     */
    @Override
    public MultiResponse poisonChangeBoardData(AntiPoisonChangeBoardModel changeBoardModel) {
        Map<String, Object> result = new HashMap<>();
        List<PoisonPrSummaryVo> prSummaryVos = poisonTaskOperation.poisonPrSummary(changeBoardModel);
        Optional.ofNullable(prSummaryVos).ifPresent(poisonPrSummaryVos -> {
            //防投毒门禁pe数据
            List<AntiPoisonPrModel> poisonPrModelList = poisonPrModelData(prSummaryVos);
            result.put("poisonPrModelList", poisonPrModelList);
            //防投毒门禁反馈时长
            poisonPrTime(result, prSummaryVos);
        });
        //防投毒版本合规
        poisonIssueCount(changeBoardModel, result);
        return new MultiResponse().code(200).result(result);
    }

    /**
     * 开源变革防投毒门禁查询
     *
     * @param changeBoardModel 查询实体
     * @return
     */
    @Override
    public MultiResponse poisonPrquery(AntiPoisonChangeBoardModel changeBoardModel) {
        List<PoisonPrSummaryVo> prSummaryVos = poisonTaskOperation.poisonPrSummary(changeBoardModel);
        List<AntiPoisonPrModel> poisonPrModelList = poisonPrModelData(prSummaryVos);
        Map<String, Object> result = new HashMap<>();
        if (!CollectionUtils.isEmpty(poisonPrModelList)) {
            result.put("poisonPrModelList", poisonPrModelList);
            return new MultiResponse().code(200).result(result);
        } else {
            return new MultiResponse().code(200).message("为查询到数据");
        }
    }


    /**
     * 开源变革防投毒门禁数据模型获取
     *
     * @param prSummaryVos 防投毒门禁vo
     */
    private List<AntiPoisonPrModel> poisonPrModelData(List<PoisonPrSummaryVo> prSummaryVos) {
        //查询总量prUrls
        List<AntiPoisonPrModel> poisonPrModelList = null;
        if (!CollectionUtils.isEmpty(prSummaryVos)) {
            poisonPrModelList = prSummaryVos.stream().map(pr -> {
                AntiPoisonPrModel antiPoisonPrModel = new AntiPoisonPrModel();
                antiPoisonPrModel.setProjectName(pr.getProjectName())
                        .setRepoName(pr.getRepoName())
                        .setResult(pr.getResult())
                        .setPrUrl(pr.getPrUrl());
                return antiPoisonPrModel;
            }).collect(Collectors.toList());
        }
        return poisonPrModelList;
    }

    /**
     * 防投毒版本问题总数
     *
     * @param changeBoardModel 查询实体
     * @param result
     */
    private void poisonIssueCount(AntiPoisonChangeBoardModel changeBoardModel, Map<String, Object> result) {
        //查询防投毒任务表
        List<PoisonInspectionVo> poisonTaskSummary = poisonTaskOperation.getPoisonTaskSummary(CollectionTableName
                .POISON_VERSION_TASK, changeBoardModel.getInfo());
        List<PoisonInspectionVo> collect = poisonTaskSummary.stream().filter(po -> ConstantsArgs.SPECIAL_PRO.contains(po.getProjectName()))
                .collect(Collectors.toList());
        Map<String, String> poisonIssueCount = new HashMap<>();
        //版本级防投毒问题数
        Map<String, Long> issueCount = collect.stream().collect(Collectors
                .groupingBy(PoisonInspectionVo::getProjectName, Collectors.summingLong(PoisonInspectionVo::getIssueCount)));
        Map<String, Long> poisonSolveCount = poisonTaskSummary.stream().collect(Collectors
                .groupingBy(PoisonInspectionVo::getProjectName, Collectors.summingLong(PoisonInspectionVo::getSolveCount)));
        issueCount.keySet().forEach(key -> {
            long totalCount = issueCount.get(key) + poisonSolveCount.get(key);
            poisonIssueCount.put(key, issueCount.get(key) + "/" + totalCount);
        });
        result.put("poisonIssueCount", poisonIssueCount);
    }

    /**
     * 门禁反馈时长
     *
     * @param result
     * @param prSummaryVos 防投毒门禁vo
     */
    private void poisonPrTime(Map<String, Object> result, List<PoisonPrSummaryVo> prSummaryVos) {
        List<PoisonPrSummaryVo> collect = prSummaryVos.stream().map(svo -> {
            String executeStartTime = svo.getExecuteStartTime();
            String executeEndTime = svo.getExecuteEndTime();
            DateTimeFormatter parseFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");
            long endTime = LocalDateTime.parse(executeEndTime, parseFormatter).atOffset(ZoneOffset.UTC).toInstant().toEpochMilli();
            long startTime = LocalDateTime.parse(executeStartTime, parseFormatter).atOffset(ZoneOffset.UTC).toInstant().toEpochMilli();
            svo.setExcuteTime(endTime - startTime);
            return svo;
        }).collect(Collectors.toList());
        Map<String, Double> poisonPrTime = collect.stream().collect(Collectors.groupingBy(PoisonPrSummaryVo::getProjectName, Collectors
                .averagingLong(PoisonPrSummaryVo::getExcuteTime)));        // 将时间转为分钟
        poisonPrTime.forEach((key, value) -> {
            poisonPrTime.put(key, Double.valueOf(String.format("%.2f", value / (60 * 1000))));
        });
        result.put("poisonPrTime", poisonPrTime);
    }

    /**
     * 检查中心列表手动分页
     *
     * @param results List<TaskEntity>
     * @param results List<TaskEntity>
     */
    public List<TaskEntity> manualPaging(List<TaskEntity> results, TaskEntity taskEntity) {
        int pageNum = taskEntity.getPageNum();
        int pageSize = taskEntity.getPageSize();
        if (pageSize == 1) {
            return results;
        } else if (pageSize >= 2) {
            int listSize = results.size();
            if (pageSize >= listSize && pageNum == 1) {
                return results;
            } else {
                int index = pageNum * pageSize - pageSize;
                List<TaskEntity> newResults = new ArrayList<>();
                for (int i = 0; i < pageSize; i++) {
                    if (index + i >= results.size()) {
                        break;
                    }
                    newResults.add(results.get(i + index));
                }
                return newResults;
            }
        }
        return Collections.emptyList();
    }
}
