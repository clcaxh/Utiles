package com.pksh.myutils.server.connect;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.pksh.myutils.ordinary.FileFormat;
import com.pksh.myutils.ordinary.StringFormat;
import com.pksh.myutils.server.UploadServer;
import com.pksh.myutils.server.modles.FileCallBack;
import com.pksh.myutils.server.modles.MySqlServerData;
import com.pksh.myutils.server.modles.ServerUploadFileExcuteType;
import com.pksh.myutils.server.modles.UploadData;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.IllegalFormatCodePointException;
import java.util.Map;
import java.util.Set;

/**
 * 上传表单
 */
public class UploadForm extends Thread implements UploadServer {
    private static final String TAG = "服务器调用(UploadData)";

    // 这里个boundary（边界）为了避免出现以为添加了"--"，所以没有加"--"字符串！！
    private final static String boundary = "WebKitFormBoundaryVFAV6iACjwkGHYYK";
    private final static String LINE = "\r\n";

    private final static String PREFIX = "--";
    private final static String LAST = "--";
    public final static int FILE_UPLOAD_PERSENT = 0x30;//返回文件上传进度
    public final static int FILE_UPLOAD_EXCUTION = 0x31;//返回执行情况
    public final static int SERVER_ERROR = 0x32;//返回执行情况
    public final static int MYSQL_ERROR = 0x33;//返回执行情况
    public final static int DATA_OK = 0x34;//返回执行情况
    public final static int DATA_ERROR = 0x35;//返回执行情况
    public final static int CHECK_DATA_ERROR = 0x36;//数据检查不通过
    public final static int SERVER_EXECUTING = 0x37;//检查都没有问题，文件也已经上传，服务器执行中，可以退出等待

    String uploadPath = "";
    String message = "";
    UploadData uploadData;
    ServerUploadFileExcuteType excuteType;
    Handler handler;
    /**
     * @param uploadData
     */
    public UploadForm(@NonNull UploadData uploadData){
        uploadPath = getUploadPath(uploadData.address,uploadData.part,uploadData.api);
        this.uploadData = uploadData;
        this.excuteType = uploadData.excuteType;
    }

    @Override
    public void run() {
        if (checkUploadData()) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(uploadPath).openConnection();
                conn.setRequestMethod("POST");
                // 设置不进行缓存
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setChunkedStreamingMode(128 * 1024);// 128K，这个配置可以提高性能
                //下面设置http请求头
                conn.setRequestProperty("Accept", "ext/plain, */*");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                //根据选择的上传类型，执行不同的逻辑
                if (uploadData.isUploadFile) {
                    getFields(dos);
                    getFileFields(dos);

                } else {
                    getFields(dos);
                }

                // 结尾
                dos.write((PREFIX + boundary + LAST + LINE).getBytes());
                dos.flush();
                Log.d(TAG, "run: 开始连接服务器");

                serverAnalysis(serverCallBack(conn));//解析信息返回调用类消息

            } catch (IOException e) {
                setCallMessage(FILE_UPLOAD_EXCUTION,DATA_ERROR,uploadData.messageNum,"服务器执行异常："+e.getMessage());
                Log.d(TAG, "服务器执行异常："+e.getMessage());
                e.printStackTrace();
            }

        } else {
            setCallMessage(FILE_UPLOAD_EXCUTION,CHECK_DATA_ERROR,uploadData.messageNum,message);
            Log.d(TAG, "run: 数据检查未通过："+message);
        }
    }

    /**
     * 检查表单数据完整性
     * @return
     */
    private boolean checkUploadData(){
        if (uploadData.handler != null) this.handler = uploadData.handler;

        if (StringFormat.isNull(uploadData.address)) {
            message = "服务器地址不可为空！";
            return false;
        }
        if (StringFormat.isNull(uploadData.part)) {
            message = "服务器端口不可为空！";
            return false;
        }
        if (StringFormat.isNull(uploadData.api)) {
            message = "服务器API不可为空！";
            return false;
        }
        if (uploadData.formData.size() < 1) {
            message = "上传表单不可为空！";
            return false;
        }

        if (!checkForm()) {
            return false;
        }

        switch (excuteType) {
            case insert:

                break;
            case update:
                if (!uploadData.formData.containsKey("mysql_where")){
                    message = "数据库更新操作必须上传where条件";
                    return false;
                }
                break;
            case delete:
                if (!uploadData.formData.containsKey("mysql_where")){
                    message = "数据库删除操作必须上传where条件";
                    return false;
                }
                break;
            case select:
                if (!checkSelect()) {
                    message = "查找数据操作必须上传查找的字段";
                    return false;
                }
                break;
            case noDataSql:
                if (!uploadData.formData.containsKey("noDataSql")){
                    message = "执行无数据返回的命令必须上传命令";
                    return false;
                }
                break;
            case dataSql:
                if (!uploadData.formData.containsKey("dataSql")){
                    message = "执行有数据返回的命令必须上传命令";
                    return false;
                }
                break;
        }

        //如果上传文件,检查上传文件的是否存在，以及文件大小是否超过要求
        if (uploadData.isUploadFile && uploadData.files.size() > 0) {
            for (Map.Entry<String, String> entry : uploadData.files.entrySet()) {
                if (FileFormat.checkFileIsExist(entry.getValue())){
                    long fileLength = FileFormat.getFileLength(entry.getValue());
                    if (fileLength > 1024*1024*6) {
                        message = "文件："+entry.getValue()+"大小"+FileFormat.formatFileSize(fileLength)+"过大，无法上传，最大6M";
                        return false;
                    }
                } else {
                    message = "文件："+entry.getValue()+"不存在";
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检查数据库select命令是否上传字段信息
     * @return
     */
    private boolean checkSelect(){
        int s = 3;
        if (uploadData.formData.containsKey("mysql_where")) s = s+1;
        if (uploadData.formData.containsKey("mysql_order")) s = s+1;
        if (uploadData.formData.containsKey("user")) s = s+1;
        if (uploadData.formData.size() <= s) return false;
        return true;
    }

    /**
     * 检查数据库操作必传字段
     * @return
     */
    private boolean checkForm(){
        if (uploadData.excuteType == null){
            message = "uploadData bean 中的excuteType不可为空";
            return false;
        } else {
            uploadData.formData.put("excute_type",uploadData.excuteType.toString());
        }
        if (!uploadData.formData.containsKey("api_type")){
            message = "上传表单必须包含服务器执行类型";
            return false;
        }
        if (!uploadData.formData.containsKey("excute_type")){
            message = "上传表单必须包含数据库执行类型";
            return false;
        }
        if (!uploadData.formData.containsKey("table_name")){
            message = "上传表单必须包含执行数据库表名";
            return false;
        }
        return true;
    }




    //服务器返回信息封装
    private FileCallBack serverCallBack(HttpURLConnection connection) throws IOException {
        FileCallBack callBack = new FileCallBack();
        MySqlServerData sqlServerData = new MySqlServerData();
        //封装返回值
        callBack.setStatusCode(connection.getResponseCode());
        callBack.setStatusValue(connection.getResponseMessage());
        Log.d(TAG, "serverCallBack服务器返回码: "+connection.getResponseCode());

        StringBuilder sb=new StringBuilder();
        String readLine;
        BufferedReader responseReader=new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        while((readLine=responseReader.readLine())!=null){
            sb.append(readLine);
            Log.d(TAG, sb.toString());
        }
        Log.d(TAG,"serverCallBack服务器返回信息长度："+sb.length());
        callBack.setEntity(sb.toString());
        JSONObject accessTokenJsonObject = JSONObject.parseObject(sb.toString());

        if (accessTokenJsonObject != null){
            Log.d(TAG, "serverCallBack返回sql错误编码信息: "+accessTokenJsonObject.get("code"));
            String code = accessTokenJsonObject.getString("code");
            sqlServerData.setCode(Integer.parseInt(code));
            sqlServerData.setSqlData((accessTokenJsonObject.getString("sqlData")));
            sqlServerData.setMessage((accessTokenJsonObject.getString("message")));
            sqlServerData.setErrorMessage( (accessTokenJsonObject.getString("errorMessage")));
            sqlServerData.setSQLState((accessTokenJsonObject.getString("SQLState")));
            callBack.setServerData(sqlServerData);
            responseReader.close();
            Log.d(TAG, "服务器-getStatusCode"+callBack.getStatusCode());
            Log.d(TAG, "服务器-getStatusValue"+callBack.getStatusValue());
            Log.d(TAG, "服务器-getEntity"+callBack.getEntity());
            Log.d(TAG, "服务器-getTableName"+callBack.getTableName() );
            Log.d(TAG, "数据库-getCode"+callBack.getServerData().getCode());
            Log.d(TAG, "数据库-getMessage"+callBack.getServerData().getMessage());
            Log.d(TAG, "数据库-getSqlDataNum"+callBack.getServerData().getSqlDataNum());
            Log.d(TAG, "数据库-getErrorMessage"+callBack.getServerData().getErrorMessage());
            Log.d(TAG, "数据库-getSqlData"+callBack.getServerData().getSqlData());
        }else{
            Log.d(TAG, "serverCallBack: 服务器-未返回消息");
        }
        return callBack;
    }

    //解析，返回给调用类消息
    private void serverAnalysis(FileCallBack callBack){
        // 成功响应
        if (callBack != null){
            if (callBack.getStatusCode() == 200){
                if (callBack.getServerData().getCode() == 0){
                    setCallMessage(FILE_UPLOAD_EXCUTION,DATA_OK,uploadData.messageNum,callBack);
                    Log.d(TAG, "serverAnalysis:服务器正常，数据库正常");
                    return;
                }
                setCallMessage(FILE_UPLOAD_EXCUTION,MYSQL_ERROR,uploadData.messageNum,callBack);
                Log.d(TAG, "serverAnalysis:服务器返回信息正常，数据库执行错误 ");
            }else{
                setCallMessage(FILE_UPLOAD_EXCUTION,SERVER_ERROR,uploadData.messageNum,callBack);
                Log.d(TAG, "serverAnalysis:服务器错误");
            }
        }
    }

    /**
     * 添加普通字段
     */
    private void getFields(DataOutputStream dos) throws IOException {
        Log.d(TAG, "setFields:开始处理表单 ");
        // 处理普通表单字段
        StringBuilder builder = new StringBuilder();
        if (uploadData.formData != null) {
            Set<String> keys = uploadData.formData.keySet();
            for (String k : keys) {
                builder.append(PREFIX).append(boundary).append(LINE);
                builder.append("Content-Disposition: form-data; name=\"").append(k).append("\"").append(LINE).append(LINE);
                builder.append(uploadData.formData.get(k)).append(LINE);
            }
            // 把数据写到输出流中
            dos.write(builder.toString().getBytes("utf-8"));
        }
        Log.d(TAG, "setFields:表单处理完毕 ");
    }


    /**
     * 添加文件
     */
    private void getFileFields(DataOutputStream dos) throws IOException {
        Log.d(TAG, "setFiles: 开始处理文件");
        // 处理文件
        int result = 1;//标识当前处理的文件编号
        if (uploadData.files != null && uploadData.files.size() > 0) {
            for (String f : uploadData.files.keySet()) {
                File file = new File(uploadData.files.get(f));

                dos.write((PREFIX + boundary + LINE).getBytes());
                String fileName = f;
                dos.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"").getBytes());
                dos.write((LINE + LINE).getBytes());

                // 使用NIO的channel来处理字节流
                FileInputStream fis = new FileInputStream(file);
                FileChannel channel = fis.getChannel();
                long current = 0;
                long fileSize = channel.size();
                ByteBuffer buffer = ByteBuffer.allocate(4096);
                int len = channel.read(buffer);
                while (len != -1) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        dos.write(buffer.get());
                        // 这个用来记录当前文件的上传进度
                        current++;
                    }

                    // 计算文件上传进度
                    int percent = (int) ((current * 1.0f / fileSize) * 100);
                    // 返回文件上传进度
                    setCallMessage(FILE_UPLOAD_PERSENT,result,percent,f);

                    buffer.clear();
                    len = channel.read(buffer);
                }
                dos.write(LINE.getBytes());
                result++;
            }
        }
        setCallMessage(FILE_UPLOAD_EXCUTION,SERVER_EXECUTING,uploadData.messageNum,"文件上传完毕，服务器正在执行，可以点击确定退出");
        Log.d(TAG, "setFiles: 文件处理完毕");
    }


    /**
     * 返回调用类消息
     * @param arg1
     * @param arg2
     * @param obj
     */
    private void setCallMessage(int what,int arg1,int arg2,Object obj){
        if (handler != null) {
            Message msg = null;
            msg.what = what;//代表返回消息类型
            msg.arg1 = arg1;//代表返回消息类型
            msg.arg2 = arg2;//代表返回消息的代号
            msg.obj = obj;//返回的消息内容
            handler.sendMessage(msg);
        }
    }
}
