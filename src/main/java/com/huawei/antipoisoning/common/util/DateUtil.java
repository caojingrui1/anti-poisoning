/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类
 *
 * @since: 2022/5/31 9:19
 */
public class DateUtil {
    private static final Logger logger = LoggerFactory.getLogger(DateUtil.class);

    /**
     * 获取N小时前/N小时后的时间
     */
    public static String getAppointTime(String time, int hours) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = dateFormat.parse(time);
        } catch (ParseException exception) {
            logger.error(exception.getMessage());
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return dateFormat.format(calendar.getTime());
    }

    /**
     * 将日期转换为字符串
     *
     * @param date 日期
     * @return String
     */
    public static String getDateToString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    /**
     * 将字符串转为日期
     *
     * @param dateStr 字符串
     * @return Date
     */
    public static Date getStringToDate(String dateStr) {
        Date date = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException parseException) {
            logger.error(parseException.getMessage());
        }
        return date;
    }

    /**
     * 获取整点
     *
     * @param date 时间
     * @return String
     */
    public static String getNumTime(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.MILLISECOND,0);
        return dateFormat.format(calendar.getTime());
    }

    /**
     * 转换年月日格式的日期
     *
     * @param dateStr 日期
     * @return Date
     */
    public static Date dateToDate(String dateStr) {
        Date parse = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dateFormat.parse(dateStr);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            parse = simpleDateFormat.parse(simpleDateFormat.format(date));
        } catch (ParseException e) {
            logger.error("format string to date error : {}", e);
        }
        return parse;
    }
}
