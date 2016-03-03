package com.wynd.app.wyndterminalpocket;

/**
 * Created by cgutu on 03/03/16.
 */
public class OrderInfo {

    protected String id;
    protected String entity_id;
    protected String channelName;
    protected String order_reference;
    protected String order_status;
    protected String order_desired_delivery;
    protected String status_report_timestamp;

    public String getReporting_terminal_id() {
        return reporting_terminal_id;
    }

    public String getStatus_report_timestamp() {
        return status_report_timestamp;
    }

    public String getOrder_desired_delivery() {
        return order_desired_delivery;
    }

    public String getOrder_status() {
        return order_status;
    }

    public String getOrder_reference() {
        return order_reference;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getEntity_id() {
        return entity_id;
    }

    public String getId() {
        return id;
    }

    protected String reporting_terminal_id;
}
