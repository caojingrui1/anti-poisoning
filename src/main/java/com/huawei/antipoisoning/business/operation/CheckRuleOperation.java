package com.huawei.antipoisoning.business.operation;

import com.huawei.antipoisoning.business.entity.checkRule.RuleModel;
import com.huawei.antipoisoning.business.entity.checkRule.RuleResultDetailsVo;
import com.huawei.antipoisoning.business.entity.checkRule.RuleSetModel;
import com.huawei.antipoisoning.business.entity.checkRule.TaskRuleSetVo;
import com.huawei.antipoisoning.business.entity.vo.PageVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.huawei.antipoisoning.business.enmu.CollectionTableName.ANTI_CHECK_RULE;
import static com.huawei.antipoisoning.business.enmu.CollectionTableName.ANTI_CHECK_RULE_SET;
import static com.huawei.antipoisoning.business.enmu.CollectionTableName.ANTI_TASK_RULE_SET;

/**
 * 扫描结果包裹数据存档
 *
 * @since: 2022/5/31 17:01
 */
@Component
public class CheckRuleOperation {
    @Resource
    private MongoTemplate mongoTemplate;


    /**
     * 根据条件获取规则详情
     *
     * @param rule    查询参数
     * @param ruleIds 规则id数组
     * @return getAllRules
     */
    public PageVo getAllRules(RuleModel rule, List<String> ruleIds) {
        Criteria criteria = new Criteria();
        if (ruleIds.size() > 0) {
            criteria.and("rule_id").in(ruleIds);
        }
        if (StringUtils.isNotBlank(rule.getRuleId())) {
            criteria.and("rule_id").is(rule.getRuleId());
        }
        if (StringUtils.isNotBlank(rule.getRuleLanguage())) {
            criteria.and("rule_language").is(rule.getRuleLanguage());
        }
        if (StringUtils.isNotBlank(rule.getRuleName())) {
            Pattern pattern = Pattern
                    .compile("^.*" + escapeSpecialWord(rule.getRuleName()) + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and("rule_name").regex(pattern);
        }
        Query query = Query.query(criteria);
        // 总数量
        long count = mongoTemplate.count(query, ANTI_CHECK_RULE);
        if (rule.getPageNum() != null && rule.getPageSize() != null && count > 0) {
            query.skip((long) (rule.getPageNum() - 1) * rule.getPageSize());
            query.limit(rule.getPageSize());
        }
        List<RuleModel> codeCheckRuleVos = mongoTemplate.find(query, RuleModel.class, ANTI_CHECK_RULE);
        return new PageVo(count, codeCheckRuleVos);
    }

    /**
     * 对特殊字符进行转义
     *
     * @param keyword 字符串
     * @return escapeSpecialWord
     */
    private String escapeSpecialWord(String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|", "\n"};
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }

    /**
     * 自定义规则集
     *
     * @param ruleSetModel 规则集参数
     */
    public void createRuleSet(RuleSetModel ruleSetModel) {
        mongoTemplate.save(ruleSetModel, ANTI_CHECK_RULE_SET).getId();
    }

    /**
     * 添加规则
     *
     * @param ruleModel 规则实体类
     */
    public void createRule(List<RuleModel> ruleModel) {
        mongoTemplate.insert(ruleModel, ANTI_CHECK_RULE);
    }

    /**
     * 对语言进行分组获取总数
     *
     * @return getAllRulesConfig
     */
    public List<RuleModel> getAllRulesConfig() {
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.group("rule_language")
                .first("rule_language").as("rule_language")
                .count().as("count"));
        return mongoTemplate.aggregate(Aggregation.newAggregation(operations),
                ANTI_CHECK_RULE, RuleModel.class).getMappedResults();
    }

    /**
     * 根据条件查询规则集信息
     *
     * @param ruleSetModel 查询参数
     * @return queryRuleSet
     */
    public List<RuleSetModel> queryRuleSet(RuleSetModel ruleSetModel) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotBlank(ruleSetModel.getId())) {
            criteria.and("_id").is(ruleSetModel.getId());
        }
        if (StringUtils.isNotBlank(ruleSetModel.getTemplateName())) {
            criteria.and("template_name").is(ruleSetModel.getTemplateName());
        }
        if (Objects.nonNull(ruleSetModel.getDefaultTemplate())) {
            criteria.and("default_template").is(ruleSetModel.getDefaultTemplate());
        }
        if (StringUtils.isNotBlank(ruleSetModel.getLanguage())) {
            criteria.and("language").is(ruleSetModel.getLanguage());
        }
        return mongoTemplate.find(Query.query(criteria), RuleSetModel.class, ANTI_CHECK_RULE_SET);
    }

    /**
     * 根据主键删除规则集
     *
     * @param id 主键id
     */
    public void delRuleSet(String id) {
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(id)), ANTI_CHECK_RULE);
    }

    /**
     * 根据规则集参数查询任务使用
     *
     * @param id          任务规则集的主键id
     * @param projectName 社区
     * @param repoName    仓库
     * @return RuleModel
     */
    public List<TaskRuleSetVo> getTaskRuleSet(String id, String projectName, String repoName) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotBlank(id)) {
            criteria.and("_id").is(id);
        }
        if (StringUtils.isNotBlank(projectName)) {
            criteria.and("project_name").is(projectName);
        }
        if (StringUtils.isNotBlank(repoName)) {
            criteria.and("repo_name_en").is(repoName);
        }
        return mongoTemplate.find(Query.query(criteria), TaskRuleSetVo.class, ANTI_TASK_RULE_SET);
    }

    /**
     * 获取规则配置界面的信息
     *
     * @param ruleSetModel 查询参数
     * @return queryRuleSetConfig
     */
    public RuleResultDetailsVo queryRuleSetConfig(RuleSetModel ruleSetModel) {
        Criteria criteria = new Criteria();
        if (ruleSetModel.getRuleIds().size() > 0) {
            if ("disenable".equals(ruleSetModel.getType())) {
                criteria.and("rule_id").nin(ruleSetModel.getRuleIds());
            } else {
                criteria.and("rule_id").in(ruleSetModel.getRuleIds());
            }
        }
        if (StringUtils.isNotBlank(ruleSetModel.getLanguage())) {
            criteria.and("rule_language").is(ruleSetModel.getLanguage());
        }
        if (StringUtils.isNotBlank(ruleSetModel.getTemplateName())) {
            Pattern pattern = Pattern
                    .compile("^.*" + escapeSpecialWord(ruleSetModel.getTemplateName()) + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and("rule_name").regex(pattern);
        }
        Query query = Query.query(criteria);
        // 总数量
        List<RuleModel> ruleVosCount = mongoTemplate.find(query, RuleModel.class, ANTI_CHECK_RULE);
        if (ruleSetModel.getPageNum() != null && ruleSetModel.getPageSize() != null && ruleVosCount.size() > 0) {
            query.skip((long) (ruleSetModel.getPageNum() - 1) * ruleSetModel.getPageSize());
            query.limit(ruleSetModel.getPageSize());
        }
        int enableCount = 0;
        if (StringUtils.isNotBlank(ruleSetModel.getId()) && queryRuleSet(ruleSetModel).size() == 1) {
            // 对比当前规则是否启用
            List<String> ruleCount = queryRuleSet(ruleSetModel).get(0).getRuleIds();
            for (RuleModel ruleModel : ruleVosCount) {
                if (ruleCount.contains(ruleModel.getRuleId())) {
                    ruleModel.setIsUsed("1");
                    enableCount++;
                } else {
                    ruleModel.setIsUsed("0");
                }
            }

        }
        List<RuleModel> ruleModelList = mongoTemplate.find(query, RuleModel.class, ANTI_CHECK_RULE);
        return new RuleResultDetailsVo(Long.valueOf(ruleVosCount.size()).intValue(), enableCount, ruleModelList);
    }
}
