package com.wynd.app.wyndterminalpocket;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
    private SharedPreferences pref;
    private String permission;
    private LinearLayout mLinearLayout;


    public RestaurantAdapter(List<RestaurantInfo> restaurantList) {
        this.restaurantList = restaurantList;
    }
    public RestaurantAdapter(Context appContext, List <RestaurantInfo> restaurantList) {
        this.mContext = appContext;
        this.restaurantList = restaurantList;
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
        restaurantViewHolder.vAddress.setText(ri.address);

        if(ri.status.equals("0")){
            restaurantViewHolder.vType.setVisibility(View.VISIBLE);
        }
        if(ri.phone.isEmpty()){
            restaurantViewHolder.vPhone.setVisibility(View.GONE);
        }
        if(ri.address.isEmpty()){
            restaurantViewHolder.vAddress.setVisibility(View.GONE);
        }
        if(ri.email.isEmpty()){
            restaurantViewHolder.vEmail.setVisibility(View.GONE);
        }

        permission = ri.userPermission;
        if(permission.equalsIgnoreCase("2") || permission.equalsIgnoreCase("3")){
            restaurantViewHolder.vBtnUsers.setVisibility(View.VISIBLE);
            restaurantViewHolder.vBtnOrders.setVisibility(View.VISIBLE);
        }else if(permission.equalsIgnoreCase("1")){
            restaurantViewHolder.vBtnUsers.setVisibility(View.GONE);
            restaurantViewHolder.vBtnOrders.setVisibility(View.VISIBLE);
        }


        restaurantViewHolder.vBtnUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), UsersActivity.class);
                intent.putExtra("restId", ri.id);
                v.getContext().startActivity(intent);

            }
        });
        restaurantViewHolder.vBtnTerminals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Terminals.class);
                System.out.println("terminal info "+ri.id+" "+ri.channel);
                intent.putExtra("restId",ri.id);
                intent.putExtra("channel", ri.channel);
                v.getContext().startActivity(intent);

            }
        });

        restaurantViewHolder.vInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), InfoOfRestaurant.class);
                intent.putExtra("restId", ri.id);
                intent.putExtra("channel", ri.channel);
                v.getContext().startActivity(intent);
            }
        });
        restaurantViewHolder.vBtnOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("channelID send" + ri.id);
                Intent intent = new Intent(v.getContext(), Orders.class);
                intent.putExtra("restId", ri.id);
                intent.putExtra("channel", ri.channel);
                v.getContext().startActivity(intent);
            }
        });

        new DownloadImageTask(restaurantViewHolder.vPicture)
                .execute(ri.photo);

        if(ri.email.isEmpty()){
            restaurantViewHolder.lEmail.setVisibility(View.GONE);
        }
        if(ri.phone.isEmpty()){
            restaurantViewHolder.lPhone.setVisibility(View.GONE);
        }
        System.out.println("adapter size "+ri.nbOrders + " terminals "+ri.nbTerminals);
        Resources res = mContext.getResources();
        String msgT = String.format(res.getString(R.string.terminals), ri.nbTerminals);
        restaurantViewHolder.vBtnTerminals.setText(msgT);

        String msgO = String.format(res.getString(R.string.orders), ri.nbOrders);
        restaurantViewHolder.vBtnOrders.setText(msgO);

        String msgU = String.format(res.getString(R.string.users), ri.nbUsers);
        restaurantViewHolder.vBtnUsers.setText(msgU);
    }

    @Override
    public RestaurantAdapter.RestaurantViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_restaurant, viewGroup, false);

        RestaurantViewHolder restaurantViewHolder = new RestaurantViewHolder(itemView);

        return restaurantViewHolder;
    }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{
        protected TextView vEmail;
        protected TextView vName;
        protected TextView vPhone;
        protected TextView vChannel;
        protected Button vBtnUsers;
        protected Button vBtnOrders;
        protected Button vBtnTerminals;
        protected LinearLayout vHeader;
        protected LinearLayout vExpandable;
        protected CardView vCardView;
        protected ImageView vInfo;
        protected TextView vAddress, vType;
        private boolean isViewExpanded = false;
        protected ImageView vPicture;
        protected LinearLayout lEmail, lPhone;

        public RestaurantViewHolder(View v) {
            super(v);
            vEmail = (TextView) v.findViewById(R.id.txtEmail);
            vName = (TextView) v.findViewById(R.id.txtName);
            vPhone = (TextView) v.findViewById(R.id.txtPhone);
            vChannel = (TextView) v.findViewById(R.id.txtChannel);
            vBtnUsers = (Button) v.findViewById(R.id.btnUsers);
            vBtnTerminals = (Button) v.findViewById(R.id.btnTerminals);
            vBtnOrders = (Button) v.findViewById(R.id.orders);

            vExpandable = (LinearLayout) v.findViewById(R.id.expandable);
            vHeader = (LinearLayout) v.findViewById(R.id.header);
            vCardView = (CardView) v.findViewById(R.id.card_view);

            vInfo = (ImageView) v.findViewById(R.id.info);
            vAddress = (TextView) v.findViewById(R.id.txtAdd);
            vType = (TextView) v.findViewById(R.id.txtType);

           // vExpandable.setVisibility(View.GONE);
            vPicture = (ImageView) v.findViewById(R.id.picture);
            lEmail = (LinearLayout) v.findViewById(R.id.lEmail);
            lPhone = (LinearLayout) v.findViewById(R.id.lPhone);


           // v.setOnClickListener(this);

        }

        private void expand() {
            //set Visible
            vExpandable.setVisibility(View.VISIBLE);

            final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            vExpandable.measure(widthSpec, heightSpec);

            ValueAnimator mAnimator = slideAnimator(0, vExpandable.getMeasuredHeight());
            mAnimator.start();
        }

        private void collapse() {
            int finalHeight = vExpandable.getHeight();

            ValueAnimator mAnimator = slideAnimator(finalHeight, 0);

            mAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    //Height=0, but it set visibility to GONE
                    vExpandable.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

            });
            mAnimator.start();
        }

        private ValueAnimator slideAnimator(int start, int end) {

            ValueAnimator animator = ValueAnimator.ofInt(start, end);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    //Update Height
                    int value = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = vExpandable.getLayoutParams();
                    layoutParams.height = value;
                    vExpandable.setLayoutParams(layoutParams);
                }
            });
            return animator;
        }

        @Override
        public void onClick(View v) {
            if (vExpandable.getVisibility()==View.GONE){
                isViewExpanded = true;
                expand();
            }else{
                isViewExpanded = false;
                collapse();
            }
            int position = getAdapterPosition();
            System.out.println("position " + position);
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