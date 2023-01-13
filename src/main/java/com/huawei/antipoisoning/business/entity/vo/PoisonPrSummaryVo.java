package com.huawei.antipoisoning.business.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 防投毒任务结果数量映射
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PoisonPrSummaryVo {
    // 社区
    private String projectName;

    // 仓库
    private String repoName;

    // 执行开始时间
    private String executeEndTime;

    // 执行结束时间
    private String executeStartTime;

    // 任务执行时间
    private long excuteTime;

    // pr链接
    private String prUrl;

    // 执行结果，pass failed
    private String result;
}
