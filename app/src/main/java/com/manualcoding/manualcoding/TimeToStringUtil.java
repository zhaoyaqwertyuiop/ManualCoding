package com.manualcoding.manualcoding;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhaoya on 2016/10/8.
 */
public class TimeToStringUtil {

    /**
     * 毫秒值转string
     * @param time 毫秒值
     * @param formatStr yyyyMMddHHmmss
     * @return
     */
    public static final String format(String time, String formatStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        Date date = new Date(Long.parseLong(time) + 0);
        return sdf.format(date);
    }

    /**
     * 毫秒值转string
     * @param time 毫秒值
     * @param formatStr yyyyMMddHHmmss
     * @return
     */
    public static final String format(long time, String formatStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        Date date = new Date(time);
        return sdf.format(date);
    }

    /**
     * 毫秒值转距离现在的时间
     * @param time 毫秒值
     * @return
     */
    public static final String formatCurrentTime(String time) {
        if (TextUtils.isEmpty(time)) {
            return "";
        }
        Time curTime = new Time(System.currentTimeMillis());
        Time srcTime = new Time(Long.parseLong(time));

        long difference = System.currentTimeMillis() - Long.parseLong(time);
        long s = difference / 1000; // 秒
        long m = s / 60; // 分钟
        long H = m / 60; // 小时
        long d = H / 24; // 天
        String result = "";

        // 计算日差,这里假设每个月30天
        long dayDifference = ((curTime.year - srcTime.year) * 12 + curTime.month - srcTime.month) * 30 + curTime.day - srcTime.day;

//        if (dayDifference == 0) {
//            if (H > 0) {
//                result = H + "小时前";
//            } else if (m > 0) {
//                result = m + "分钟前";
//            } else {
//                result = "刚刚";
//            }
//        } else if (dayDifference == 1) {
//            result = "昨天";
//        } else if (dayDifference == 2) {
//            result = "前天";
//        } else if (dayDifference > 2 && dayDifference <= 5) {
//            result = dayDifference + "天前";
//        } else if (dayDifference > 5) { // 大于5天,显示年月日
//            result = TimeToStringUtil.format(time, "yyyy.MM.dd");
//        }

        if (dayDifference > 5) { // 大于5天,显示年月日
            result = TimeToStringUtil.format(time, "yyyy.MM.dd");
        } else if (dayDifference > 2 && dayDifference <= 5) {
            result = dayDifference + "天前";
        } else if (dayDifference == 2) {
            result = "前天";
        } else if (dayDifference == 1) {
            result = "昨天";
        } else if (H > 0 ) {
            result = H + "小时前";
        } else if (m > 0) {
            result = m + "分钟前";
        } else {
            result = "刚刚";
        }
        return result;
    }

    public static class Time {
        int year, month, day, hour, minite, second;

        public Time(long longTime) {
            String strTime = format(longTime, "yyyyMMddHHmmss");
            this.year = Integer.parseInt(strTime.substring(0, 4));
            this.month = Integer.parseInt(strTime.substring(4, 6));
            this.day = Integer.parseInt(strTime.substring(6, 8));
            this.hour = Integer.parseInt(strTime.substring(8, 10));
            this.minite = Integer.parseInt(strTime.substring(10, 12));
            this.second = Integer.parseInt(strTime.substring(12, 14));
        }
    }

    /**
     * 字符串 转 Date
     * @param str
     * @return
     */
    public static final Date getDate(String str) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = formatter.parse(str);
            return date;
        } catch (Exception e) {
        }
        return null;


    }
}
