package com.huawei.antipoisoning.business.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author liuwugang LWX1222007
 * @ClassName AntiPoisonChangeBoardModel
 * @since 2023/1/5 15:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AntiPoisonChangeBoardModel {
    //开始时间
    @JsonProperty("startTime")
    private String startTime = null;

    //结束时间
    @JsonProperty("endTime")
    private String endTime = null;

    //仓库列表
    private List<String> info;

    //页数
    private Integer pageNum = null;

    //每页数量
    private Integer pageSize = null;

    //项目名称
    private String projectName = null;
}
