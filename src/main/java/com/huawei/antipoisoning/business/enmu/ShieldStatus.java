package com.huawei.antipoisoning.business.enmu;

/**
 * @author liuwugang LWX1222007
 * @ClassName ShieldStatus
 * @since 2023/2/16 10:52
 */
public enum ShieldStatus {
    /**
     * 问题状态（0：未屏蔽 1：屏蔽中 2：已屏蔽）
     */
    ZERO("0","未屏蔽"),
    ONE("1","屏蔽中"),
    TWO("2","已屏蔽");

    /**
     * code编码
     */
    private String code;

    /**
     * 信息描述
     */
    private String msg;
    //构造方法（枚举的构造方法只允许private类型）
    private ShieldStatus(String code, String msg)
    {
        this.code = code;
        this.msg = msg;
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
