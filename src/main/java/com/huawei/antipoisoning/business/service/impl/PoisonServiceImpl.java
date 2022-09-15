package com.huawei.antipoisoning.business.service.impl;


import com.huawei.antipoisoning.business.enmu.CommonConstants;
import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.RepoInfo;
import com.huawei.antipoisoning.business.entity.ResultEntity;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.entity.checkRule.CheckRuleSet;
import com.huawei.antipoisoning.business.entity.checkRule.RuleModel;
import com.huawei.antipoisoning.business.entity.checkRule.RuleSetModel;
import com.huawei.antipoisoning.business.entity.checkRule.TaskRuleSetVo;
import com.huawei.antipoisoning.business.entity.vo.PageVo;
import com.huawei.antipoisoning.business.operation.CheckRuleOperation;
import com.huawei.antipoisoning.business.operation.PoisonResultOperation;
import com.huawei.antipoisoning.business.operation.PoisonScanOperation;
import com.huawei.antipoisoning.business.operation.PoisonTaskOperation;
import com.huawei.antipoisoning.business.operation.RepoOperation;
import com.huawei.antipoisoning.business.service.AntiService;
import com.huawei.antipoisoning.business.service.PoisonService;
import com.huawei.antipoisoning.business.util.YamlUtil;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import com.huawei.antipoisoning.common.util.AntiMainUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class PoisonServiceImpl implements PoisonService {

    @Autowired
    private AntiService antiService;

    @Autowired
    private PoisonScanOperation poisonScanOperation;

    @Autowired
    private PoisonResultOperation poisonResultOperation;

    @Autowired
    private RepoOperation repoOperation;

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
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        System.out.println("linux path --- " + path.replace("file:", ""));
        System.out.println(System.getProperty("user.dir"));
        // 获取仓库信息
        RepoInfo info = repoOperation.getById(repoInfo);
        info.setExecutorId(repoInfo.getExecutorId());
        info.setExecutorName(repoInfo.getExecutorName());
        repoInfo = info;
        // 查询仓库语言和规则集
        List<TaskRuleSetVo> taskRuleSet = checkRuleOperation.getTaskRuleSet("", repoInfo.getProjectName(), repoInfo.getRepoName());
        List<String> ruleIds = new ArrayList<>();
        if (taskRuleSet.size() == 1) {
            for (CheckRuleSet checkRuleSet : taskRuleSet.get(0).getAntiCheckRules()) {
                RuleSetModel ruleSetModel = new RuleSetModel();
                ruleSetModel.setId(checkRuleSet.getRuleSetId());
                List<RuleSetModel> ruleSetModels = checkRuleOperation.queryRuleSet(ruleSetModel);
                if (ruleSetModels.size() == 1) {
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
        List<TaskRuleSetVo> ruleList = checkRuleOperation.getTaskRuleSet("", repoInfo.getProjectName(), repoInfo.getRepoName());
        List<String> languageList = new ArrayList<>();
        for (TaskRuleSetVo rule : ruleList) {
            List<CheckRuleSet> checkRuleSetList = rule.getAntiCheckRules();
            for (CheckRuleSet checkRuleSet : checkRuleSetList) {
                languageList.add(checkRuleSet.getLanguage());
            }
        }
        ruleModelList.addAll(commList);
        String tableName = repoInfo.getProjectName() + "-" + repoInfo.getRepoName() + "-" + repoInfo.getRepoBranchName();
        if (YamlUtil.getRulesMap(ruleModelList, tableName)) {
            //1.生成scanId
            String scanId = ScanIdGenerate(repoInfo.getProjectName(), repoInfo.getRepoName(), repoInfo.getRepoBranchName());
            //请求下载目标仓地址参数
            AntiEntity antiEntity = new AntiEntity();
            antiEntity.setScanId(scanId);
            antiEntity.setBranch(repoInfo.getRepoBranchName());
            antiEntity.setRepoUrl(repoInfo.getRepoUrl());
            antiEntity.setRepoName(repoInfo.getRepoName());
            antiEntity.setLanguage(languageList.toString());
            antiEntity.setIsScan(true);
            antiEntity.setProjectName(repoInfo.getProjectName());
            antiEntity.setRulesName(tableName + ".yaml");
            antiEntity.setExecutorId(repoInfo.getExecutorId());
            antiEntity.setExecutorName(repoInfo.getExecutorName());
            // 下载目标仓库代码
            antiService.downloadRepo(antiEntity);
            // 防投毒扫描
            antiService.scanRepo(scanId);
        } else {
            return new MultiResponse().code(400).message("create rule yaml is error");
        }
        return new MultiResponse().code(200).result("poisonScan start");
    }

    @Override
    public MultiResponse queryResults(RepoInfo repoInfo) {
        PageVo summaryVos = poisonScanOperation.queryResults(repoInfo);
        return new MultiResponse().code(200).result(summaryVos);
    }

    @Override
    public MultiResponse queryResultsDetail(AntiEntity antiEntity) {
        List<ResultEntity> resultEntity = poisonResultOperation.queryResultEntity(antiEntity.getScanId());
        return new MultiResponse().code(200).result(resultEntity);
    }

    @Override
    public MultiResponse selectLog(AntiEntity antiEntity) throws IOException {
        String url = "/usr/local/anti-poisoning/tools/SoftwareSupplyChainSecurity-v1/poison_logs";
        return new MultiResponse().code(200).result(AntiMainUtil.getTxtContent(url, antiEntity.getScanId()));
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
        String scanId = community + "-" + repoName + "-" + branch + "-" + time;
        return scanId;
    }

    public String readUrl(String uuid) {
        String read;
        String readStr = "";
        try {
            URL url = new URL(YamlUtil.getToolPath() + "/tools/SoftwareSupplyChainSecurity-v1/poison_logs/" + uuid + ".txt");
            HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
            urlCon.setConnectTimeout(5000);
            urlCon.setReadTimeout(5000);
            BufferedReader br = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
            while ((read = br.readLine()) != null) {
                readStr = readStr + read;
            }
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            readStr = e.toString();
        }
        return readStr;
    }

    /**
     * 检测中心主界面
     *
     * @param taskEntity 查询参数
     * @return queryTaskInfo
     */
    @Override
    public MultiResponse queryTaskInfo(TaskEntity taskEntity) {
        PageVo pageVo = poisonTaskOperation.queryTaskInfo(taskEntity);
        List<TaskEntity> taskEntities = pageVo.getList();
        //获取所有仓库信息
        RepoInfo repoInfoTask = new RepoInfo();
        repoInfoTask.setProjectName(taskEntity.getProjectName());
        repoInfoTask.setRepoName(taskEntity.getRepoName());
        repoInfoTask.setRepoBranchName(taskEntity.getBranch());
        List<RepoInfo> repoInfos = repoOperation.getRepoByInfo(repoInfoTask);
        // 查询任务所用的规则集信息
        //给所有已启动过的任务匹配一个仓库信息，以便检测中心启动
        for (TaskEntity task : taskEntities) {
            List<TaskRuleSetVo> taskRuleSet = checkRuleOperation.getTaskRuleSet("", task.getProjectName(), task.getRepoName());
            if (taskRuleSet.size() == CommonConstants.CommonNumber.NUMBER_ONE) {
                task.setTaskRuleSetVo(taskRuleSet.get(0));
                RepoInfo repoInfo2 = new RepoInfo();
                repoInfo2.setProjectName(task.getProjectName());
                repoInfo2.setRepoName(task.getRepoName());
                repoInfo2.setRepoBranchName(task.getBranch());
                List<RepoInfo> result = repoOperation.getRepoByInfo(repoInfo2);
                task.setBranchRepositoryId(result.get(0).getId());
            }
        }
        if (Objects.nonNull(taskEntity.getIsSuccess())){
            return new MultiResponse().code(200).result(taskEntities);
        }
        List<TaskEntity> result = new ArrayList<>();
        for (RepoInfo repoInfo : repoInfos){
            if(taskEntities.size()==0){
                TaskEntity taskEntityNew = new TaskEntity();
                taskEntityNew.setProjectName(repoInfo.getProjectName());
                taskEntityNew.setRepoName(repoInfo.getRepoName());
                taskEntityNew.setBranch(repoInfo.getRepoBranchName());
                result.add(taskEntityNew);
            }
            for (TaskEntity taskEntity1 : taskEntities){
                //筛选出没跑过任务的仓库信息，赋予初始值
                if (repoInfo.getId().equals(taskEntity1.getBranchRepositoryId())){
                    result.add(taskEntity1);
                }else {
                    TaskEntity taskEntityNew = new TaskEntity();
                    taskEntityNew.setProjectName(repoInfo.getProjectName());
                    taskEntityNew.setRepoName(repoInfo.getRepoName());
                    taskEntityNew.setBranch(repoInfo.getRepoBranchName());
                    result.add(taskEntityNew);
                }
            }
        }
        return new MultiResponse().code(200).result(result);
    }

    /**
     * 删除防投毒任务以及相关规则集
     *
     * @param taskEntity 删除参数体
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
}
