package com.wynd.app.wyndterminalpocket;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cgutu on 21/01/16.
 */
public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private List<RestaurantInfo> restaurantList;
    public Context mContext, mActivity;
    private FragmentManager FragManager;
    private String RestID;
    private Activity context;
    private boolean isViewExpanded = false;

    public RestaurantAdapter(List<RestaurantInfo> restaurantList) {
        this.restaurantList = restaurantList;
    }
    public RestaurantAdapter(Context appContext, List <RestaurantInfo> restaurantList, FragmentManager fmanager) {
        this.mContext = appContext;
        this.restaurantList = restaurantList;
        this.FragManager = fmanager;

    }
    public RestaurantAdapter(Context appContext) {
        this.mContext = appContext;
    }


    @Override
    public int getItemCount() {
        return restaurantList.size();
    }


    @Override
    public void onBindViewHolder(final RestaurantAdapter.RestaurantViewHolder restaurantViewHolder,final int i)  {
        final RestaurantInfo ri = restaurantList.get(i);
        restaurantViewHolder.vEmail.setText(ri.email);
        restaurantViewHolder.vName.setText(ri.name);
        restaurantViewHolder.vPhone.setText(ri.phone);
        restaurantViewHolder.vChannel.setText(ri.channel);
       // restaurantViewHolder.vId.setText(ri.id);

       // restaurantViewHolder.vId.setVisibility(View.INVISIBLE);

        restaurantViewHolder.vExpandable.setVisibility(View.GONE);

        restaurantViewHolder.vCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("test " + i);
                if (restaurantViewHolder.vExpandable.getVisibility() == View.GONE) {
                    restaurantViewHolder.vExpandable.setVisibility(View.VISIBLE);

                    final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    restaurantViewHolder.vExpandable.measure(widthSpec, heightSpec);


                    ValueAnimator animator = ValueAnimator.ofInt(0, restaurantViewHolder.vExpandable.getMeasuredHeight());

                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            //Update Height
                            int value = (Integer) valueAnimator.getAnimatedValue();
                            ViewGroup.LayoutParams layoutParams = restaurantViewHolder.vExpandable.getLayoutParams();
                            layoutParams.height = value;
                            restaurantViewHolder.vExpandable.setLayoutParams(layoutParams);
                        }
                    });
                    animator.start();
                } else {
                    int finalHeight = restaurantViewHolder.vExpandable.getHeight();


                    ValueAnimator animator = ValueAnimator.ofInt(finalHeight, 0);

                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            //Update Height
                            int value = (Integer) valueAnimator.getAnimatedValue();
                            ViewGroup.LayoutParams layoutParams = restaurantViewHolder.vExpandable.getLayoutParams();
                            layoutParams.height = value;
                            restaurantViewHolder.vExpandable.setLayoutParams(layoutParams);
                        }
                    });

                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            //Height=0, but it set visibility to GONE
                            restaurantViewHolder.vExpandable.setVisibility(View.GONE);
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

        restaurantViewHolder.vBtnUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), UsersActivity.class);
                intent.putExtra("restId",ri.id);
                System.out.println("restId "+ri.id);
                v.getContext().startActivity(intent);

            }
        });
        restaurantViewHolder.vBtnTerminals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Terminals.class);
                intent.putExtra("restId",ri.id);
                intent.putExtra("channel", ri.channel);
                v.getContext().startActivity(intent);

            }
        });

        restaurantViewHolder.vInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), InfoOfRestaurant.class);
                intent.putExtra("restId",ri.id);
                intent.putExtra("channel", ri.channel);
                v.getContext().startActivity(intent);
            }
        });


    }

    @Override
    public RestaurantAdapter.RestaurantViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_restaurant, viewGroup, false);

        final RestaurantViewHolder restaurantViewHolder = new RestaurantViewHolder(itemView);


        return restaurantViewHolder;
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        protected TextView vEmail;
        protected TextView vName;
        protected TextView vPhone;
        protected TextView vChannel;
        //protected TextView vId;
        protected Button vBtnUsers;
        protected Button vBtnTerminals;
        protected RelativeLayout vHeader;
        protected LinearLayout vExpandable;
        protected CardView vCardView;
        protected ImageView vInfo;

        public RestaurantViewHolder(View v) {
            super(v);
            vEmail = (TextView) v.findViewById(R.id.txtEmail);
            vName = (TextView) v.findViewById(R.id.txtName);
            vPhone = (TextView) v.findViewById(R.id.txtPhone);
            vChannel = (TextView) v.findViewById(R.id.txtChannel);
           // vId = (TextView) v.findViewById(R.id.txtID);
            vBtnUsers = (Button) v.findViewById(R.id.btnUsers);
            vBtnTerminals = (Button) v.findViewById(R.id.btnTerminals);

            vExpandable = (LinearLayout) v.findViewById(R.id.expandable);
            vHeader = (RelativeLayout) v.findViewById(R.id.header);
            vCardView = (CardView) v.findViewById(R.id.card_view);

            vInfo = (ImageView) v.findViewById(R.id.info);

         //   v.setOnClickListener(this);


        }

//        @Override
//        public void onClick(View v) {
//
////            Intent intent = new Intent(v.getContext(), UsersActivity.class);
////            intent.putExtra("restId",vId.getText());
////            v.getContext().startActivity(intent);
//        }
    }






}