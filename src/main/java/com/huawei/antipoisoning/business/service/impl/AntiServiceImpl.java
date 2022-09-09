package com.huawei.antipoisoning.business.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.ResultEntity;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.operation.AntiOperation;
import com.huawei.antipoisoning.business.operation.PoisonResultOperation;
import com.huawei.antipoisoning.business.operation.PoisonTaskOperation;
import com.huawei.antipoisoning.business.service.AntiService;
import com.huawei.antipoisoning.business.util.YamlUtil;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import com.huawei.antipoisoning.common.util.AntiMainUtil;
import com.huawei.antipoisoning.common.util.JGitUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * vms接口服务实现类
 *
 * @since: 2022/5/30 16:22
 */
@Service("vmsService")
public class AntiServiceImpl implements AntiService {

    private static final String SCANRESULTPATH = "/tools/softwareFile/report/";

    private static final String SCANTOOLPATH = "/tools/SoftwareSupplyChainSecurity-v1/openeuler_scan.py";

    private static final String SCANTOOLFILE = "/tools/SoftwareSupplyChainSecurity-v1/";

    private static final String REPOPATH = "/tools/softwareFile/download/";

    private static final String CONFIG_PATH = "/tools/SoftwareSupplyChainSecurity-v1/ruleYaml/";

    @Value("${git.username}")
    private String gitUser;

    @Value("${git.password}")
    private String gitPassword;

    @Autowired
    private AntiOperation antiOperation;

    @Autowired
    private PoisonTaskOperation poisonTaskOperation;

    @Autowired
    private PoisonResultOperation poisonResultOperation;

    /**
     * 执行漏洞
     *
     * @return MultiResponse
     */
    @Override
    public MultiResponse scanRepo(String uuid) {
        AntiEntity antiEntity = antiOperation.queryAntiEntity(uuid);
        TaskEntity taskEntity = poisonTaskOperation.queryTaskEntity(uuid);
        //扫描指定仓库 下载后放入文件夹 扫描 产生报告
        if (null != antiEntity) {
            try {
                if (antiEntity.getIsDownloaded() == true) {
                    String[] arguments = new String[]{"/bin/sh", "-c",
                            "python3"
                                    // 工具地址
                                    + " " + YamlUtil.getToolPath() + SCANTOOLPATH
                                    // 仓库下载后存放地址
                                    + " " + YamlUtil.getToolPath() + REPOPATH + "/" + antiEntity.getRepoName() +
                                    // 扫描完成后结果存放地址   /usr/result/openeuler-os-build
                                    " " + YamlUtil.getToolPath() + SCANRESULTPATH + antiEntity.getRepoName() + ".json " +
                                    // 支持多语言规则扫描
                                    "--custom-yaml " + YamlUtil.getToolPath() + CONFIG_PATH + antiEntity.getRulesName()
                                    + " > " + YamlUtil.getToolPath() + SCANTOOLFILE +
                                    "poison_logs/" + uuid + ".txt"};
                    System.out.println(arguments[2]);
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                    long startTime = System.currentTimeMillis();
                    String taskStartTime = df.format(startTime);
                    taskEntity.setExecuteStartTime(taskStartTime);
                    String sb = AntiMainUtil.execute(arguments);
                    long endTime = System.currentTimeMillis();
                    String taskEndTime = df.format(endTime);
                    taskEntity.setExecuteEndTime(taskEndTime);
                    String taskConsuming = String.valueOf((endTime - startTime) / 1000);
                    taskEntity.setTaskConsuming(taskConsuming + "s");
                    System.out.println("sb ==== :" + sb);
                    String result = AntiMainUtil.getJsonContent(YamlUtil.getToolPath() + SCANRESULTPATH, antiEntity.getRepoName());
                    System.out.println(result);
                    List<ResultEntity> results = JSONArray.parseArray(result, ResultEntity.class);
                    for (ResultEntity resultEntity : results) {
                        resultEntity.setProjectName(antiEntity.getProjectName());
                        resultEntity.setRepoName(antiEntity.getRepoName());
                        resultEntity.setBranch(antiEntity.getBranch());
                        resultEntity.setStatus("0");
                        resultEntity.setScanId(uuid);
                        poisonResultOperation.insertResultDetails(resultEntity);
                    }
                    // 是否执行扫描
                    antiEntity.setIsScan(true);
                    // 扫描是否成功
                    antiEntity.setIsSuccess(true);
                    // 扫描结果
                    antiEntity.setScanResult(results);
                    //结果计数
                    antiEntity.setResultCount(results.size());
                    antiOperation.updateScanResult(antiEntity);
                    poisonTaskOperation.updateTask(antiEntity, taskEntity);
                    // 获取执行时间
                    return MultiResponse.success(200, "success", results);
                } else //这里可以重试下载 后期优化
                {
                    // 是否执行扫描
                    antiEntity.setIsScan(false);
                    // 扫描是否陈工
                    antiEntity.setIsSuccess(false);
                    // 原因
                    antiEntity.setTips("repo not Downloaded.");
                    antiOperation.updateScanResult(antiEntity);
                    return MultiResponse.error(400, "repoNotDownloaded error");
                }
            } catch (IOException e) {
                antiEntity.setIsSuccess(false);
                antiEntity.setTips(e.getCause().toString());
                antiOperation.updateScanResult(antiEntity);
                e.printStackTrace();
                return MultiResponse.error(400, "scan error : " + e.getCause());
            }
        } else {
            return MultiResponse.error(400, "scan error , task not exist!");
        }
    }

    //设置环境变量，暂时黄区演示需要
    @Override
    public MultiResponse setEnv() {
        System.out.println("开始 start");
        try {
            String command0 = "cd";
            String command1 = "export JOERN_HOME=/opt/sscs/joern-cli/";
            String command2 = "export JAVA_HOME=/usr/lib/jvm/java-1.11.0-openjdk-amd64 ";
            Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command0, SCANTOOLFILE});
            Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command1});
            Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command2});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return MultiResponse.success(200, "success");
    }

    @Override
    public MultiResponse downloadRepo(AntiEntity antiEntity) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String createTime = df.format(System.currentTimeMillis());
        antiEntity.setCreateTime(createTime);
        //ci-backend-service.git
        //"openeuler-os-build";
        // String branch = "master";
        if (StringUtils.isEmpty(antiEntity.getBranch())) {
            antiEntity.setBranch("master");
        }
        String revision = "7c2f9fa05ec24426a289d881814745d8f2482f4b";
        String workspace = YamlUtil.getToolPath() + REPOPATH + "/" + antiEntity.getRepoName();
        antiEntity.setBranch(antiEntity.getBranch());
        antiEntity.setLanguage(antiEntity.getLanguage());
        antiEntity.setRepoUrl(antiEntity.getRepoUrl());
        antiOperation.insertScanResult(antiEntity);
        long startTime = System.currentTimeMillis();
        JGitUtil gfxly = new JGitUtil(antiEntity.getRepoName(), gitUser, gitPassword, antiEntity.getBranch(), revision, workspace);
        long endTime = System.currentTimeMillis();
        String downloadConsuming = String.valueOf((endTime - startTime) / 1000);
        //生成任务id
        taskIdGenerate(antiEntity, downloadConsuming);
        int getPullCode = gfxly.pullVersion(antiEntity.getRepoUrl());
        if (getPullCode == 0) {
            System.out.println("检出代码成功===0");
            antiEntity.setIsDownloaded(true);
            antiOperation.updateScanResult(antiEntity);
            return MultiResponse.success(200, "success");
        } else if (getPullCode == 1) {
            antiEntity.setTips("检出代码未知异常===1");
            antiEntity.setIsDownloaded(false);
            antiOperation.updateScanResult(antiEntity);
            return MultiResponse.error(400, "downloadRepo error");
        } else if (getPullCode == 2) {
            antiEntity.setTips("检出代码未知异常===2");
            antiEntity.setIsDownloaded(false);
            antiOperation.updateScanResult(antiEntity);
            return MultiResponse.error(400, "downloadRepo error");
        } else if (getPullCode == 3) {
            antiEntity.setTips("检出代码未知异常===3");
            antiEntity.setIsDownloaded(false);
            antiOperation.updateScanResult(antiEntity);
            return MultiResponse.error(400, "downloadRepo error");
        } else if (getPullCode == 4) {
            antiEntity.setTips("检出代码未知异常===4");
            antiEntity.setIsDownloaded(false);
            antiOperation.updateScanResult(antiEntity);
            return MultiResponse.error(400, "downloadRepo error");
        } else {
            antiEntity.setTips("检出代码未知异常===5");
            antiEntity.setIsDownloaded(false);
            antiOperation.updateScanResult(antiEntity);
            return MultiResponse.error(400, "downloadRepo error");
        }
    }

    public void taskIdGenerate(AntiEntity antiEntity, String downloadConsuming) {
        List<TaskEntity> taskEntity = poisonTaskOperation.queryTaskId(antiEntity);
        if (null != taskEntity && taskEntity.size() != 0) {
            taskEntity.get(0).setDownloadConsuming(downloadConsuming);
            poisonTaskOperation.updateTask(antiEntity, taskEntity.get(0));
            return;
        }else {
            TaskEntity newTaskEntity = new TaskEntity();
            String taskId = antiEntity.getProjectName() + "-" + antiEntity.getRepoUrl() + "-" + antiEntity.getRepoName();
            newTaskEntity.setTaskId(taskId);
            newTaskEntity.setDownloadConsuming(downloadConsuming);
            poisonTaskOperation.insertTaskResult(antiEntity, newTaskEntity);
        }

    }


}
