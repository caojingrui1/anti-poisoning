package com.huawei.antipoisoning.business.entity.checkRule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * 语言实体
 *
 * @author zsn
 * @since 2021-11-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRuleSetVo {
    private String id;

    @Field("repo_name_en")
    private String repoNameEn;

    @Field("project_name")
    private String projectName;

    @Field("languages")
    private List<CheckRuleSet> languages;
}
