package com.protocol;

/**
 * 登陆请求指令
 */
public class LoginRequestPacket extends Packet {

    private Integer userId;
    private String userName;
    private String password;

    /**
     * 指令
     * @return
     */
    @Override
    public Byte getCommand() {
        return Commamd.LOGIN_REQUEST;
    }

    @Override
    public Byte getVersion() {
        return Commamd.LOGIN_REQUEST;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
