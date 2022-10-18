/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.service.impl;

import com.huawei.antipoisoning.business.entity.checkrule.*;
import com.huawei.antipoisoning.business.operation.CheckRuleOperation;
import com.huawei.antipoisoning.business.service.CheckRuleService;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 规则配置接口实现类
 */
@Service
public class CheckRuleServiceImpl implements CheckRuleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckRuleServiceImpl.class);

    @Autowired
    private CheckRuleOperation checkRuleOperation;

    /**
     * 根据条件获取规则
     *
     * @param ruleModel 查询参数
     * @return getAllRules
     */
    @Override
    public MultiResponse getAllRules(RuleModel ruleModel) {
        return new MultiResponse().code(200).message("success")
                .result(checkRuleOperation.getAllRules(ruleModel, new ArrayList<>()));
    }

    /**
     * 自定义规则集
     *
     * @param ruleSetModel 自定义实体类
     * @return createRuleSet
     */
    @Override
    public MultiResponse createRuleSet(RuleSetModel ruleSetModel) {
        if (StringUtils.isNotBlank(ruleSetModel.getId())) {
            // 修改规则集
            checkRuleOperation.updateRuleSet(ruleSetModel);
        } else {
            // 新增规则集
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ruleSetModel.setTemplateCreateTime(dateFormat.format(new Date()));
            ruleSetModel.setDefaultTemplate(1);
            // 根据规则集社区和名称判断是否重复
            RuleSetModel ruleSet = new RuleSetModel();
            ruleSet.setProjectName(ruleSetModel.getProjectName());
            ruleSet.setTemplateName(ruleSetModel.getTemplateName());
            List<RuleSetModel> models = checkRuleOperation.queryRuleSet(ruleSet);
            if (models.size() != 0) {
                return new MultiResponse().code(400).message("templateName is repeat");
            }
            checkRuleOperation.createRuleSet(ruleSetModel);
        }
        return new MultiResponse().code(200).message("success");
    }

    /**
     * 获取规则界面左侧的导航栏
     *
     * @return getAllRulesConfig
     */
    @Override
    public MultiResponse getAllRulesConfig() {
        List<RuleModel> allRulesConfig = checkRuleOperation.getAllRulesConfig();
        Map<String, Long> collect = allRulesConfig.stream().collect(Collectors
                .groupingBy(RuleModel::getRuleLanguage, Collectors.summingLong(RuleModel::getCount)));
        return new MultiResponse().code(200).message("success").result(collect);
    }

    /**
     * 获取社区的所有规则集信息（主界面）
     *
     * @param ruleSetModel 查询参数
     * @return queryRuleSet
     */
    @Override
    public MultiResponse queryRuleSet(RuleSetModel ruleSetModel) {
        // 查出所有的系统规则集
        RuleSetModel ruleSets = new RuleSetModel();
        ruleSets.setDefaultTemplate(0);
        List<RuleSetModel> ruleSetModels = checkRuleOperation.queryRuleSet(ruleSets);
        // 查询该社区自定义的规则集
        ruleSetModel.setDefaultTemplate(1);
        List<RuleSetModel> modelList = checkRuleOperation.queryRuleSet(ruleSetModel);
        if (modelList.size() > 0) {
            ruleSetModels.addAll(modelList);
        }
        // 得到每个规则集的规则个数
        for (RuleSetModel ruleSet : ruleSetModels) {
            if (ruleSet.getRuleIds().size() > 0) {
                List<RuleModel> ruleByIds = checkRuleOperation.getRuleByIds(ruleSet.getRuleIds());
                ruleSet.setRuleCount(ruleByIds.size());
                if (ruleByIds.size() != ruleSet.getRuleIds().size()) {
                    // 更换该规则集的规则id
                    List<String> ruleIds = ruleByIds.stream()
                            .map(RuleModel::getRuleId).distinct().collect(Collectors.toList());
                    checkRuleOperation.updateRuleSetToRuleIds(ruleSet.getId(), ruleIds);
                }
            } else {
                ruleSet.setRuleCount(0);
            }
            // 判断是否在使用中
            List<TaskRuleSetVo> taskRuleSet =
                    checkRuleOperation.getTaskRuleSet(ruleSet.getId(), "", "");
            if (taskRuleSet.size() != 0) {
                ruleSet.setUsed(true);
            }
        }
        return new MultiResponse().code(200).message("success").result(ruleSetModels);
    }

    /**
     * 获取规则集详情（配置界面）
     *
     * @param ruleSetModel 查询参数
     * @return queryRuleSet
     */
    @Override
    public MultiResponse queryRuleSetConfig(RuleSetModel ruleSetModel) {
        RuleResultDetailsVo ruleResultDetailsVo = checkRuleOperation.queryRuleSetConfig(ruleSetModel);
        return new MultiResponse().code(200).message("success").result(ruleResultDetailsVo);
    }

    /**
     * 删除自定义规则集
     *
     * @param ruleSetModel 删除参数
     */
    @Override
    public MultiResponse delRuleSet(RuleSetModel ruleSetModel) {
        if (StringUtils.isBlank(ruleSetModel.getId())) {
            return new MultiResponse().code(400).message("ruleSet is error");
        }
        checkRuleOperation.delRuleSet(ruleSetModel.getId());
        return new MultiResponse().code(200).message("success");
    }

    /**
     * 查找任务规则
     *
     * @param ruleSetModel 查找id
     */
    @Override
    public MultiResponse queryTaskById(RuleSetModel ruleSetModel) {
        return new MultiResponse().code(200).
                result(checkRuleOperation.queryRuleById(ruleSetModel));
    }

    /**
     * 更改任务规则
     *
     * @param taskRuleSetVo 修改后的任务规则信息
     */
    @Override
    public MultiResponse updateTaskRule(TaskRuleSetVo taskRuleSetVo) {
        checkRuleOperation.updateTaskRule(taskRuleSetVo);
        return new MultiResponse().code(200).message("success");
    }

    /**
     * 获取仓库的所选规则集
     *
     * @param taskRuleSetVo 查询实体类
     * @return getTaskRule
     */
    @Override
    public MultiResponse getTaskRule(TaskRuleSetVo taskRuleSetVo) {
        LOGGER.info("The rule id {}", taskRuleSetVo.getId());
        List<TaskRuleSetVo> taskRuleSet = checkRuleOperation.getTaskRuleSet("", taskRuleSetVo.getProjectName(),
                taskRuleSetVo.getRepoNameEn());
        if (taskRuleSet.size() == 0) {
            return new MultiResponse().code(400).message("data is null");
        }
        return new MultiResponse().code(200).message("succedss").result(taskRuleSet);
    }
}
