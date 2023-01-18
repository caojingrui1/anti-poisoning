/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.common.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Map;

/**
 * 码云接口调用工具类
 *
 * @since 2021-09-15
 * @author zyx
 */
public class GiteeApiUtil implements Serializable {
    // 访问url
    private final String URL = "https://gitee.com/api/v5";

    private Map<String, String> params;
    private String getPr = "/repos";

    public GiteeApiUtil(Map<String, String> params) {
        this.params = params;
    }

    /**
     * 获取PR信息。
     *
     * @return JSONObject
     */
    public JSONObject getPullRequestInfo() {
        String path = getPr + "/" + params.get("owner") + "/"
                + params.get("repo") + "/pulls/" + params.get("pullNumber");
        if (!StringUtils.isEmpty(params.get("accessToken"))) {
            path = path + "?access_token=" + params.get("accessToken");
        }
        HttpUtil httpUtil = new HttpUtil(URL);
        String result = httpUtil.doGet("", path);
        return JSONObject.parseObject(result);
    }

    /**
     * Pull Request Commit文件列表。最多显示300条diff,实际最多显示100条.
     *
     * @return JSONObject
     */
   public JSONArray getPrDiffFiles() {
       String path = getPr + "/" + params.get("owner") + "/"
               + params.get("repo") + "/pulls/" + params.get("pullNumber") + "/files";
       if (!StringUtils.isEmpty(params.get("accessToken"))) {
           path = path + "?access_token=" + params.get("accessToken");
       }
       HttpUtil httpUtil = new HttpUtil(URL);
       String result = httpUtil.doGet("", path);
       return JSONArray.parseArray(result);
   }
}
