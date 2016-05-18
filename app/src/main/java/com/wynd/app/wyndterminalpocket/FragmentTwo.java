package com.wynd.app.wyndterminalpocket;


import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentTwo extends Fragment {

    private View rootView;

    public FragmentTwo() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_fragment_two, container, false);
        ImageView mImageView = (ImageView)rootView.findViewById(R.id.two);
        int screenDensity = getResources().getDisplayMetrics().densityDpi;
        int widthInDp = ConvertPixelsToDp(getResources().getDisplayMetrics().widthPixels);
        int heightInDp = ConvertPixelsToDp(getResources().getDisplayMetrics().heightPixels);

        System.out.println("screendensity "+screenDensity + " wh "+widthInDp +" "+heightInDp);

        mImageView.setImageBitmap(
                Globales.decodeSampledBitmapFromResource(getResources(), R.drawable.slidetwo, widthInDp, heightInDp));
        return rootView;
    }
    private int ConvertPixelsToDp(float pixelValue)
    {
        int dp = (int) ((pixelValue)/getResources().getDisplayMetrics().density);
        return dp;
    }

}
