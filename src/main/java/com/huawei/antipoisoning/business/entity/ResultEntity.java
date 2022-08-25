package com.huawei.antipoisoning.business.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author zhangshengjie
 */
@Data
@ToString
public class ResultEntity {
    /**
     * 社区
     */
    @Field("community")
    private String community;

    /**
     * 仓名
     */
    @Field("repo_name")
    private String repoName;

    /**
     * 分支
     */
    @Field("branch")
    private String branch;

    /**
     * 问题状态（0：未屏蔽 1：屏蔽中 2：已屏蔽）
     */
    private String status;

    /**
     * id
     */
    @Field("scan_id")
    private String scanId;

    /**
     * 扫描文件名
     */
    @Field("suspicious_file_name")
    private String suspiciousFileName;

    /**
     * 规则名称
     */
    @Field("rule_name")
    private String ruleName;

    /**
     * 检查代码行+原文
     */
    @Field("check_result")
    private String checkResult;

    /**
     * 问题关键字
     */
    @Field("key_log_info")
    private String keyLogInfo;

    /**
     * 问题内容
     */
    @Field("suggestion")
    private String suggestion;
}
