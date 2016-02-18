package com.wynd.app.wyndterminalpocket;

/**
 * Created by cgutu on 17/02/16.
 */

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class ParentsAdapter extends RecyclerView.Adapter<ParentsAdapter.ParentsViewHolder> {

    private List<ParentInfo> parentList;
    public Context mContext, mActivity;
    private FragmentManager FragManager;
    private String RestID;
    private Activity context;
    private boolean isViewExpanded = false;
    private SharedPreferences pref;
    private String permission;
    private LinearLayout mLinearLayout;


    public ParentsAdapter(List<ParentInfo> parentList) {
        this.parentList = parentList;
    }
    public ParentsAdapter(Context appContext, List <ParentInfo> parentList, FragmentManager fmanager) {
        this.mContext = appContext;
        this.parentList = parentList;
        this.FragManager = fmanager;

    }
    public ParentsAdapter(Context appContext) {
        this.mContext = appContext;
    }


    @Override
    public int getItemCount() {
        return parentList.size();
    }


    @Override
    public void onBindViewHolder(final ParentsAdapter.ParentsViewHolder restaurantViewHolder,final int i)  {
        final ParentInfo ri = parentList.get(i);
        restaurantViewHolder.vEmail.setText(ri.email);
        restaurantViewHolder.vName.setText(ri.name);
        restaurantViewHolder.vPhone.setText(ri.phone);
        restaurantViewHolder.vAddress.setText(ri.address);

    }

    @Override
    public ParentsAdapter.ParentsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_parent, viewGroup, false);

        final ParentsViewHolder parentViewHolder = new ParentsViewHolder(itemView);

        return parentViewHolder;
    }

    public static class ParentsViewHolder extends RecyclerView.ViewHolder{
        protected TextView vEmail;
        protected TextView vName;
        protected TextView vPhone;
        protected TextView vAddress;

        public ParentsViewHolder(View v) {
            super(v);
            vEmail = (TextView) v.findViewById(R.id.txtEmail);
            vName = (TextView) v.findViewById(R.id.txtName);
            vPhone = (TextView) v.findViewById(R.id.txtPhone);
            vAddress = (TextView) v.findViewById(R.id.txtAdd);
        }


    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}