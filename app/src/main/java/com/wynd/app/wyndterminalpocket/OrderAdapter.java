package com.wynd.app.wyndterminalpocket;

/**
 * Created by cgutu on 03/03/16.
 */

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<OrderInfo> orderList;
    public Context mContext, mActivity;
    private FragmentManager FragManager;
    private String RestID;
    private Activity context;

    public OrderAdapter(List<OrderInfo> orderList) {
        this.orderList = orderList;
    }
    public OrderAdapter(Context appContext, List <UserInfo> userList, FragmentManager fmanager) {
        this.mContext = appContext;
        this.orderList = orderList;
        this.FragManager = fmanager;

    }


    @Override
    public int getItemCount() {
        return orderList.size();
    }


    @Override
    public void onBindViewHolder(OrderAdapter.OrderViewHolder orderViewHolder, int i)  {
        final OrderInfo ri = orderList.get(i);
        orderViewHolder.vRef.setText(ri.order_reference);
        orderViewHolder.vStatus.setText(ri.order_status);
        //orderViewHolder.vDeliveryTime.setText(ri.order_desired_delivery);
        orderViewHolder.vTerminal.setText(ri.reporting_terminal_id);

        if(ri.order_status.equals("Accepted")){
            orderViewHolder.vStatus.setTextColor(Color.GREEN);
            orderViewHolder.vCircle.setBackgroundResource(R.drawable.circle_shape_accepted);
        }else{
            orderViewHolder.vStatus.setTextColor(Color.RED);
            orderViewHolder.vCircle.setBackgroundResource(R.drawable.circle_shape_refused);
        }


    }

    @Override
    public OrderAdapter.OrderViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_order, viewGroup, false);

        final OrderViewHolder orderViewHolder = new OrderViewHolder(itemView);

        return orderViewHolder;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        protected TextView vRef;
        protected TextView vStatus;
        //protected TextView vDeliveryTime;
        protected CardView vCard;
        protected ImageView vCircle;
        protected TextView vTerminal;

        public OrderViewHolder(View v) {
            super(v);
            vRef = (TextView) v.findViewById(R.id.order_ref);
            vStatus = (TextView) v.findViewById(R.id.order_status);
            //vDeliveryTime= (TextView) v.findViewById(R.id.delivery_timestamp);
            vCard= (CardView) v.findViewById(R.id.card_view);
            vCircle = (ImageView) v.findViewById(R.id.circleShape);
            vTerminal = (TextView) v.findViewById(R.id.terminal);
        }
    }


}

