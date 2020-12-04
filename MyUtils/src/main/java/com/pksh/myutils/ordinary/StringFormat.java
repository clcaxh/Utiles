package com.pksh.myutils.ordinary;

import java.util.List;
/**
 * 字符串工具类
 * 曹丽超
 * 2020/12/04
 */
public class StringFormat {
    /**
     * 判断多个字符串是否有为空的 有一个为空即true
     * @param strs 字符串数组
     * @return 字符串是否为空
     */
    public static Boolean isNull(String... strs){
        for (String str : strs){
            if (str == null || str.equals("null") || str.trim().length() == 0 || str.trim().equals(""))
                return true;
        }
        return false;
    }

    /**
     * 判断字符串是否为空 为空即true
     *
     * @param str 字符串
     * @return 字符串是否为空
     */
    public static Boolean isNull(String str){
        return str == null || str.trim().equals("null") || str.trim().length() == 0 || str.trim().equals(" ");
    }

    /**
     * 字符串是否是整数
     * @param str 3
     * @return 3
     */
    public static Boolean isInteger(String str){
        try {
            Integer.parseInt(str);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 字符串是否数字组成
     * @param str 3
     * @return 3
     */
    public static Boolean isNumberComposition(String str){
        char[] data = str.toCharArray();         // 将字符串变为字符数组，可以取出每一位字符进行判断
        for (char datum : data) {   // 循环判断
            if (datum > '9' || datum < '0') { // 不是数字字符范围
                return false;                     // 后续不再判断
            }
        }
        return true;                        // 如果全部验证通过返回true
    }

    /**
     * list内的String转换为String类型的长段，使用”,“分割
     * @param list 存放String的list
     * @return 使用,分割的字符串
     */
    public static String listToString (List<String> list){
        if (list == null || list.size() <1) return null;
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : list){
            stringBuilder.append(str).append(",");
        }
        stringBuilder.delete(stringBuilder.lastIndexOf(","),stringBuilder.length());
        return stringBuilder.toString();
    }


}
