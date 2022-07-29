package com.huawei.antipoisoning.business.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.ResultEntity;
import com.huawei.antipoisoning.business.operation.AntiOperation;
import com.huawei.antipoisoning.business.service.AntiService;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import com.huawei.antipoisoning.common.util.AntiMainUtil;
import com.huawei.antipoisoning.common.util.JGitUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * vms接口服务实现类
 *
 * @since: 2022/5/30 16:22
 */
@Service("vmsService")
public class AntiServiceImpl implements AntiService {

    private static final String SCANRESULTPATH = "/usr/result";

    private static final String SCANTOOLPATH = "/opt/sscs/SoftwareSupplyChainSecurity-release-openeuler/openeuler_scan.py";

    private static final String REPOPATH = "/usr/test/download";

    @Value("${git.username}")
    private String gitUser;

    @Value("${git.password}")
    private String gitPassword;

    @Autowired
    private AntiOperation antiOperation;
    /**
     * 执行漏洞
     *
     * @return MultiResponse
     */
    @Override
    public MultiResponse scanRepo(String uuid) {
        AntiEntity antiEntity = antiOperation.queryAntiEntity(uuid);
        //扫描指定仓库 下载后放入文件夹 扫描 产生报告
        if (null != antiEntity) {
            try {
                if (antiEntity.getIsDownloaded() == true) {
                    String[] arguments = new String[]{"/bin/sh", "-c", "time /usr/local/bin/python3"
                            + " " + SCANTOOLPATH // 工具地址
                            + " " + REPOPATH + "/" + antiEntity.getRepoName() + // 仓库下载后存放地址
                            " " + SCANRESULTPATH + "/" + antiEntity.getRepoName() + ".json " +  // 扫描完成后结果存放地址   /usr/result/openeuler-os-build
                            "--enable-" + antiEntity.getLanguage()}; // 语言
                    String sb = AntiMainUtil.execute(arguments);
                    System.out.println("sb ==== :" + sb);
                    String result = AntiMainUtil.getJsonContent(SCANRESULTPATH, antiEntity.getRepoName());
                    System.out.println(result);
                    List<ResultEntity> results = JSONArray.parseArray(result, ResultEntity.class);
                    antiEntity.setIsScan(true); // 是否执行扫描
                    antiEntity.setStatus(true); // 扫描是否成功
                    antiEntity.setScanResult(results);  // 扫描结果
                    antiEntity.setResultCount(results.size()); //结果计数
                    antiOperation.updateScanResult(antiEntity);
                    // 获取执行时间
                    return MultiResponse.success(200, "success", results);
                } else //这里可以重试下载 后期优化
                {
                    antiEntity.setIsScan(false); // 是否执行扫描
                    antiEntity.setStatus(false); // 扫描是否陈工
                    antiEntity.setTips("repo not Downloaded."); // 原因
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
            String command1 = "export JOERN_HOME=/opt/sscs/joern-cli/" ;
            String command2 = "export JAVA_HOME=/usr/lib/jvm/java-1.11.0-openjdk-amd64 ";
            Runtime.getRuntime().exec(command1);
            Runtime.getRuntime().exec(command2);
            Runtime.getRuntime().exec(new String[] {"/bin/sh","-c",command1});
            Runtime.getRuntime().exec(new String[] {"/bin/sh","-c",command2});
            Runtime.getRuntime().exec(new String[] {command1});
            Runtime.getRuntime().exec(new String[] {command2});
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return MultiResponse.success(200, "success");
    }

    @Override
    public MultiResponse downloadRepo(AntiEntity antiEntity) {
        String gitUrl = antiEntity.getRepoUrl().split("/")[2]; //ci-backend-service.git
        String module = gitUrl.substring(0,gitUrl.length()-4);//"openeuler-os-build";
        antiEntity.setRepoName(module);
        if (null == antiEntity.getBranch() || "".equals(antiEntity.getBranch())) // String branch = "master";
        {
            antiEntity.setBranch("master");
        }
        String revision = "7c2f9fa05ec24426a289d881814745d8f2482f4b";
        String workspace = REPOPATH + File.separator + module;
        antiEntity.setBranch(antiEntity.getBranch());
        antiEntity.setLanguage(antiEntity.getLanguage());
        antiEntity.setRepoName(module);
        antiEntity.setRepoUrl(antiEntity.getRepoUrl());
        antiOperation.insertScanResult(antiEntity);
        JGitUtil gfxly = new JGitUtil(module, gitUser, gitPassword, antiEntity.getBranch(), revision, workspace);
        int getPullCode = gfxly.pull(antiEntity.getRepoUrl());
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
}
