package com.pksh.myutils.conversion;

import androidx.annotation.NonNull;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 类型转换工具类
 * 曹丽超
 * 2020/12/04
 */
public class ConversionType {

    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(@NonNull String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = 0;
        try {
            lt = simpleDateFormat.parse(s).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }
    /**
     * 时间转换时间戳
     * @param s
     * @return
     * @throws ParseException
     */
    public static String dateToStamp(@NonNull String s) throws ParseException {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }

    /**
     * 通过反射机制对象克隆
     * @param obj
     * @return
     * @throws Exception
     */
    public static Object colneJavaBean(@NonNull Object obj) throws Exception {
        Class<?> classType = obj.getClass();//获取class对象
        Object objRes = classType.newInstance();//构建目标对象
        for (Field field : classType.getDeclaredFields()) {
            field.setAccessible(true);//设置可访问权限
            Object value = field.get(obj);//利用get方法取obj的值
            field.set(objRes, value);
        }
        return objRes;
    }

    /**
     * 克隆map
     * @param map
     * @return
     */
    public static Map<Object,Object> cloneMap(@NonNull Map<Object,Object> map){
        if (map == null) return null;
        Map<Object,Object> newMap = new HashMap();
        for (Map.Entry<Object, Object> key :map.entrySet()) {
            newMap.put(key.getKey(),key.getValue());
        }
        return newMap;
    }

    /**
     * 克隆list
     * @param list
     * @return
     */
    public static List<Object> cloneList(@NonNull List<Object> list){
        if (list == null) return null;
        return new ArrayList<>(list);
    }

    /**
     * Map的值转换List
     * @param map
     * @param <T>
     * @return
     */
    public static <T> List<T> MapValueToList(@NonNull Map<Object,T> map){
        return new ArrayList<>(map.values());
    }

    /**
     * Map的键转换List
     * @param map
     * @param <T>
     * @return
     */
    public static <T> List<Object> MapKeyToList(@NonNull Map<Object,T> map){
        return new ArrayList<>(map.keySet());
    }

    /**
     * JSON数据转后台类
     */
    public static <T> ArrayList<T> jsonToJavaBean(@NonNull String jsonStr,Class<T> t){
        JSONArray json = JSONArray.parseArray(jsonStr);
        ArrayList<T> lists = new ArrayList<>();
        for (int i = 0; i < json.size(); i++) {
            JSONObject jsonObject = json.getJSONObject(i);
            T list = jsonObject.toJavaObject(t);
            lists.add(list);
        }
        if (lists.size() < 1) return null;
        return lists;
    }

}
