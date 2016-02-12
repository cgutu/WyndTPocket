package com.wynd.app.wyndterminalpocket;

/** 
 * UserInfo - get all user informations
 * @author Cornelia Gutu
 * @version 1.0
 * @see UserInfo
 */

public class UserInfo {

    protected String id;
    protected String username;
    protected String email;
    protected String phone;
    protected String permission;
    protected String rest_channel;

    protected static final String ID_PREFIX = "";
    protected static final String USERNAME_PREFIX = "";
    protected static final String EMAIL_PREFIX = "";
    protected static final String PHONE_PREFIX = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public void setRest_channel(String rest_channel) {
        this.rest_channel = rest_channel;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPermission() {
        return permission;
    }

    public String getRest_channel() {
        return rest_channel;
    }

}

