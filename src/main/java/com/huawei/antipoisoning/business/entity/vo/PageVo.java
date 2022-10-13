/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据分页实体类
 *
 * @author zhangshengjie
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageVo {
    private Long total;

    private List list;
}
