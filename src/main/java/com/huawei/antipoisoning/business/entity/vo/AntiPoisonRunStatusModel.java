package com.huawei.antipoisoning.business.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author liuwugang LWX1222007
 * @ClassName AntiPoisonRunStatusModel1
 * @since 2022/12/8 14:47
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AntiPoisonRunStatusModel {

    //仓库名列表
    private List<String> repoList;

    //项目名称列表
    private List<String> projectNameList;

}
