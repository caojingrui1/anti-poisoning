package com.huawei.antipoisoning.business.service.impl;


import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.RepoInfo;
import com.huawei.antipoisoning.business.entity.vo.PageVo;
import com.huawei.antipoisoning.business.operation.PoisonScanOperation;
import com.huawei.antipoisoning.business.service.AntiService;
import com.huawei.antipoisoning.business.service.PoisonService;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PoisonServiceImpl implements PoisonService {

    @Autowired
    private AntiService antiService;

    @Autowired
    private PoisonScanOperation poisonScanOperation;

    @Override
    public MultiResponse poisonScan(RepoInfo repoInfo) {
        //1.生成scanId
        String scanId = ScanidGenerate(repoInfo.getCommunity(), repoInfo.getRepoName(), repoInfo.getBranch());
        //请求下载目标仓地址参数
        AntiEntity antiEntity = new AntiEntity();
        antiEntity.setScanId(scanId);
        antiEntity.setBranch(repoInfo.getBranch());
        antiEntity.setLanguage(repoInfo.getLanguage());
        antiEntity.setRepoUrl(repoInfo.getRepoUrl());
        antiEntity.setRepoName(repoInfo.getRepoName());
        antiEntity.setIsScan(true);
        antiEntity.setCommunity(repoInfo.getCommunity());
        // 下载目标仓库代码
        antiService.downloadRepo(antiEntity);
        // 防投毒扫描
        antiService.scanRepo(scanId);
        return new MultiResponse().code(200).result("poisonScan start");
    }

    @Override
    public MultiResponse queryResults(RepoInfo repoInfo) {
        PageVo summaryVos = poisonScanOperation.queryResults(repoInfo);
        return new MultiResponse().code(200).result(summaryVos);
    }


    /**
     * 随机码生成。
     *
     * @param community 社区名称
     * @param repoName 仓库名称
     * @param branch 分支名称
     * @return String 随机码
     */
    public String ScanidGenerate(String community, String repoName, String branch){
        long time = System.currentTimeMillis();
        String scanId = community + "-" + repoName + "-" + branch + "-" + time;
        return scanId;
    }
}
