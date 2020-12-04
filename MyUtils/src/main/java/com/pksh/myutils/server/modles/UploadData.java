package com.pksh.myutils.server.modles;

import android.os.Handler;

import java.util.HashMap;
import java.util.Map;

public class UploadData {
    public Map<String, String> formData = new HashMap<>(); //上传表单数据
    public Handler handler; //返回消息
    public int messageNum; //返回消息代码
    public String address = "120.27.62.79"; //服务器地址
    public String part = "8080";//服务器端口
    public String api;//服务器API
    public boolean isUploadFile = false;//是否上传文件
    public Map<String,String> files = new HashMap<>();//上传文件列表，文件名，文件路径
    public ServerUploadFileExcuteType excuteType;
}
