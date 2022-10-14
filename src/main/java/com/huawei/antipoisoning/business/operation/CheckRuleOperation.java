/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.operation;

import com.huawei.antipoisoning.business.enmu.CollectionTableName;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.entity.checkrule.RuleModel;
import com.huawei.antipoisoning.business.entity.checkrule.RuleResultDetailsVo;
import com.huawei.antipoisoning.business.entity.checkrule.RuleSetModel;
import com.huawei.antipoisoning.business.entity.checkrule.TaskRuleSetVo;
import com.huawei.antipoisoning.business.entity.vo.PageVo;
import com.huawei.antipoisoning.business.service.impl.CheckRuleServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 扫描结果包裹数据存档
 *
 * @since: 2022/5/31 17:01
 */
@Component
public class CheckRuleOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckRuleOperation.class);

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
        if (StringUtils.isNotBlank(rule.getRuleDesc())) {
            Pattern pattern = Pattern
                    .compile("^.*" + escapeSpecialWord(rule.getRuleDesc()) + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and("rule_desc").regex(pattern);
        }
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
     * 根据主键删除规则集
     *
     * @param id 主键id
     */
    public void delRuleSet(String id) {
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(id)), CollectionTableName.ANTI_CHECK_RULE_SET);
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
        if (StringUtils.isNotBlank(taskRuleSetVo.getId())) {
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
        Set<String> tables = mongoTemplate.getCollectionNames();
        for (String table : tables) {
            LOGGER.info("dbTable : {}", table);
        }
        TaskRuleSetVo taskRuleSetVo1 = mongoTemplate.findOne(Query.query(Criteria.where("_id").is("63313feba1506f131b5dd29a")),
                TaskRuleSetVo.class, CollectionTableName.ANTI_TASK_RULE_SET);
        LOGGER.info("taskRuleSetVo1 : {}", taskRuleSetVo1);
        TaskRuleSetVo taskRuleSetVo2 = mongoTemplate.findById("63313feba1506f131b5dd29a",
                TaskRuleSetVo.class, CollectionTableName.ANTI_TASK_RULE_SET);
        LOGGER.info("taskRuleSetVo2 : {}", taskRuleSetVo2);
        TaskRuleSetVo taskRuleSetVo3 = mongoTemplate.findOne(Query.query(Criteria.where("repo_name_en").is("pkgship")),
                TaskRuleSetVo.class, CollectionTableName.ANTI_TASK_RULE_SET);
        LOGGER.info("taskRuleSetVo3 : {}", taskRuleSetVo3);
        return mongoTemplate.findOne(Query.query(Criteria.where("_id").is(ruleSetModel.getId())),
                TaskRuleSetVo.class, CollectionTableName.ANTI_TASK_RULE_SET);
    }
}
