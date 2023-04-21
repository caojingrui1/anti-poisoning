/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.service;

import com.huawei.antipoisoning.business.entity.checkrule.RuleModel;
import com.huawei.antipoisoning.business.entity.checkrule.RuleSetModel;
import com.huawei.antipoisoning.business.entity.checkrule.TaskRuleSetVo;
import com.huawei.antipoisoning.common.entity.MultiResponse;

/**
 * 规则方法接口
 */
public interface CheckRuleService {

    /**
     * 根据条件获取规则
     *
     * @param ruleModel 查询参数
     * @return getAllRules
     */
    MultiResponse getAllRules(RuleModel ruleModel);

    MultiResponse getExportRules(RuleModel ruleModel);

    /**
     * 自定义规则集
     *
     * @param ruleSetModel 自定义实体类
     * @return createRuleSet
     */
    MultiResponse createRuleSet(RuleSetModel ruleSetModel);

    /**
     * 获取规则界面左侧的导航栏
     *
     * @return getAllRulesConfig
     */
    MultiResponse getAllRulesConfig();

    /**
     * 获取社区的所有规则集信息（主界面）
     *
     * @param ruleSetModel 查询参数
     * @return queryRuleSet
     */
    MultiResponse queryRuleSet(RuleSetModel ruleSetModel);

    /**
     * 获取规则集详情（配置界面）
     *
     * @param ruleSetModel 查询参数
     * @return queryRuleSet
     */
    MultiResponse queryRuleSetConfig(RuleSetModel ruleSetModel);

    /**
     * 删除自定义规则集
     *
     * @param ruleSetModel 删除参数
     */
    MultiResponse delRuleSet(RuleSetModel ruleSetModel);

    /**
     * 查找任务规则
     *
     * @param ruleSetModel 查找id
     */
    MultiResponse queryTaskById(RuleSetModel ruleSetModel);


    /**
     * 更改任务规则
     *
     * @param taskRuleSetVo 修改后的任务规则信息
     */
    MultiResponse updateTaskRule(TaskRuleSetVo taskRuleSetVo);

    /**
     * 获取仓库的所选规则集
     *
     * @param taskRuleSetVo 查询实体类
     * @return getTaskRule
     */
    MultiResponse getTaskRule(TaskRuleSetVo taskRuleSetVo);

    /**
     * 新增规则。
     *
     * @param ruleModel 规则
     * @return MultiResponse
     */
    MultiResponse createRule(RuleModel ruleModel);

    /**
     * 修改规则。
     *
     * @param ruleModel 规则
     * @return MultiResponse
     */
    MultiResponse updateRule(RuleModel ruleModel);
}
