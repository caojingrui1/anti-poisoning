package com.huawei.antipoisoning.business.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.ResultEntity;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.operation.AntiOperation;
import com.huawei.antipoisoning.business.operation.PoisonResultOperation;
import com.huawei.antipoisoning.business.operation.PoisonTaskOperation;
import com.huawei.antipoisoning.business.service.AntiService;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import com.huawei.antipoisoning.common.util.AntiMainUtil;
import com.huawei.antipoisoning.common.util.JGitUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * vms接口服务实现类
 *
 * @since: 2022/5/30 16:22
 */
@Service("vmsService")
public class AntiServiceImpl implements AntiService {

    private static final String SCANRESULTPATH = "/root/softwareFile/report/";

    private static final String SCANTOOLPATH = "/root/opt/SoftwareSupplyChainSecurity-v1/openeuler_scan.py";

    private static final String SCANTOOLFILE = "/root/opt/SoftwareSupplyChainSecurity-v1/";

    private static final String REPOPATH = "/root/softwareFile/download/";

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
        taskEntity.setLastExecuteStartTime(taskEntity.getExecuteStartTime());
        taskEntity.setLastExecuteEndTime(taskEntity.getExecuteEndTime());
        //扫描指定仓库 下载后放入文件夹 扫描 产生报告
        if (null != antiEntity) {
            try {
                if (antiEntity.getIsDownloaded() == true) {
                    // 设置环境变量
//                    setEnv();
                    String[] arguments = new String[]{"/bin/sh", "-c", "time python"
                            + " " + SCANTOOLPATH // 工具地址
                            + " " + REPOPATH + "/" + antiEntity.getRepoName() + // 仓库下载后存放地址
                            " " + SCANRESULTPATH + "/" + antiEntity.getRepoName() + ".json " +  // 扫描完成后结果存放地址   /usr/result/openeuler-os-build
                            "--enable-" + antiEntity.getLanguage() + "> poison_logs/" + uuid + ".txt"}; // 语言
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                    long startTime = System.currentTimeMillis();
                    String taskStartTime = df.format(startTime);
                    taskEntity.setExecuteStartTime(taskStartTime);
                    String sb = AntiMainUtil.execute(arguments);
                    long endTime = System.currentTimeMillis();
                    String taskEndTime = df.format(endTime);
                    taskEntity.setExecuteStartTime(taskEndTime);
                    String timeConsuming = df.format(startTime - endTime);
                    antiEntity.setTimeConsuming(timeConsuming);
                    System.out.println("sb ==== :" + sb);
                    String result = AntiMainUtil.getJsonContent(SCANRESULTPATH, antiEntity.getRepoName());
                    System.out.println(result);
                    List<ResultEntity> results = JSONArray.parseArray(result, ResultEntity.class);
                    for (ResultEntity resultEntity : results){
                        resultEntity.setCommunity(antiEntity.getCommunity());
                        resultEntity.setRepoName(antiEntity.getRepoName());
                        resultEntity.setBranch(antiEntity.getBranch());
                        resultEntity.setStatus("0");
                        resultEntity.setScanId(uuid);
                        poisonResultOperation.insertResultDetails(resultEntity);
                    }
                    // 是否执行扫描
                    antiEntity.setIsScan(true);
                    // 扫描是否成功
                    antiEntity.setStatus(true);
                    // 扫描结果
                    antiEntity.setScanResult(results);
                    //结果计数
                    antiEntity.setResultCount(results.size());
                    antiOperation.updateScanResult(antiEntity);
                    poisonTaskOperation.updateTask(taskEntity);
                    // 获取执行时间
                    return MultiResponse.success(200, "success", results);
                } else //这里可以重试下载 后期优化
                {
                    // 是否执行扫描
                    antiEntity.setIsScan(false);
                    // 扫描是否陈工
                    antiEntity.setStatus(false);
                    // 原因
                    antiEntity.setTips("repo not Downloaded.");
                    antiOperation.updateScanResult(antiEntity);
                    return MultiResponse.error(400, "repoNotDownloaded error");
                }
            } catch (IOException e) {
                antiEntity.setStatus(false);
                 antiEntity.setTips(e.getCause().toString());
                antiOperation.updateScanResult(antiEntity);
                e.printStackTrace();
                return MultiResponse.error(400, "scan error : "+ e.getCause());
            }
        }
        else {
            return MultiResponse.error(400, "scan error , task not exist!");
        }
    }

    //设置环境变量，暂时黄区演示需要
    @Override
    public MultiResponse setEnv(){
        System.out.println("开始 start");
        try {
            String command0 = "cd";
            String command1 = "export JOERN_HOME=/opt/sscs/joern-cli/" ;
            String command2 = "export JAVA_HOME=/usr/lib/jvm/java-1.11.0-openjdk-amd64 ";
            Runtime.getRuntime().exec(new String[] {"/bin/sh","-c", command0, SCANTOOLFILE});
            Runtime.getRuntime().exec(new String[] {"/bin/sh","-c", command1});
            Runtime.getRuntime().exec(new String[] {"/bin/sh","-c", command2});
        } catch (IOException e)
        {
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
        String gitUrl = antiEntity.getRepoUrl().split("/")[4];
        //"openeuler-os-build";
        String module = gitUrl.substring(0,gitUrl.length()-4);
        antiEntity.setRepoName(module);
        // String branch = "master";
        if (StringUtils.isEmpty(antiEntity.getBranch()))
        {
            antiEntity.setBranch("master");
        }
        String revision = "7c2f9fa05ec24426a289d881814745d8f2482f4b";
        String workspace = REPOPATH + "/" + module;
        antiEntity.setBranch(antiEntity.getBranch());
        antiEntity.setLanguage(antiEntity.getLanguage());
        antiEntity.setRepoName(module);
        antiEntity.setRepoUrl(antiEntity.getRepoUrl());
        antiOperation.insertScanResult(antiEntity);
        //生成任务id
        taskIdGenerate(antiEntity);
        JGitUtil gfxly = new JGitUtil(module, gitUser, gitPassword, antiEntity.getBranch(), revision, workspace);
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
            return MultiResponse.error(400,"downloadRepo error");
        } else if (getPullCode == 2) {
            antiEntity.setTips("检出代码未知异常===2");
            antiEntity.setIsDownloaded(false);
            antiOperation.updateScanResult(antiEntity);
            return MultiResponse.error(400,"downloadRepo error");
        } else if (getPullCode == 3) {
            antiEntity.setTips("检出代码未知异常===3");
            antiEntity.setIsDownloaded(false);
            antiOperation.updateScanResult(antiEntity);
            return MultiResponse.error(400,"downloadRepo error");
        } else if (getPullCode == 4) {
            antiEntity.setTips("检出代码未知异常===4");
            antiEntity.setIsDownloaded(false);
            antiOperation.updateScanResult(antiEntity);
            return MultiResponse.error(400,"downloadRepo error");
        } else {
            antiEntity.setTips("检出代码未知异常===5");
            antiEntity.setIsDownloaded(false);
            antiOperation.updateScanResult(antiEntity);
            return MultiResponse.error(400,"downloadRepo error");
        }
    }

    public void taskIdGenerate(AntiEntity antiEntity){
        List<TaskEntity> taskEntity = poisonTaskOperation.queryTaskId(antiEntity);
        if(null != taskEntity && taskEntity.size() != 0){
            poisonTaskOperation.updateTaskResult(antiEntity, taskEntity.get(0).getTaskId());
            return;
        }
        String taskId = antiEntity.getCommunity() + "-" + antiEntity.getRepoUrl() + "-" + antiEntity.getRepoName();
        poisonTaskOperation.insertTaskResult(antiEntity, taskId);
    }
}
