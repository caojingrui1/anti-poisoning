package com.huawei.antipoisoning.business.entity;

import lombok.Data;
import lombok.ToString;

/**
 * vms:外部漏洞源-漏洞修复版本
 *
 * @since: 2022-05-20 11:35
 */
@Data
@ToString
public class AntiEntity {
    /**
     * 修复方式
     */
    private String repairType;

    /**
     * 修补版本，补丁
     */
    private String patchCode;

    /**
     * 补丁链接
     */
    private String patchUrl;

    /**
     * owner修复流程链接
     */
    private String processId;

    /**
     * 内部修复时间
     */
    private String publishTime;
}
