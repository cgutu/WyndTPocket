package com.wynd.app.wyndterminalpocket;

/**
 * Created by cgutu on 28/01/16.
 */


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import java.util.List;

public class TerminalAdapter extends RecyclerView.Adapter<TerminalAdapter.TerminalViewHolder> {

    private List<TerminalInfo> terminalList;
    public Context mContext, mActivity;
    private FragmentManager FragManager;

    public TerminalAdapter(List<TerminalInfo> terminalList) {
        this.terminalList = terminalList;
    }
    public TerminalAdapter(Context appContext, List <TerminalInfo> terminalList, FragmentManager fmanager) {
        this.mContext = appContext;
        this.terminalList = terminalList;
        this.FragManager = fmanager;

    }
    public TerminalAdapter(Context appContext) {
        this.mContext = appContext;
    }


    @Override
    public int getItemCount() {
        return terminalList.size();
    }


    @Override
    public void onBindViewHolder(final TerminalAdapter.TerminalViewHolder terminalViewHolder,final int i)  {
        final TerminalInfo ti = terminalList.get(i);
        terminalViewHolder.vUuid.setText(ti.uuid);
        terminalViewHolder.vRestaurant.setText(ti.restaurant);
        terminalViewHolder.vChannel.setText(ti.channel);
        terminalViewHolder.vEmail.setText(ti.email);
        terminalViewHolder.vUser.setText(ti.terminalUser);
        terminalViewHolder.vPhone.setText(ti.phone);
        terminalViewHolder.vApk.setText(ti.apk_version);
        terminalViewHolder.vInfos.setText(ti.entity_parent + " / " + ti.entity_label + " / " + ti.entity_id);

        if(!ti.terminalStatus.isEmpty() && ti.terminalStatus.equalsIgnoreCase("1")){
            terminalViewHolder.vOn.setVisibility(View.VISIBLE);
            terminalViewHolder.vOff.setVisibility(View.GONE);
            terminalViewHolder.vTime.setVisibility(View.GONE);
        }else if(!ti.terminalStatus.isEmpty() && ti.terminalStatus.equalsIgnoreCase("0")){
            terminalViewHolder.vOn.setVisibility(View.GONE);
            terminalViewHolder.vOff.setVisibility(View.VISIBLE);

            if(!ti.terminalStatusUpdateTime.isEmpty()){
                terminalViewHolder.vTime.setVisibility(View.VISIBLE);
                terminalViewHolder.vTime.setText(ti.terminalStatusUpdateTime);
            }
        }

        terminalViewHolder.vHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Historique.class);
                intent.putExtra("terminalID", ti.id);
                intent.putExtra("terminalUuid", ti.uuid);
                intent.putExtra("channelID", ti.channel_id);
                v.getContext().startActivity(intent);
            }
        });

        terminalViewHolder.vLocalise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), TerminalPosition.class);
                intent.putExtra("terminalID", ti.id);
                intent.putExtra("terminalUuid", ti.uuid);
                intent.putExtra("channelID", ti.channel_id);
                intent.putExtra("phone", ti.phone);
                v.getContext().startActivity(intent);
            }
        });
        terminalViewHolder.getInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), InfoOfTerminal.class);
                intent.putExtra("terminalID", ti.id);
                intent.putExtra("terminalUuid", ti.uuid);
                intent.putExtra("channelID", ti.channel_id);
                v.getContext().startActivity(intent);
            }
        });

        terminalViewHolder.vExpandable.setVisibility(View.GONE);

        terminalViewHolder.vCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (terminalViewHolder.vExpandable.getVisibility() == View.GONE) {
                    terminalViewHolder.vExpandable.setVisibility(View.VISIBLE);

                    final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    terminalViewHolder.vExpandable.measure(widthSpec, heightSpec);


                    ValueAnimator animator = ValueAnimator.ofInt(0, terminalViewHolder.vExpandable.getMeasuredHeight());

                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            //Update Height
                            int value = (Integer) valueAnimator.getAnimatedValue();
                            ViewGroup.LayoutParams layoutParams = terminalViewHolder.vExpandable.getLayoutParams();
                            layoutParams.height = value;
                            terminalViewHolder.vExpandable.setLayoutParams(layoutParams);
                        }
                    });
                    animator.start();
                } else {
                    int finalHeight = terminalViewHolder.vExpandable.getHeight();


                    ValueAnimator animator = ValueAnimator.ofInt(finalHeight, 0);

                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            //Update Height
                            int value = (Integer) valueAnimator.getAnimatedValue();
                            ViewGroup.LayoutParams layoutParams = terminalViewHolder.vExpandable.getLayoutParams();
                            layoutParams.height = value;
                            terminalViewHolder.vExpandable.setLayoutParams(layoutParams);
                        }
                    });

                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            //Height=0, but it set visibility to GONE
                            terminalViewHolder.vExpandable.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    animator.start();
                }
            }
        });

        if(ti.apk_version.isEmpty()){
            terminalViewHolder.lApk.setVisibility(View.GONE);
        }
        if(ti.email.isEmpty()){
            terminalViewHolder.lEmail.setVisibility(View.GONE);
        }
        if(ti.phone.isEmpty()){
            terminalViewHolder.lPhone.setVisibility(View.GONE);
        }
        if(ti.terminalUser.isEmpty()){
            terminalViewHolder.lUser.setVisibility(View.GONE);
        }
        if(ti.entity_parent.isEmpty() && ti.entity_id.isEmpty() && ti.entity_label.isEmpty()){
            terminalViewHolder.vInfos.setVisibility(View.GONE);
        }


    }

    @Override
    public TerminalAdapter.TerminalViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_terminal, viewGroup, false);

        final TerminalViewHolder terminalViewHolder = new TerminalViewHolder(itemView);


        return terminalViewHolder;
    }

    public static class TerminalViewHolder extends RecyclerView.ViewHolder {
        protected TextView vUuid;
        protected TextView vRestaurant;
        protected TextView vChannel;
        protected Button vOn;
        protected Button vOff;
        protected TextView vTime;
        protected TextView vUser;
        protected TextView vInfos;
        protected TextView vEmail;
        protected TextView vPhone;
        protected TextView vApk;
        protected Button vLocalise;
        protected LinearLayout vExpandable;
        protected CardView vCardView;
        protected LinearLayout lApk, lEmail, lPhone, lUser;
        protected Button vHistory;
        protected ImageView getInfo;

        public TerminalViewHolder(View v) {
            super(v);
            vUuid = (TextView) v.findViewById(R.id.uuid);
            vRestaurant = (TextView) v.findViewById(R.id.restaurant);
            vChannel = (TextView) v.findViewById(R.id.txtChannel);
            vOn = (Button) v.findViewById(R.id.on);
            vOff = (Button) v.findViewById(R.id.off);
            vTime = (TextView) v.findViewById(R.id.txtTimestamp);
            vUser = (TextView) v.findViewById(R.id.txtUser);
            vInfos = (TextView) v.findViewById(R.id.txtInfos);
            vEmail = (TextView) v.findViewById(R.id.txtEmail);
            vPhone = (TextView) v.findViewById(R.id.txtPhone);
            vApk = (TextView) v.findViewById(R.id.txtApk);
            vLocalise = (Button) v.findViewById(R.id.btnLocaliser);
            vExpandable = (LinearLayout) v.findViewById(R.id.expandable);
            vCardView = (CardView) v.findViewById(R.id.card_view);
            lApk = (LinearLayout) v.findViewById(R.id.lApk);
            lEmail = (LinearLayout) v.findViewById(R.id.lEmail);
            lPhone = (LinearLayout) v.findViewById(R.id.lPhone);
            lUser = (LinearLayout) v.findViewById(R.id.lUsername);
            vHistory = (Button) v.findViewById(R.id.btnHistory);
            getInfo = (ImageView) v.findViewById(R.id.info);
        }

    }


}