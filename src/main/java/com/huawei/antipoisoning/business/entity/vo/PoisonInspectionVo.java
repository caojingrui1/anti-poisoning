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
public class PoisonInspectionVo {

    // 社区
    private String projectName;

    // 仓库
    private String repoName;

    // 分支版本
    private String branch;

    // 已解决数
    private long solveCount;

    // 未解决数
    private long issueCount;

}
