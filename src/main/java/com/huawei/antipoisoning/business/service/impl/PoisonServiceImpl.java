package com.huawei.antipoisoning.business.service.impl;

import com.huawei.releasepoison.entity.RepoInfo;
import com.huawei.releasepoison.service.PoisonService;
import com.huawei.releasepoison.utils.MultiResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PoisonServiceImpl implements PoisonService {
    /**
     * 请求下载目标仓地址
     * */
    private static final String urlDownload = "http://10.244.34.89:8086/downloadRepo";
    /**
     * 请求下载目标仓地址
     * */
    private static final String urlAntiPoisoning = "http://10.244.34.89:8086/antiPoisoning/scanRepo";

    @Override
    public MultiResponse poisonScan(RepoInfo repoInfo) {
        //1.生成scanId
        String scanId = ScanidGenerate();
        //请求下载目标仓地址参数
        Map<String, String> paramDownload = new HashMap<>();
        paramDownload.put("scanId", scanId);
        paramDownload.put("repoUrl", repoInfo.getRepoUrl());
        paramDownload.put("language", repoInfo.getLanguage());
        //请求防投毒扫描地址参数
        Map<String, String> paramAntiPoisoning = new HashMap<>();
        paramAntiPoisoning.put("scanId", scanId);
        //2.请求下载目标仓
        requestUrl(urlDownload, paramDownload);
        //3.请求放投毒扫描
        requestUrl(urlAntiPoisoning, paramAntiPoisoning);
        //4.查询扫描结果
        return new MultiResponse().code(200).result("6");
    }

    public String ScanidGenerate(){

        return "";
    }

    public void requestUrl(String url, Map<String, String> param){
        try {
            //请求发起客户端
            CloseableHttpClient httpclient = HttpClients.createDefault();
            List postParams = new ArrayList();
            if (param != null){
                for(Map.Entry<String, String> entry:param.entrySet()){
                    postParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
            }
            //通过post方式访问
            HttpPost post = new HttpPost(url);
            HttpEntity paramEntity = new UrlEncodedFormEntity(postParams,"UTF-8");
            post.setEntity(paramEntity);
            httpclient.execute(post);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
