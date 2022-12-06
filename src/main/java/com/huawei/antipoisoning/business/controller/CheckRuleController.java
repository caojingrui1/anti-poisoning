/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.controller;

import com.huawei.antipoisoning.business.enmu.ConstantsArgs;
import com.huawei.antipoisoning.business.entity.checkrule.RuleModel;
import com.huawei.antipoisoning.business.entity.checkrule.RuleSetModel;
import com.huawei.antipoisoning.business.entity.checkrule.TaskRuleSetVo;
import com.huawei.antipoisoning.business.service.CheckRuleService;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 防投毒规则维护
 *
 * @since 2022-09-11
 * @author cqx
 */
@RestController
@RequestMapping(value = "/checkRule")
public class CheckRuleController {
    @Autowired
    private CheckRuleService checkRuleService;

    /**
     * 根据条件获取所有的规则
     *
     * @param ruleModel 查询参数
     * @return getAllRules
     */
    @PostMapping(value = "/query/rules")
    public MultiResponse getAllRules(@RequestBody RuleModel ruleModel) {
        return checkRuleService.getAllRules(ruleModel);
    }

    /**
     * 规则界面获取左侧导航栏
     *
     * @return getAllRulesConfig
     */
    @GetMapping(value = "/query/rules/config")
    public MultiResponse getAllRulesConfig() {
        return checkRuleService.getAllRulesConfig();
    }

    /**
     * 自定义创建规则集
     *
     * @param ruleSetModel 规则集实体类
     * @return createRuleSet
     */
    @PostMapping(value = "/add/rule/set")
    public MultiResponse createRuleSet(@RequestBody RuleSetModel ruleSetModel) {
        checkRuleService.createRuleSet(ruleSetModel);
        return new MultiResponse().code(200).message("success");
    }

    /**
     * 删除自定义创建规则集
     *
     * @param ruleSetModel 规则集实体类
     * @return createRuleSet
     */
    @PostMapping(value = "/del/rule/set")
    public MultiResponse delRuleSet(@RequestBody RuleSetModel ruleSetModel) {
        return checkRuleService.delRuleSet(ruleSetModel);
    }

    /**
     * 获取社区的所有的规则集（主界面）
     *
     * @param ruleSetModel 查询参数
     * @return queryRuleSet
     */
    @PostMapping(value = "/query/rule/set")
    public MultiResponse queryRuleSet(@RequestBody RuleSetModel ruleSetModel) {
        return checkRuleService.queryRuleSet(ruleSetModel);
    }

    /**
     * 获取规则集详情（配置界面）
     *
     * @param ruleSetModel 查询参数
     * @return queryRuleSetConfig
     */
    @PostMapping(value = "/query/rule/set/config")
    public MultiResponse queryRuleSetConfig(@RequestBody RuleSetModel ruleSetModel) {
        return checkRuleService.queryRuleSetConfig(ruleSetModel);
    }

    /**
     * 获取社区的所有的规则集（主界面）
     *
     * @param ruleSetModel 查询参数
     * @return queryRuleSet
     */
    @PostMapping(value = "/update/taskRule/set")
    public MultiResponse updateTaskRuleSet(@RequestBody RuleSetModel ruleSetModel) {
        return checkRuleService.queryRuleSet(ruleSetModel);
    }

    /**
     * 查询检测中心任务的规则列表
     *
     * @param ruleSetModel 查询参数
     * @return queryRuleSet
     */
    @PostMapping(value = "poison/queryRuleById")
    public MultiResponse queryRuleById(@RequestBody RuleSetModel ruleSetModel) {
        return checkRuleService.queryTaskById(ruleSetModel);
    }

    /**
     * 修改任务规则集
     *
     * @param taskRuleSetVo 任务规则集实体类
     * @return createRuleSet
     */
    @PostMapping(value = "/update/taskRule")
    public MultiResponse updateTaskRule(@RequestBody TaskRuleSetVo taskRuleSetVo) {
        checkRuleService.updateTaskRule(taskRuleSetVo);
        return new MultiResponse().code(ConstantsArgs.CODE_SUCCESS).message("success");
    }

    /**
     * 获取仓库的规则集
     *
     * @param taskRuleSetVo 任务规则集实体类
     * @return createRuleSet
     */
    @PostMapping(value = "/get/taskRule")
    public MultiResponse getTaskRule(@RequestBody TaskRuleSetVo taskRuleSetVo) {
        return checkRuleService.getTaskRule(taskRuleSetVo);
    }
}
