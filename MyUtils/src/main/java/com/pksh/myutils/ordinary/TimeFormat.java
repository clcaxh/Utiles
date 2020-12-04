package com.pksh.myutils.ordinary;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.pksh.myutils.conversion.ConversionType.stampToDate;

/**
 * 时间工具类
 * 曹丽超
 * 2020/12/04
 */
public class TimeFormat {
    /**
     * 获取当前设备时间：格式化样式：yyyy-MM-dd HH:mm:ss
     * @return 返回当前时间 类型：yyyy-MM-dd HH:mm:ss
     */
    public static String getTimeTypeOne(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
    /**
     * 获取当前设备时间：格式化样式：yyyyMMddHHmmss
     * @return 返回当前时间 类型：yyyyMMddHHmmss
     */
    public static String getTimeTypeTwo(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(date);
    }

    /**
     * 获取当前设备时间：格式化样式：yyyy-MM-dd
     * @return 返回当前时间 类型：yyyy-MM-dd
     */
    public static String getTimeTypeThree(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     * 获取网络时间并验证是否是有效时间字符串和是否是当前时间
     * @return
     */
    public static String getNetWorkTime(){
        String baiDu = "http://www.baidu.com";//百度
        String taoBao = "http://www.taobao.com";//淘宝
        String keXueYuan = "http://www.ntsc.ac.cn";//中国科学院国家授时中心
        String three60 = "http://www.360.cn";//360

        ArrayList<String> urls = new ArrayList<>();
        urls.add(baiDu);
        urls.add(taoBao);
        urls.add(keXueYuan);
        urls.add(three60);
        String thisDate = null;
        for(int i=0;i<urls.size();i++){
            String s = getWebsiteDatetime(urls.get(i));
            if (s==null) return null;
            if (verifyDateLegal(s)){
                if (stampToDate(s).length()>12){
                    return  s;
                }
            }
        }
        return null;
    }

    /**
     * @author Carol
     * @date 2020年7月14日18:11:16
     * 验证字符串是否为合法日期 支持2019-03-12 2019/03/12 2019.03.12   HH:mm:ss HH:mm常用格式
     * @param date
     * @return
     */
    public static boolean verifyDateLegal(String date) {
        if ((date.contains("-") && date.contains("/"))
                || (date.contains("-") && date.contains("."))
                || (date.contains("/") && date.contains("."))){
            return false;
        }
        date.trim();
        StringBuilder timeSb = new StringBuilder();
        date = date.replaceAll("[\\.]|[//]", "-");
        String[] time = date.split(" ");
        timeSb.append(time[0]);
        timeSb.append(" ");
        if (time.length > 1) {
            timeSb.append(time[1]);
        }
        int i = time.length > 1 ? time[1].length() : 0;
        for ( ; i < 8 ; i ++) {
            if (i == 2 || i == 5){
                timeSb.append(":");
            } else {
                timeSb.append("0");
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            sdf.setLenient(false);
            sdf.parse(timeSb.toString());
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * 获取指定网站的日期时间
     *
     * @param webUrl
     * @return
     */
    private static String getWebsiteDatetime(String webUrl){
        try {
            URL url = new URL(webUrl);// 取得资源对象
            URLConnection uc = url.openConnection();// 生成连接对象
            uc.connect();// 发出连接
            long ld = uc.getDate();// 读取网站日期时间
            Date date = new Date(ld);// 转换为标准时间对象
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);// 输出北京时间
            return sdf.format(date);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
