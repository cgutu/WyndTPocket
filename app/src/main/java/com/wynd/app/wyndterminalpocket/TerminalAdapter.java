package com.wynd.app.wyndterminalpocket;

/**
 * Created by cgutu on 28/01/16.
 */


import android.app.FragmentManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

        public TerminalViewHolder(View v) {
            super(v);
            vUuid = (TextView) v.findViewById(R.id.uuid);
            vRestaurant = (TextView) v.findViewById(R.id.restaurant);
            vChannel = (TextView) v.findViewById(R.id.txtChannel);

        }

    }


}