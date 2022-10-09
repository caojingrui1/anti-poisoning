package com.huawei.antipoisoning.business.enmu;

/**
 * 数据库中集合名
 *
 * @since 2022-08-24
 */
public class CollectionTableName {
    /**
     * 防投毒规则表
     */
    public static final String ANTI_CHECK_RULE = "anti_check_rule";

    /**
     * 防投毒规则集表
     */
    public static final String ANTI_CHECK_RULE_SET = "anti_check_rule_set";

    /**
     * 防投毒任务选择规则集表
     */
    public static final String ANTI_TASK_RULE_SET = "anti_task_rule_set";

    /**
     * 仓库分支配置
     */
    public static final String BRANCH_REPO = "branch_repository";

    /**
     * 版本扫描结果
     */
    public static final String SCAN_RESULTS = "scan_result";

    /**
     * 门禁扫描结果
     */
    public static final String SCAN_PR_RESULTS = "scan_pr_result";

    /**
     * 版本扫描结果详情表
     */
    public static final String SCAN_RESULT_DETAILS = "scan_result_details";

    /**
     * 门禁扫描结果详情表
     */
    public static final String SCAN_PR_RESULT_DETAILS = "scan_pr_result_details";

    /**
     * 版本扫描任务表
     */
    public static final String POISON_VERSION_TASK = "poison_version_task";

    /**
     * 门禁扫描任务表
     */
    public static final String POISON_PR_TASK = "poison_pr_task";

    /**
     * 屏蔽问题详情
     */
    public static final String SHIELD_RESULT_DETAIL = "shield_result_detail";
}
