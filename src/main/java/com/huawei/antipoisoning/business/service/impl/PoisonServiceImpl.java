package com.huawei.antipoisoning.business.service.impl;


import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.RepoInfo;
import com.huawei.antipoisoning.business.entity.ResultEntity;
import com.huawei.antipoisoning.business.entity.checkRule.CheckRuleSet;
import com.huawei.antipoisoning.business.entity.checkRule.RuleModel;
import com.huawei.antipoisoning.business.entity.checkRule.RuleSetModel;
import com.huawei.antipoisoning.business.entity.checkRule.TaskRuleSetVo;
import com.huawei.antipoisoning.business.entity.vo.PageVo;
import com.huawei.antipoisoning.business.operation.CheckRuleOperation;
import com.huawei.antipoisoning.business.operation.PoisonResultOperation;
import com.huawei.antipoisoning.business.operation.PoisonScanOperation;
import com.huawei.antipoisoning.business.operation.RepoOperation;
import com.huawei.antipoisoning.business.service.AntiService;
import com.huawei.antipoisoning.business.service.PoisonService;
import com.huawei.antipoisoning.business.util.YamlUtil;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import com.huawei.antipoisoning.common.util.AntiMainUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        String id = repoInfo.getId();
        repoInfo = repoOperation.getById(id);
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
        ruleModelList.addAll(commList);
        if (YamlUtil.getRulesMap(ruleModelList)) {
            //1.生成scanId
            String scanId = ScanIdGenerate(repoInfo.getProjectName(), repoInfo.getRepoName(), repoInfo.getRepoBranchName());
            //请求下载目标仓地址参数
            AntiEntity antiEntity = new AntiEntity();
            antiEntity.setScanId(scanId);
            antiEntity.setBranch(repoInfo.getRepoBranchName());
            antiEntity.setRepoUrl(repoInfo.getRepoUrl());
            antiEntity.setRepoName(repoInfo.getRepoName());
            antiEntity.setIsScan(true);
            antiEntity.setProjectName(repoInfo.getProjectName());
            antiEntity.setRulesName("check_anti.yaml");
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
        String url = "/root/opt/SoftwareSupplyChainSecurity-v1/poison_logs/";
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
}
