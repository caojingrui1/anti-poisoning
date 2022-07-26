package com.huawei.antipoisoning.business.operation;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * vms接口返回数据存档操作表
 *
 * @since: 2022/5/31 17:01
 */
@Component
public class VmsDataOperation {
    /**
     * 外部源接口返回数据
     */
    private static final String DB_TABLE_NAME_SOURCE_OUT = "vms_source_out_details";

    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 新增外部源接口返回数据
     *
     * @param vmsExternalCveSourceDTOS 外部源接口返回数据
     */
    public void insertSourceOut(List<Object> vmsExternalCveSourceDTOS){
        if (CollectionUtils.isEmpty(vmsExternalCveSourceDTOS)){
            return;
        }
        mongoTemplate.insert(vmsExternalCveSourceDTOS,DB_TABLE_NAME_SOURCE_OUT);
    }
}
