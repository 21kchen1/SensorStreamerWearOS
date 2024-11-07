package com.SensorStreamer.Utils;

/**
 * 判断类型转换是否成功的工具函数
 * @author chen
 * @version 1.0
 * */

public class TypeTranDeter {
    public static boolean isStr2Num(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
