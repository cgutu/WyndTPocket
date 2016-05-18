package com.wynd.app.wyndterminalpocket;

import android.graphics.Bitmap;

/**
 * Created by cgutu on 21/01/16.
 */

public class RestaurantInfo {

    protected String id;
    protected String name;
    protected String email;
    protected String phone;
    protected String channel;
    protected String userPermission;
    protected String status;
    protected String address;
    protected String photo;
    protected Bitmap bitmap;
    protected String nbOrders;
    protected String nbTerminals;
    protected String nbUsers;


    protected static final String ID_PREFIX = "";
    protected static final String NAME_PREFIX = "";
    protected static final String EMAIL_PREFIX = "";
    protected static final String PHONE_PREFIX = "";
    protected static final String CHANNEL_PREFIX = "";
    protected static final String STATUS_PREFIX = "(inactive)";

}

