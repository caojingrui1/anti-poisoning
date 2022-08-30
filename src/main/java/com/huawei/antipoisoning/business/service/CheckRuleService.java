package com.huawei.antipoisoning.business.service;


import com.huawei.antipoisoning.business.entity.checkRule.RuleModel;
import com.huawei.antipoisoning.business.entity.checkRule.RuleSetModel;
import com.huawei.antipoisoning.common.entity.MultiResponse;

/**
 * 规则方法接口
 */
public interface CheckRuleService {

    MultiResponse getAllRules(RuleModel ruleModel);

    MultiResponse createRuleSet(RuleSetModel ruleSetModel);

    MultiResponse getAllRulesConfig();

    MultiResponse queryRuleSet(RuleSetModel ruleSetModel);

    MultiResponse queryRuleSetConfig(RuleSetModel ruleSetModel);

    MultiResponse delRuleSet(RuleSetModel ruleSetModel);
}
