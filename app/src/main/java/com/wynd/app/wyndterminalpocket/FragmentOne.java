package com.wynd.app.wyndterminalpocket;


import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class FragmentOne extends Fragment {


    private View rootView;
    public FragmentOne() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_fragment_one, container, false);

        ImageView mImageView = (ImageView)rootView.findViewById(R.id.one);
        int screenDensity = getResources().getDisplayMetrics().densityDpi;
        int widthInDp = ConvertPixelsToDp(getResources().getDisplayMetrics().widthPixels);
        int heightInDp =  ConvertPixelsToDp(getResources().getDisplayMetrics().heightPixels);

        System.out.println("screendensity "+screenDensity + " wh "+widthInDp +" "+heightInDp);

        mImageView.setImageBitmap(
                Globales.decodeSampledBitmapFromResource(getResources(), R.drawable.slideone, widthInDp, heightInDp));


        return rootView;
    }
    private int ConvertPixelsToDp(float pixelValue)
    {
        int dp = (int) ((pixelValue)/getResources().getDisplayMetrics().density);
        return dp;
    }

}