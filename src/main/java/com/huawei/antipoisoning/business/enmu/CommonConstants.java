package com.huawei.antipoisoning.business.enmu;

/**
 * 项目常量接口
 *
 * @since 2021-10-1
 */
public interface CommonConstants {
    /**
     * 项目路径
     */
    class commUrl {

        public static final String SCAN_RESULT_PATH = "/root/softwareFile/report/";

        public static final String SCAN_TOOL_PATH = "/root/opt/SoftwareSupplyChainSecurity-v1/openeuler_scan.py";

        public static final String SCAN_TOOL_FILE = "/root/opt/SoftwareSupplyChainSecurity-v1/";

        public static final String REPO_PATH = "/root/softwareFile/download/";
    }

    /**
     * 魔鬼数字
     */
    class CommonNumber {
        /**
         * 数字0
         */
        public static final String NUMBER_ZERO = "0";

        /**
         * 数字9
         */
        public static final String NUMBER_NINE = "9";

        /**
         * 数字1
         */
        public static final Integer NUMBER_ONE = 1;

        /**
         * 数字0
         */
        public static final Integer NUMBER = 0;

        /**
         * 数字2
         */
        public static final Integer NUMBER_TWO = 2;

        /**
         * 数字3
         */
        public static final Integer NUMBER_THREE = 3;

        /**
         * 字符串1
         */
        public static final String ONE = "1";

        /**
         * 字符串2
         */
        public static final String TWO = "2";

        /**
         * 字符串3
         */
        public static final String THREE = "3";

        /**
         * 字符串4
         */
        public static final String FOUR = "4";

        /**
         * 字符串5
         */
        public static final String FIVE = "5";

        /**
         * 最大保存的历史文件浏览
         */
        public static final int MAX_HISTORY = 20;
    }
}
