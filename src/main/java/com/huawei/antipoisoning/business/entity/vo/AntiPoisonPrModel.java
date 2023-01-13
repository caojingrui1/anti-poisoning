package com.huawei.antipoisoning.business.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 防投毒任务结果数量映射
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AntiPoisonPrModel {
    // 社区
    private String projectName;

    // 仓库
    private String repoName;

    // pr链接
    private String prUrl;

    // 执行结果，pass failed
    private String result;
}
