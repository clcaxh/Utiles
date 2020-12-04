package com.pksh.myutils.server.modles;

/**
 * 服务器返回信息model
 */
public class FileCallBack {

    private int statusCode;//服务器返回码
    private String statusValue;//服务器返回码信息
    private String entity;//服务器返回信息
    private boolean isOk;
    private String TableName;//查询表名
    private MySqlServerData serverData;//服务器返回数据，包含数据库异常信息

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusValue() {
        return statusValue;
    }

    public void setStatusValue(String statusValue) {
        this.statusValue = statusValue;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public boolean isOk() {
        return isOk;
    }

    public void setOk(boolean ok) {
        isOk = ok;
    }

    public String getTableName() {
        return TableName;
    }

    public void setTableName(String tableName) {
        TableName = tableName;
    }

    public MySqlServerData getServerData() {
        return serverData;
    }

    public void setServerData(MySqlServerData serverData) {
        this.serverData = serverData;
    }



}
