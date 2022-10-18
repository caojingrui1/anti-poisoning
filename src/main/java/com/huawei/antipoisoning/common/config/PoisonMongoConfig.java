package com.huawei.antipoisoning.common.config;


import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

/**
 * mongoDB配置类
 *
 * @author zyx
 * @since 2022-10-18
 */
@Configuration
public class PoisonMongoConfig {
    private final String mongouri =
            System.getProperty("spring.data.mongodb.uri");

    private final String dbName =
            System.getProperty("spring.data.mongodb.dbname");


    /**
     * mongodb客户端
     *
     * @return MongoClient
     */
    @Bean("poisonMongoClient")
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(mongouri);
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
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient(), "anti-poison"));
    }

}