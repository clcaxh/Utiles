package com.pksh.myutils.server;

import com.pksh.myutils.ordinary.StringFormat;

public interface UploadServer {
    /**
     * 连接服务器
     * @param address 地址 例：120.27.62.79
     * @param port 端口 例：8080
     * @param api api (必传) 例：/ErpForOne/MySqlForZhiNengPeiBian
     * @return 连接服务器字符串
     */
    default String getUploadPath(String address,String port,String api){
        if (StringFormat.isNull(address)) address = "120.27.62.79";
        if (StringFormat.isNull(port)) port = "8080";
        return "http://"+address+":"+port+api;
    }


}
