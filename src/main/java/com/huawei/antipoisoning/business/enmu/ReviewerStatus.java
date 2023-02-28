package com.huawei.antipoisoning.business.enmu;

/**
 * @author liuwugang LWX1222007
 * @ClassName ReviewerStatus
 * @since 2023/2/16 10:47
 */
public enum ReviewerStatus {
    /**
     * 审核状态 1:待审批 2:已审批 3:未审核前已撤销 4:审核通过后撤销
     */
    ONE("1", "待审批"),
    TWO("2", "已审批"),
    TREE("3", "未审核前已撤销"),
    FOUR("4", "审核通过后撤销");

    /**
     * code编码
     */
    private String code;

    /**
     * 信息描述
     */
    private String msg;

    //构造方法（枚举的构造方法只允许private类型）
    private ReviewerStatus(String state, String info)
    {
        this.code = state;
        this.msg = info;
    }

    public String getCode()
    {
        return code;
    }

    public String getMsg()
    {
        return msg;
    }
}
