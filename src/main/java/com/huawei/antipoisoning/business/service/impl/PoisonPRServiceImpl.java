/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.antipoisoning.business.enmu.CommonConstants;
import com.huawei.antipoisoning.business.enmu.ConstantsArgs;
import com.huawei.antipoisoning.business.entity.RepoInfo;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.entity.checkrule.*;
import com.huawei.antipoisoning.business.entity.pr.*;
import com.huawei.antipoisoning.business.entity.vo.PageVo;
import com.huawei.antipoisoning.business.operation.CheckRuleOperation;
import com.huawei.antipoisoning.business.operation.PoisonResultOperation;
import com.huawei.antipoisoning.business.operation.PoisonScanOperation;
import com.huawei.antipoisoning.business.operation.PoisonTaskOperation;
import com.huawei.antipoisoning.business.service.AntiService;
import com.huawei.antipoisoning.business.service.PoisonPRService;
import com.huawei.antipoisoning.business.util.YamlUtil;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import com.huawei.antipoisoning.common.util.GiteeApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;


@Service
public class PoisonPRServiceImpl implements PoisonPRService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PoisonPRServiceImpl.class);

    private static final String INC_URL = ("prod".equals(ConstantsArgs.DB_ENV)
            ? ConstantsArgs.MAJUN_URL  : ConstantsArgs.MAJUN_BETA_URL) + ConstantsArgs.MAJUN_POISON_INC;

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

    @Value("${git.username}")
    private String gitUser;

    @Value("${git.password}")
    private String gitPass;

    /**
     * 启动扫扫描任务
     *
     * @param info 仓库主键id
     * @return poisonScan
     */
    @Override
    public MultiResponse poisonPRScan(PRInfo info) {
        PullRequestInfo pullRequestInfo = getPRInfo(info);
        // 查询仓库语言和规则集
        List<TaskRuleSetVo> taskRuleSet = checkRuleOperation.getTaskRuleSet("", pullRequestInfo.getProjectName(), pullRequestInfo.getRepoName());
        List<String> ruleIds = new ArrayList<>();
        if (taskRuleSet.size() == 1) {
            for (CheckRuleSet checkRuleSet : taskRuleSet.get(0).getAntiCheckRules()) {
                RuleSetModel ruleSetModel = new RuleSetModel();
                ruleSetModel.setId(checkRuleSet.getRuleSetId());
                List<RuleSetResult> ruleSetModels = checkRuleOperation.queryRuleSet(ruleSetModel);
                if (ruleSetModels.size() == 1 && (!("通用检查规则集").equals(ruleSetModels.get(0).getTemplateName()))) {
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
        PageVo commRules = checkRuleOperation.getAllRules(ruleModel, new ArrayList<>());
        List<RuleModel> commList = commRules.getList();
        List<TaskRuleSetVo> ruleList = checkRuleOperation.getTaskRuleSet("",
                pullRequestInfo.getProjectName(), pullRequestInfo.getRepoName());
        List<String> languageList = new ArrayList<>();
        for (TaskRuleSetVo rule : ruleList) {
            List<CheckRuleSet> checkRuleSetList = rule.getAntiCheckRules();
            for (CheckRuleSet checkRuleSet : checkRuleSetList) {
                languageList.add(checkRuleSet.getLanguage());
            }
        }
        ruleModelList.addAll(commList);
        String tableName = pullRequestInfo.getProjectName() + "-" +
                pullRequestInfo.getRepoName() + "-" + pullRequestInfo.getBranch();
        if (YamlUtil.getRulesMap(ruleModelList, tableName)) {
            //1.生成scanId
            String scanId = ScanIdGenerate(pullRequestInfo.getProjectName(), pullRequestInfo.getRepoName(), pullRequestInfo.getBranch());
            //请求下载PR代码地址参数
            PRAntiEntity prAntiEntity = new PRAntiEntity();
            prAntiEntity.setScanId(scanId);
            prAntiEntity.setProjectName(pullRequestInfo.getProjectName());
            prAntiEntity.setRepoName(pullRequestInfo.getRepoName());
            prAntiEntity.setBranch(pullRequestInfo.getBranch());
            prAntiEntity.setRepoUrl(pullRequestInfo.getGitUrl());
            prAntiEntity.setPrUrl(pullRequestInfo.getMergeUrl());
            prAntiEntity.setPrNumber(pullRequestInfo.getPullNumber());
            prAntiEntity.setExecutorName(pullRequestInfo.getExecutorName());
            prAntiEntity.setExecutorId(pullRequestInfo.getExecutorId());
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < languageList.size(); i++) {
                stringBuffer.append(languageList.get(i));
                if (i < languageList.size() - 1) {
                    stringBuffer.append(" ");
                }
            }
            //同步同社区同仓库的语言配置
            prAntiEntity.setLanguage(stringBuffer.toString());
            prAntiEntity.setIsScan(true);
            prAntiEntity.setProjectName(pullRequestInfo.getProjectName());
            prAntiEntity.setRulesName(tableName + ".yaml");
            // 下载目标仓库代码,下载目标PR增量代码
            JSONArray fileArray = getPRDiffFile(info);
            antiService.downloadPRRepoFile(prAntiEntity, pullRequestInfo, fileArray);
            // 防投毒扫描
            antiService.scanPRFile(scanId, pullRequestInfo);
            return new MultiResponse().code(200).result(INC_URL + scanId +
                    "/" + prAntiEntity.getProjectName() + "/" + prAntiEntity.getRepoName());
        } else {
            return new MultiResponse().code(400).message("create rule yaml is error");
        }
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
     * @param prAntiEntity 参数
     * @return MultiResponse
     */
    @Override
    public MultiResponse queryPRResultsDetail(PRAntiEntity prAntiEntity) {
        List<PRResultEntity> resultEntity = poisonResultOperation.queryPRResultEntity(prAntiEntity.getScanId());
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
     * 获取PR增量文件信息。
     *
     * @param info pr信息
     * @return MultiResponse
     */
    @Override
    public MultiResponse getPrDiff(PullRequestInfo info) {
        return MultiResponse.success(200, "success");
    }

    /**
     * 通过构建参数调用Gitee API获取PR信息
     *
     * @param prInfo PR参数信息
     * @return map
     */
    @Override
    public PullRequestInfo getPRInfo(PRInfo prInfo) {
        Map<String, String> params = new HashMap<>();
        params.put("owner", prInfo.getProjectName());
        params.put("repo", prInfo.getRepoName());
        params.put("pullNumber", prInfo.getPullNumber());
        params.put("accessToken", prInfo.getAccessToken());
        GiteeApiUtil giteeApiUtil = new GiteeApiUtil(params);
        JSONObject pullRequestInfo = giteeApiUtil.getPullRequestInfo();
        // 通过Giturl查询
        PullRequestInfo info = new PullRequestInfo();
        if (pullRequestInfo.isEmpty()) {
            LOGGER.debug("Get the pullRequestInfo failed!");
            return null;
        } else {
            JSONObject head = JSONObject.parseObject(pullRequestInfo.get("head").toString()); // 源信息
            JSONObject headUser = JSONObject.parseObject(head.get("user").toString());
            JSONObject headRepos = JSONObject.parseObject(head.get("repo").toString());
            JSONObject headReposNameSpace = JSONObject.parseObject(headRepos.get("namespace").toString());
            JSONObject base = JSONObject.parseObject(pullRequestInfo.get("base").toString()); // 目标信息
            JSONObject baseRepos = JSONObject.parseObject(base.get("repo").toString());
            JSONObject baseReposNameSpace = JSONObject.parseObject(baseRepos.get("namespace").toString());
            info.setPullNumber(pullRequestInfo.get("number").toString()); // pr编号
            info.setVersion(head.get("sha").toString()); // sha值
            info.setTarget(base.get("ref").toString()); // 目标分支
            info.setBranch(head.get("ref").toString());
            info.setProjectName("openMajun_enterprise".equals(baseReposNameSpace.get("path").toString()) ? "openMajun" :
                    baseReposNameSpace.get("path").toString()); // 目标社区名
            info.setRepoName(baseRepos.get("path").toString());
            info.setPullInfo("pull/" + pullRequestInfo.get("number").toString() + "/MERGE"); // pr信息
            info.setGitUrl(baseRepos.get("html_url").toString()); // 目标仓库下载地址
            info.setMergeUrl(pullRequestInfo.get("html_url").toString()); // pr地址
            info.setWorkspace(headReposNameSpace.get("path").toString() + "-" +
                    headRepos.get("path").toString() + "-" + head.get("ref").toString());
            info.setExecutorName(headUser.get("name").toString()); // pr请求发起者
            info.setExecutorId(headUser.get("id").toString()); // pr请求发起者ID
            info.setUser(gitUser);
            info.setPassword(gitPass);
            return info;
        }
    }

    /**
     * 获取差异文件数。
     *
     * @param prInfo pr信息
     * @return JSONArray
     */
    public JSONArray getPRDiffFile(PRInfo prInfo) {
        Map<String, String> params = new HashMap<>();
        params.put("owner", prInfo.getProjectName());
        params.put("repo", prInfo.getRepoName());
        params.put("pullNumber", prInfo.getPullNumber());
        params.put("accessToken", prInfo.getAccessToken());
        GiteeApiUtil giteeApiUtil = new GiteeApiUtil(params);
        JSONArray fileArray = giteeApiUtil.getPrDiffFiles();
        return fileArray;
    }
}
