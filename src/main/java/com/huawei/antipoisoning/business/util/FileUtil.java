/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * 文件处理工具类。
 *
 * @since 2022-10-31
 * @author zyx
 */
public class FileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 删除目录及目录下的文件
     * @param dir：要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator))
            dir = dir + File.separator;
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            LOGGER.error("删除目录失败：{} 不存在！", dir);
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            try {
                if (files[i].isFile()) {
                    flag = deleteFile(files[i].getCanonicalPath());
                    if (!flag)
                        break;
                }
                // 删除子目录
                else if (files[i].isDirectory()) {
                    flag = deleteDirectory(files[i].getCanonicalPath());
                    if (!flag)
                        break;
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
        if (!flag) {
            LOGGER.error("删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 删除单个文件
     * @param fileName：要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                LOGGER.error("删除单个文件：{} 失败！", fileName);
                return false;
            }
        } else {
            LOGGER.error("删除单个文件失败：{} 不存在！", fileName);
            return false;
        }
    }
}
