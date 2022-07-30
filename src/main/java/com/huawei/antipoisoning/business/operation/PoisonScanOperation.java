package com.huawei.antipoisoning.business.operation;

import com.huawei.antipoisoning.business.entity.AntiEntity;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 扫描操作。
 *
 * @author zyx
 * @since 2022-07-30
 */
@Component
public class PoisonScanOperation {
    /**
     * 外部源接口返回数据
     */
    private static final String SCAN_RESULTS = "scan_result";

    @Resource
    private MongoTemplate mongoTemplate;


    public List<AntiEntity> queryResults() {
        return mongoTemplate.findAll(AntiEntity.class, SCAN_RESULTS);
    }
}
