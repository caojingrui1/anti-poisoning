/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.operation;

import com.huawei.antipoisoning.business.enmu.CollectionTableName;
import com.huawei.antipoisoning.business.entity.AntiEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * 扫描结果包裹数据存档
 *
 * @since: 2022/5/31 17:01
 */
@Component
public class AntiOperation {
    @Autowired
    @Qualifier("poisonMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * 保存扫描结果
     *
     * @param antiScan 扫描数据
     */
    public void insertScanResult(AntiEntity antiScan) {
        if (ObjectUtils.isEmpty(antiScan)) {
            return;
        }
        mongoTemplate.insert(antiScan, CollectionTableName.SCAN_RESULTS);
    }

    /**
     * 保存扫描结果
     *
     * @param query 查询参数
     * @return AntiEntity
     */
    public AntiEntity queryScanResult(Query query) { // 查询入库结果
        return mongoTemplate.findOne(query, AntiEntity.class, CollectionTableName.SCAN_RESULTS);
    }

    /**
     * ID更新扫描结果
     *
     * @param antiEntity 参数
     * @@return 结果
     */
    public long updateScanResult(AntiEntity antiEntity) {
        Update update = new Update();
        if (antiEntity.getIsScan() != null) {
            update.set("isScan", antiEntity.getIsScan());
        }
        if (antiEntity.getIsDownloaded() != null) {
            update.set("is_downloaded", antiEntity.getIsDownloaded());
        }
        if (antiEntity.getResultCount() != null) {
            update.set("result_count", antiEntity.getResultCount());
        }
        if (antiEntity.getIsSuccess() != null) {
            update.set("is_downloaded", antiEntity.getIsSuccess());
        }
        if (antiEntity.getTips() != null) {
            update.set("tips", antiEntity.getTips());
        }
        if (antiEntity.getTimeConsuming() != null) {
            update.set("time_consuming", antiEntity.getTimeConsuming());
        }
        Query query = Query.query(Criteria.where("scan_id").is(antiEntity.getScanId()));
        return mongoTemplate.updateFirst(query, update, CollectionTableName.SCAN_RESULTS).getModifiedCount();
    }

    /**
     * 更新下载结果
     *
     * @param uuid 参数
     * @param isDownloaded 是否下载成功
     * @return long
     */
    public long updateDownloadResult(String uuid, boolean isDownloaded) {
        Query query = Query.query(Criteria.where("scan_id").is(uuid));
        Update update = new Update();
        update.set("is_downloaded", isDownloaded);
        return mongoTemplate.updateFirst(query, update, CollectionTableName.SCAN_RESULTS).getModifiedCount();
    }

    /**
     * 查询一条结果
     *
     * @param uuid 扫描ID
     * @return AntiEntity
     */
    public AntiEntity queryAntiEntity(String uuid) {
        Query query = Query.query(new Criteria("scan_id").is(uuid));
        return mongoTemplate.findOne(query, AntiEntity.class, CollectionTableName.SCAN_RESULTS);
    }
}
