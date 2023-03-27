/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.common.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Gitlab接口调用工具类
 *
 * @since 2023-03-20
 * @author zyx
 */
public class GitlabApiUtil implements Serializable {
    // 访问url
    private final String URL = "https://source.openeuler.sh/api/v4";

    private Map<String, String> params;
    private String getPr = "/projects";

    public GitlabApiUtil(Map<String, String> params) {
        this.params = params;
    }

    /**
     * 获取PR信息。
     *
     * @return JSONObject
     */
    public JSONObject getGitlabPullRequestInfo() {
        String path = getPr + "/" + params.get("projectId") + "/merge_requests/" + params.get("pullNumber");
        String token = "";
        if (!StringUtils.isEmpty(params.get("accessToken"))) {
            token = params.get("accessToken");
        }
        HttpUtil httpUtil = new HttpUtil(URL);
        String result = httpUtil.doGitlabGet(token, path);
        return JSONObject.parseObject(result);
    }

    /**
     * Pull Request Commit文件列表。
     * 显示合并请求的相关信息，包括改动的文件和内容。
     * 引入于 13.6 版本，无论是 API 还是网页端界面， 文件改动差异数据都有相同的大小限制。
     * 当这一限制影响响应结果时，overflow 的值将为 true。
     * 可以通过添加 access_raw_diffs 参数来获取不加限制的差异数据，此时将不再从数据库而是直接从 Gitaly 访问差异。
     * 这种方法通常速度较慢且资源密集，但不受数据库的限制。
     *
     * @return JSONObject
     */
   public JSONArray getGitlabPrDiffFiles() {
       String path = getPr + "/" + params.get("projectId") + "/merge_requests/" + params.get("pullNumber") + "/changes";
       String token = "";
       if (!StringUtils.isEmpty(params.get("accessToken"))) {
           token = params.get("accessToken");
       }
       HttpUtil httpUtil = new HttpUtil(URL);
       String result = httpUtil.doGitlabGet(token, path);
       String changes = JSONObject.parseObject(result).get("changes").toString();
       return JSONArray.parseArray(changes);
   }

    public static void main(String[] args) {
//       Map<String, String> params = new HashMap<>();
//       params.put("projectId", "30975");
//       params.put("pullNumber", "76");
//       GitlabApiUtil gitlabApiUtil = new GitlabApiUtil(params);
////       gitlabApiUtil.getGitlabPullRequestInfo();
//       gitlabApiUtil.getGitlabPrDiffFiles();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", 30975);
        jsonObject.put("name", "anti-poisoning");
        jsonObject.put("description", "防投毒");
        jsonObject.put("web_url", "http://source.openeuler.sh/openMajun/anti-poisoning");
        jsonObject.put("git_ssh_url", "git@source.openeuler.sh:openMajun/anti-poisoning.git");
        jsonObject.put("git_http_url", "http://source.openeuler.sh/openMajun/anti-poisoning.git");
        jsonObject.put("namespace", "openMaJun");
        jsonObject.put("http_url", "http://source.openeuler.sh/openMajun/anti-poisoning.git");
        String sss = jsonObject.toJSONString();

       String filePath = "src/main/java/com/majun/gateway/GatewayMain.java";
        try {
            String ss = URLEncoder.encode(filePath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
