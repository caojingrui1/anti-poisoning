package com.huawei.antipoisoning.common.config;//package com.huawei.vms.data.config;
//
//import com.huawei.vms.data.util.SecurityUtil;
//import com.mongodb.ConnectionString;
//import com.mongodb.MongoClientSettings;
//import com.mongodb.MongoCredential;
//import com.mongodb.ReadPreference;
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.internal.MongoClientImpl;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
//
///**
// * mongoDB配置类
// *
// * @since: 2022/5/31 11:40
// */
//@Configuration
//public class MongoConfig {
//    private final String userName ="rwuser";
//
//    private final String address ="192.168.0.180:8635";
//
//    private final String dbName ="vms_dev";
//
//    private final String password ="mBiFjLvB4OSQLz3cAQ1sVtfkUaUAqmmZ";
//
//    /**
//     * mongoDB客户端
//     *
//     * @return MongoClient mongodb连接对象
//     */
//    @Bean("mongoClient")
//    @Primary
//    public MongoClient mongoClient() {
//        // 使用数据库名、用户名密码登录
//        // 使用加密
//        MongoCredential credential =
//                MongoCredential.createCredential(userName, dbName,
//                        SecurityUtil.decrypt(password).toCharArray());
//        return new MongoClientImpl(getMongoClientOptions(credential), null);
//    }
//
//    private MongoClientSettings getMongoClientOptions(MongoCredential credential) {
//        MongoClientSettings.Builder builder = MongoClientSettings.builder();
//
//        builder.applyConnectionString(new ConnectionString("mongodb://" + address));
//        builder.credential(credential);
//        return builder.readPreference(ReadPreference.primary()).build();
//    }
//
//    /**
//     * 注册mongodb操作类
//     *
//     * @param mongoClient mongo客户端
//     * @return MongoTemplate
//     */
//    @Bean("mongoTemplate")
//    @Primary
//    @ConditionalOnClass(MongoClient.class)
//    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
//        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient, dbName));
//    }
//}
