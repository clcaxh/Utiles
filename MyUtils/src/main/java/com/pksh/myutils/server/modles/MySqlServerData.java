package com.pksh.myutils.server.modles;


/**
 * 服务器返回mysql执行信息
 */

public class MySqlServerData {

    private int code;
    private String sqlData;
    private String message;
    private String errorMessage;
    private String SQLState;
    private int sqlDataNum;
    private ServerUploadFileExcuteType excuteType;

    public ServerUploadFileExcuteType getExcuteType() {
        return excuteType;
    }

    public void setExcuteType(ServerUploadFileExcuteType excuteType) {
        this.excuteType = excuteType;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getSqlData() {
        return sqlData;
    }

    public void setSqlData(String sqlData) {
        this.sqlData = sqlData;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSQLState() {
        return SQLState;
    }

    public void setSQLState(String SQLState) {
        this.SQLState = SQLState;
    }

    public int getSqlDataNum() {
        return sqlDataNum;
    }

    public void setSqlDataNum(int sqlDataNum) {
        this.sqlDataNum = sqlDataNum;
    }

}
