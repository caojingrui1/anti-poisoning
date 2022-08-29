package com.huawei.antipoisoning.business.service.impl;

import com.huawei.antipoisoning.business.entity.checkRule.RuleModel;
import com.huawei.antipoisoning.business.entity.checkRule.RuleResultDetailsVo;
import com.huawei.antipoisoning.business.entity.checkRule.RuleSetModel;
import com.huawei.antipoisoning.business.entity.checkRule.TaskRuleSetVo;
import com.huawei.antipoisoning.business.operation.CheckRuleOperation;
import com.huawei.antipoisoning.business.service.CheckRuleService;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.apache.commons.lang.StringUtils;
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
        return new MultiResponse().code(200).message("success").result(checkRuleOperation.getAllRules(ruleModel, new ArrayList<>()));
    }

    /**
     * 自定义规则集
     *
     * @param ruleSetModel 自定义实体类
     * @return createRuleSet
     */
    @Override
    public MultiResponse createRuleSet(RuleSetModel ruleSetModel) {
        if (StringUtils.isNotBlank(ruleSetModel.getDefaultTemplateId())) {
            // 修改规则集
            checkRuleOperation.delRuleSet(ruleSetModel.getId());
        } else {
            // 新增规则集
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ruleSetModel.setTemplateCreateTime(dateFormat.format(new Date()));
            ruleSetModel.setDefaultTemplate(1);
        }
        checkRuleOperation.createRuleSet(ruleSetModel);
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
        if (StringUtils.isBlank(ruleSetModel.getProjectName())) {
            return new MultiResponse().code(400).message("projectName is error");
        }
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
            ruleSet.setRuleCount(ruleSet.getRuleIds().size());
            // 判断是否在使用中
            List<TaskRuleSetVo> taskRuleSet = checkRuleOperation.getTaskRuleSet(ruleSet.getId(), "", "");
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
}
