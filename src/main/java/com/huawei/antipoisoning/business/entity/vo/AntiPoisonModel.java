/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.entity.vo;

import com.huawei.antipoisoning.business.entity.RepoInfo;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据分页实体类
 *
 * @since 2022-09-03
 * @author zhangshengjie
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AntiPoisonModel {
    private TaskEntity taskEntity;

    private List<RepoInfo> repoInfos;
}
