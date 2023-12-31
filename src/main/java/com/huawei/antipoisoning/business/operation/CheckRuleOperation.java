/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.operation;

import com.huawei.antipoisoning.business.enmu.CollectionTableName;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.entity.checkrule.RuleModel;
import com.huawei.antipoisoning.business.entity.checkrule.RuleSetModel;
import com.huawei.antipoisoning.business.entity.checkrule.TaskRuleSetVo;
import com.huawei.antipoisoning.business.entity.checkrule.RuleResultDetailsVo;
import com.huawei.antipoisoning.business.entity.vo.PageVo;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;


import java.util.Set;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * 规则操作类。
 *
 * @since: 2023/01/31 17:01
 */
@Component
public class CheckRuleOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckRuleOperation.class);

    @Autowired
    @Qualifier("poisonMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * 根据条件获取规则详情
     *
     * @param rule    查询参数
     * @param ruleIds 规则id数组
     * @return getAllRules
     */
    public PageVo getAllRules(RuleModel rule, Set<String> ruleIds) {
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
        if (StringUtils.isNotBlank(rule.getRuleDesc())) {
            Pattern pattern = Pattern
                    .compile("^.*" + escapeSpecialWord(rule.getRuleDesc()) + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and("rule_desc").regex(pattern);
        }
        criteria.and("status").is("1");
        Query query = Query.query(criteria);
        // 总数量
        long count = mongoTemplate.count(query, CollectionTableName.ANTI_CHECK_RULE);
        if (rule.getPageNum() != null && rule.getPageSize() != null && count > 0) {
            query.skip((long) (rule.getPageNum() - 1) * rule.getPageSize());
            query.limit(rule.getPageSize());
        }
        List<RuleModel> codeCheckRuleVos = mongoTemplate.find(query, RuleModel.class,
                CollectionTableName.ANTI_CHECK_RULE);
        return new PageVo(count, codeCheckRuleVos);
    }


    /**
     * 根据条件获取规则详情
     *
     * @param rule    查询参数
     * @return getAllRules
     */
    public List<RuleModel> getAllPoisonRule(RuleModel rule) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotBlank(rule.getRuleLanguage())) {
            criteria.and("rule_language").is(rule.getRuleLanguage());
        }
        if (StringUtils.isNotBlank(rule.getRuleName())) {
            Pattern pattern = Pattern
                    .compile("^.*" + escapeSpecialWord(rule.getRuleName()) + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and("rule_name").regex(pattern);
        }
        if (StringUtils.isNotBlank(rule.getRuleDesc())) {
            Pattern pattern = Pattern
                    .compile("^.*" + escapeSpecialWord(rule.getRuleDesc()) + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and("rule_desc").regex(pattern);
        }
        criteria.and("status").is("1");
        Query query = Query.query(criteria);
        return mongoTemplate.find(query, RuleModel.class,
                CollectionTableName.ANTI_CHECK_RULE);
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
        mongoTemplate.insert(ruleSetModel, CollectionTableName.ANTI_CHECK_RULE_SET);
    }

    /**
     * 添加规则
     *
     * @param ruleModel 规则实体类
     */
    public void createRule(List<RuleModel> ruleModel) {
        mongoTemplate.insert(ruleModel, CollectionTableName.ANTI_CHECK_RULE);
    }

    /**
     * 更新规则。
     *
     * @param ruleModel 规则实体类
     */
    public void updateRule(RuleModel ruleModel) {
        Criteria criteria = Criteria.where("_id").is(ruleModel.getId());
        Update update = new Update();
        update.set("rule_name", ruleModel.getRuleName());
        update.set("rule_language", ruleModel.getRuleLanguage());
        update.set("revise_opinion", ruleModel.getReviseOpinion());
        update.set("file_ids", ruleModel.getFileIds());
        update.set("right_example", ruleModel.getRightExample());
        update.set("error_example", ruleModel.getErrorExample());
        update.set("rule_desc", ruleModel.getRuleDesc());
        update.set("tag", ruleModel.getTag());
        update.set("link_name", ruleModel.getLinkName());
        update.set("status", ruleModel.getStatus());
        update.set("update_time", ruleModel.getUpdateTime());
        mongoTemplate.updateFirst(Query.query(criteria), update, CollectionTableName.ANTI_CHECK_RULE);
    }

    /**
     * 对语言进行分组获取总数
     *
     * @return getAllRulesConfig
     */
    public List<RuleModel> getAllRulesConfig() {
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("status").is("1")));
        operations.add(Aggregation.group("rule_language")
                .first("rule_language").as("rule_language")
                .count().as("count"));
        return mongoTemplate.aggregate(Aggregation.newAggregation(operations),
                CollectionTableName.ANTI_CHECK_RULE, RuleModel.class).getMappedResults();
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
        if (StringUtils.isNotBlank(ruleSetModel.getProjectName())) {
            criteria.and("project_name").is(ruleSetModel.getProjectName());
        }
        return mongoTemplate.find(Query.query(criteria), RuleSetModel.class, CollectionTableName.ANTI_CHECK_RULE_SET);
    }

    /**
     * 根据条件查询规则集信息
     *
     * @param removeIds 查询参数
     * @return queryRuleSet
     */
    public List<RuleSetModel> queryRuleSetByRuleId(List<String> removeIds) {
        Criteria criteria = new Criteria();
        if (CollectionUtils.isNotEmpty(removeIds)) {
            criteria.and("rule_ids").in(removeIds);
        }
        return mongoTemplate.find(Query.query(criteria), RuleSetModel.class, CollectionTableName.ANTI_CHECK_RULE_SET);
    }

    /**
     * 根据主键删除规则集
     *
     * @param id 主键id
     */
    public MultiResponse delRuleSet(String id) {
        RuleSetModel ruleSetModel = new RuleSetModel();
        ruleSetModel.setId(id);
        mongoTemplate.remove(ruleSetModel, CollectionTableName.ANTI_CHECK_RULE_SET);
        return  new MultiResponse().code(200).message("success");
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
            criteria.and("anti_check_rules.ruleSetId").in(id);
        }
        if (StringUtils.isNotBlank(projectName)) {
            criteria.and("project_name").is(projectName);
        }
        if (StringUtils.isNotBlank(repoName)) {
            criteria.and("repo_name_en").is(repoName);
        }
        return mongoTemplate.find(Query.query(criteria), TaskRuleSetVo.class, CollectionTableName.ANTI_TASK_RULE_SET);
    }

    /**
     * 获取规则配置界面的信息
     *
     * @param ruleSetModel 查询参数
     * @return queryRuleSetConfig
     */
    public RuleResultDetailsVo queryRuleSetConfig(RuleSetModel ruleSetModel) {
        Criteria criteria = new Criteria();
        criteria.and("status").is("1");
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
        if (StringUtils.isNotBlank(ruleSetModel.getRuleName())) {
            Pattern pattern = Pattern
                    .compile("^.*" +
                            escapeSpecialWord(ruleSetModel.getRuleName()) + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and("rule_name").regex(pattern);
        }
        Query query = Query.query(criteria);
        // 总数量
        List<RuleModel> ruleVosCount = mongoTemplate.find(query, RuleModel.class, CollectionTableName.ANTI_CHECK_RULE);
        if (ruleSetModel.getPageNum() != null && ruleSetModel.getPageSize() != null && ruleVosCount.size() > 0) {
            query.skip((long) (ruleSetModel.getPageNum() - 1) * ruleSetModel.getPageSize());
            query.limit(ruleSetModel.getPageSize());
        }
        int enableCount = 0;
        if (StringUtils.isNotEmpty(ruleSetModel.getId()) && queryRuleSet(ruleSetModel).size() == 1) {
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
        List<RuleModel> ruleModelList = mongoTemplate.find(query, RuleModel.class, CollectionTableName.ANTI_CHECK_RULE);
        return new RuleResultDetailsVo(Long.valueOf(ruleVosCount.size()).intValue(), enableCount, ruleModelList);
    }

    /**
     * 通过规则id数组获取规则详情
     *
     * @param ruleIds id数组
     * @return getRuleByIds
     */
    public List<RuleModel> getRuleByIds(List<String> ruleIds) {
        Criteria criteria = new Criteria();
        if (ruleIds.size() > 0) {
            criteria.and("rule_id").in(ruleIds);
        }
        return mongoTemplate.find(Query.query(criteria), RuleModel.class, CollectionTableName.ANTI_CHECK_RULE);
    }

    /**
     * 修改自定义规则集
     *
     * @param ruleSetModel updateRuleSet
     */
    public void updateRuleSet(RuleSetModel ruleSetModel) {
        mongoTemplate.findAndReplace(Query.query(Criteria.where("_id").is(ruleSetModel.getId())),
                ruleSetModel, CollectionTableName.ANTI_CHECK_RULE_SET);
    }

    /**
     * 去掉规则集无用的规则
     *
     * @param id      主键id
     * @param ruleIds 规则数据
     */
    public void updateRuleSetToRuleIds(String id, List<String> ruleIds) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("rule_ids", ruleIds);
        mongoTemplate.updateFirst(Query.query(criteria), update, CollectionTableName.ANTI_CHECK_RULE_SET);
    }

    /**
     * 修改任务的规则集信息
     *
     * @param taskRuleSetVo 参数体
     */
    public void updateTaskRule(TaskRuleSetVo taskRuleSetVo) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotEmpty(taskRuleSetVo.getId())) {
            criteria.and("_id").is(taskRuleSetVo.getId());
        }
        if (StringUtils.isNotBlank(taskRuleSetVo.getProjectName())
                && StringUtils.isNotBlank(taskRuleSetVo.getRepoNameEn())) {
            criteria.and("project_name").is(taskRuleSetVo.getProjectName()).and("repo_name_en")
                    .is(taskRuleSetVo.getRepoNameEn());
        }
        Query query = Query.query(criteria);
        Update update = new Update();
        if (!taskRuleSetVo.getAntiCheckRules().isEmpty()) {
            update.set("anti_check_rules", taskRuleSetVo.getAntiCheckRules());
        }
        mongoTemplate.upsert(query, update, CollectionTableName.ANTI_TASK_RULE_SET).getModifiedCount();
    }

    /**
     * 删除任务的规则集信息
     *
     * @param taskEntity 参数体
     */
    public void delTaskRuleSet(TaskEntity taskEntity) {
        mongoTemplate.remove(Query.query(Criteria.where("project_name").is(taskEntity.getProjectName())
                .and("repo_name_en").is(taskEntity.getRepoName())), CollectionTableName.ANTI_TASK_RULE_SET);
    }

    /**
     * 查找自定义规则集
     *
     * @param ruleSetModel updateRuleSet
     * @return TaskRuleSetVo
     */
    public TaskRuleSetVo queryRuleById(RuleSetModel ruleSetModel) {
        return mongoTemplate.findOne(Query.query(Criteria.where("_id").is(ruleSetModel.getId())),
                TaskRuleSetVo.class, CollectionTableName.ANTI_TASK_RULE_SET);
    }
}
