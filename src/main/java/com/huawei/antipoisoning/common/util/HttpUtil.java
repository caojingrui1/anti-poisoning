/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.common.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * http请求工具类
 *
 * @author zyx
 * @since 2022-01-05
 */
public class HttpUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

    // 访问url
    private String url;

    public HttpUtil(String url) {
        this.url = url;
    }

    /**
     * post 请求
     *
     * @param params 参数对象
     * @param request 请求接口
     * @return body
     */
    public String doPost(JSONObject params, String request) {
        String body = "";
        try {
            // 创建post方式请求对象
            HttpPost httpPost = new HttpPost(url + request);
            // 装填参数
            StringEntity stringEntity = new StringEntity(params.toString(), "utf-8");
            stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));
            // 设置参数到请求对象中
            httpPost.setEntity(stringEntity);
            // 设置header信息
            // 指定报文头【Content-type】、【User-Agent】
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            // 创建httpclient对象
            CloseableHttpClient client = HttpClients.createDefault();
            // 执行请求操作，并拿到结果（同步阻塞）
            CloseableHttpResponse response = client.execute(httpPost);
            // 获取结果实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // 按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, "UTF-8");
            }
            EntityUtils.consume(entity);
            // 释放链接
            response.close();
            return body;
        } catch (IOException e) {
            LOGGER.error("errInfo is {}", e.getMessage());
            return body;
        }
    }

    /**
     * GET 请求
     *
     * @param code 授权码
     * @param request 请求接口url
     * @return body
     */
    public String doGet(String code, String request) {
        String body = "";
        try {
            // 创建post方式请求对象
            HttpGet httpGet = new HttpGet(url + request  + code);
            // 设置header信息
            // 指定报文头【Content-type】、【User-Agent】
            httpGet.setHeader("Content-type", "application/json");
            httpGet.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");

            // 创建httpclient对象
            CloseableHttpClient client = HttpClients.createDefault();
            // 执行请求操作，并拿到结果（同步阻塞）
            CloseableHttpResponse response = client.execute(httpGet);
            // 获取结果实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // 按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, "UTF-8");
            }
            EntityUtils.consume(entity);
            // 释放链接
            response.close();
            return body;
        } catch (IOException e) {
            LOGGER.error("errInfo is {}", e.getMessage());
            return body;
        }
    }

    /**
     * GET 请求
     *
     * @param token 授权码
     * @param request 请求接口url
     * @return body
     */
    public String doGitlabGet(String token, String request) {
        String body = "";
        try {
            // 创建post方式请求对象
            HttpGet httpGet = new HttpGet(url + request );
            // 设置header信息
            // 指定报文头【Content-type】、【User-Agent】
            httpGet.setHeader("Content-type", "application/json");
            httpGet.setHeader("PRIVATE-TOKEN", token);
            httpGet.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");

            // 创建httpclient对象
            CloseableHttpClient client = HttpClients.createDefault();
            // 执行请求操作，并拿到结果（同步阻塞）
            CloseableHttpResponse response = client.execute(httpGet);
            // 获取结果实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // 按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, "UTF-8");
            }
            EntityUtils.consume(entity);
            // 释放链接
            response.close();
            return body;
        } catch (IOException e) {
            LOGGER.error("errInfo is {}", e.getMessage());
            return body;
        }
    }

    /**
     * 对http返回结果进行校验，如果状态码不是200，终止并抛出异常
     *
     * @param body 响应信息
     * @throws HttpResponseException 响应异常
     */
    public void checkBody(String body) throws HttpResponseException {
        JSONObject responseBody = JSONObject.parseObject(body);
        int code = responseBody.getInteger("code");
        if (code != 200) {
            throw new HttpResponseException(code, "请求错误！");
        }
    }
}
