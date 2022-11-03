/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.util;

import com.huawei.antipoisoning.business.entity.checkrule.RuleModel;
import com.mongodb.lang.Nullable;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 生成yaml相关工具类
 *
 * @author cqx
 * @since 2022/8/2511:30
 */
public class YamlUtil {
    private static final Logger logger = LoggerFactory.getLogger(YamlUtil.class);

    private static final String CONFIG_PATH = "/tools/SoftwareSupplyChainSecurity-v1/ruleYaml/";

    private static final DumperOptions OPTIONS = new DumperOptions();

    static {
        //设置yaml读取方式为块读取
        OPTIONS.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        OPTIONS.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
        OPTIONS.setPrettyFlow(true);
    }

    /**
     * 根据所有的规则拼接yaml参数
     *
     * @param ruleModels 规则集合
     * @return getRulesMap
     */
    public static boolean getRulesMap(List<RuleModel> ruleModels, String tableName) {
        List<LinkedHashMap<String, Object>> rulesMaps = new ArrayList<>();
        for (RuleModel ruleModel : ruleModels) {
            LinkedHashMap<String, Object> rulesMap = new LinkedHashMap<>();
            String[] files = ruleModel.getFileIds().split(",");
            rulesMap.put("rule", ruleModel.getRuleName());
            rulesMap.put("type", Arrays.toString(files));
            rulesMaps.add(rulesMap);
        }
        if (rulesMaps.size() != 0) {
            yamlFile(rulesMaps, tableName);
            return true;
        }
        return false;
    }

    /**
     * 生成yam文件
     */
    @SneakyThrows
    public static void yamlFile(List<LinkedHashMap<String, Object>> rulesMap, String tableName) {
        // 生成Filter类
        String path = CONFIG_PATH + tableName + ".yaml";
        logger.info("filepath is : {}", getToolPath() + path);
        FileWriter fileWriter = new FileWriter(new File(getToolPath()
                 + path));
        // 生成yaml类
        Yaml yaml = new Yaml(OPTIONS);
        // 拼接参数
        LinkedHashMap<String, Object> yamlMap = new LinkedHashMap<>();
        List<LinkedHashMap<String, Object>> rulesMaps = new ArrayList<>();
        LinkedHashMap<String, Object> rulesMapYaml = new LinkedHashMap<>();
        rulesMapYaml.put("name", "Custom Yara Scan");
        rulesMapYaml.put("rules", rulesMap);
        rulesMaps.add(rulesMapYaml);
        yamlMap.put("scan_tasks", rulesMaps);
        // 拼接屏蔽文件
        List<String> excludeList = Arrays.asList("test", "demo", "example", "node_modules", "/doc/", "min.js");
        yamlMap.put("exclude", excludeList);
        String dump = yaml.dump(yamlMap);
        String replace = dump.replace("\"[", "[").replace("]\"", "]")
                .replace("\\", "");
        fileWriter.write(replace);
        fileWriter.flush();
        fileWriter.close();
    }

    /**
     * 将yaml配置文件转化成map
     *
     * @param fileName 文件目录
     * @return getYamlToMap
     */
    public Map<String, Object> getYamlToMap(String fileName) {
        LinkedHashMap<String, Object> yamls = new LinkedHashMap<>();
        Yaml yaml = new Yaml();
        try {
            @Cleanup InputStream in = YamlUtil.class.getClassLoader().getResourceAsStream(fileName);
            yamls = yaml.loadAs(in, LinkedHashMap.class);
        } catch (Exception e) {
            logger.error("{} load failed !!!", fileName);
        }
        return yamls;
    }

    /**
     * key格式：aaa.bbb.ccc
     * 通过properties的方式获取yaml中的属性值
     *
     * @param key     属性key
     * @param yamlMap yamMap
     * @return getValue
     */
    public Object getValue(String key, Map<String, Object> yamlMap) {
        String[] keys = key.split("[.]");
        Object obj = yamlMap.get(keys[0]);
        if (key.contains(".")) {
            logger.info(key.substring(key.indexOf(".") + 1));
            if (obj instanceof Map) {
                return getValue(key.substring(key.indexOf(".") + 1), (Map<String, Object>) obj);
            } else if (obj instanceof List) {
                return getValue(key.substring(key.indexOf(".") + 1), (Map<String, Object>) ((List<?>) obj).get(0));
            } else {
                return null;
            }
        } else {
            return obj;
        }
    }

    /**
     * 使用递归的方式设置map中的值，仅适合单一属性
     *
     * @param key   属性
     * @param value 属性值
     * @return setValue
     */
    public Map<String, Object> setValue(String key, Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        String[] keys = key.split("[.]");
        int i = keys.length - 1;
        result.put(keys[i], value);
        if (i > 0) {
            return setValue(key.substring(0, key.lastIndexOf(".")), result);
        }
        return result;
    }

    /**
     * 设置属性值
     *
     * @param map   属性map
     * @param key   属性
     * @param value 属性值
     * @return setValue
     */
    public Map<String, Object> setValue(Map<String, Object> map, String key, Object value) {
        String[] keys = key.split("\\.");
        int len = keys.length;
        Map temp = map;
        for (int i = 0; i < len - 1; i++) {
            if (temp.containsKey(keys[i])) {
                temp = (Map) temp.get(keys[i]);
            } else {
                return null;
            }
            if (i == len - 2) {
                temp.put(keys[i + 1], value);
            }
        }
        for (int j = 0; j < len - 1; j++) {
            if (j == len - 1) {
                map.put(keys[j], temp);
            }
        }
        return map;
    }

    /**
     * 修改yaml中属性的值
     *
     * @param key      key是properties的方式： aaa.bbb.ccc (key不存在不修改)
     * @param value    新的属性值 （新属性值和旧属性值一样，不修改）
     * @param yamlName 文件路径
     * @return true 修改成功，false 修改失败。
     */
    public boolean updateYaml(String key, @Nullable Object value, String yamlName) {
        Map<String, Object> yamlToMap = this.getYamlToMap(yamlName);
        if (null == yamlToMap) {
            return false;
        }
        Object oldVal = this.getValue(key, yamlToMap);
        //未找到key 不修改
        if (null == oldVal) {
            logger.error("{} key is not found", key);
            return false;
        }
        Yaml yaml = new Yaml(OPTIONS);
        String path = this.getClass().getClassLoader().getResource(yamlName).getPath();
        try {
            Map<String, Object> resultMap = this.setValue(yamlToMap, key, value);
            if (resultMap != null) {
                yaml.dump(this.setValue(yamlToMap, key, value), new FileWriter(path));
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("yaml file update failed !");
            logger.error("msg : {} ", e.getMessage());
            logger.error("cause : {} ", e.getCause());
        }
        return false;
    }

    /**
     * 获取工具包所在主目录。
     *
     * @return path
     */
    public static String getToolPath() {
        String path = YamlUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = path.replace("file:", "");
        if (path.contains("jar")) {
            path = path.substring(0, path.lastIndexOf("."));
            path = path.substring(0, path.lastIndexOf("/"));
            return path.replace("/target", "");
        }
        return path.replace("/target", "").replace("/classes", "");
    }
}
