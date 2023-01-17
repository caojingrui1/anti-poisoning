/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.common.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * mongoDB配置类
 *
 * @author zyx
 * @since 2022-10-18
 */
@Configuration
public class PoisonMongoConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(PoisonMongoConfig.class);
    private final String mongouri =
            System.getenv("spring.data.mongodb.uri");

    private final String dbName = 
            System.getenv("spring.data.mongodb.dbname");

    /**
     * mongodb客户端
     *
     * @return MongoClient
     */
    @Bean("poisonMongoClient")
    public MongoClient mongoClient() {
        String uri = mongouri;
        // 对特殊字符进行转义
        uri = uri.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
        uri = uri.replaceAll("\\+", "%2B");
        try {
            uri = URLDecoder.decode(uri, "utf-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage());
        }
        ConnectionString connectionString = new ConnectionString(uri);
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString)
                .retryWrites(true).build();
        return MongoClients.create(settings);
    }

    /**
     * 注册mongodb操作类
     *
     * @return MongoTemplate mongodb连接对象
     */
    @Bean("poisonMongoTemplate")
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient(), dbName));
    }
}