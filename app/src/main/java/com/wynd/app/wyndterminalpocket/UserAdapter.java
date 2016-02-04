package com.wynd.app.wyndterminalpocket;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by cgutu on 22/01/16.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<UserInfo> userList;
    public Context mContext, mActivity;
    private FragmentManager FragManager;
    private String RestID;
    private Activity context;

    public UserAdapter(List<UserInfo> userList) {
        this.userList = userList;
    }
    public UserAdapter(Context appContext, List <UserInfo> userList, FragmentManager fmanager) {
        this.mContext = appContext;
        this.userList = userList;
        this.FragManager = fmanager;

    }


    @Override
    public int getItemCount() {
        return userList.size();
    }


    @Override
    public void onBindViewHolder(UserAdapter.UserViewHolder userViewHolder, int i)  {
        final UserInfo ri = userList.get(i);
     //   userViewHolder.vEmail.setText(ri.email);
        userViewHolder.vName.setText(ri.username);
//        userViewHolder.vPhone.setText(ri.phone);
//        userViewHolder.vChannel.setText(ri.rest_channel);
          userViewHolder.vPermission.setText(ri.permission);
//        userViewHolder.vId.setText(ri.id);
//
//        userViewHolder.vId.setVisibility(View.INVISIBLE);

        userViewHolder.vCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ProfilActivity.class);
                intent.putExtra("userID",ri.id);
                intent.putExtra("restId",ri.rest_channel);
                v.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_user, viewGroup, false);

        final UserViewHolder userViewHolder = new UserViewHolder(itemView);

        return userViewHolder;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        protected TextView vEmail;
        protected TextView vName;
        protected TextView vPhone;
        protected TextView vChannel;
        protected TextView vId;
        protected TextView vPermission;
        protected CardView vCardView;

        public UserViewHolder(View v) {
            super(v);
           // vEmail = (TextView) v.findViewById(R.id.txtEmail);
            vName = (TextView) v.findViewById(R.id.txtName);
//            vPhone = (TextView) v.findViewById(R.id.txtPhone);
//            vChannel = (TextView) v.findViewById(R.id.txtChannel);
//            vId = (TextView) v.findViewById(R.id.txtID);
            vPermission = (TextView) v.findViewById(R.id.txtPermission);
            vCardView= (CardView) v.findViewById(R.id.card_view);

            //v.setOnClickListener(this);


        }

//        @Override
//        public void onClick(View v) {
//            Intent intent = new Intent(v.getContext(), UsersActivity.class);
//            intent.putExtra("restId",vId.getText());
//            v.getContext().startActivity(intent);
//        }
    }


}
