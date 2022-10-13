/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.common.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 将枚举数据整理成树形结构
 *
 * @author cqx
 * @since 2022/05/18
 */
public class TreeUtil {
    /**
     * 将泛型数据整理为树形结构
     *
     * @param arr   整体数据
     * @param id    唯一性id
     * @param pid   父节点id
     * @param child 子节点id
     * @return listToTree
     */
    public static JSONArray listToTree(JSONArray arr, String id, String pid, String child) {
        JSONArray result = new JSONArray();
        JSONObject hash = new JSONObject();
        // 将数组转为Object的形式，key为数组中的id
        for (Object object : arr) {
            JSONObject json = object instanceof JSONObject ? (JSONObject) object : null;
            hash.put(json.getString(id), json);
        }
        // 遍历结果集
        for (Object object : arr) {
            JSONObject aVal = object instanceof JSONObject ? (JSONObject) object : null;
            // 在hash中取出key为单条记录中pid的值
            JSONObject hashVP = hash.get(aVal.get(pid).toString()) instanceof JSONObject
                    ? (JSONObject) hash.get(aVal.get(pid).toString()) : null;
            // 如有父节点，将她添加到孩子节点的集合中
            if (hashVP != null) {
                // 检查是否有child属性
                if (hashVP.get(child) != null) {
                    JSONArray ch = hashVP.get(child) instanceof JSONArray ? (JSONArray) hashVP.get(child) : null;
                    ch.add(aVal);
                    hashVP.put(child, ch);
                } else {
                    JSONArray ch = new JSONArray();
                    ch.add(aVal);
                    hashVP.put(child, ch);
                }
            } else {
                result.add(aVal);
            }
        }
        return result;
    }
}