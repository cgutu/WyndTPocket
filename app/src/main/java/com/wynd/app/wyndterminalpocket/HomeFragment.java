package com.wynd.app.wyndterminalpocket;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2, userID, EntityInfo, permission,restID;
    private View rootView;
    private TextView username;
    private LinearLayout restaurants, utilisateurs, historique, terminals, parents;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private JSONArray infosArray = new JSONArray();

    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(getActivity(),
                HomeFragment.class));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_home, container, false);
        username = (TextView) rootView.findViewById(R.id.user);

        restaurants = (LinearLayout) rootView.findViewById(R.id.layout1);
        utilisateurs = (LinearLayout) rootView.findViewById(R.id.layout2);
        terminals = (LinearLayout) rootView.findViewById(R.id.layout3);
        historique = (LinearLayout) rootView.findViewById(R.id.layout4);
        parents = (LinearLayout) rootView.findViewById(R.id.layout5);

        pref = getContext().getSharedPreferences("Infos", 0);
        String user = pref.getString("username", "");
        userID = pref.getString("myuserID", "");
        EntityInfo = pref.getString("EntityInfo", "");
        JSONArray array = new JSONArray();
        try{
            infosArray = new JSONArray(EntityInfo);
            for (int j = 0; j < infosArray.length(); j++) {
                JSONObject infoObject = infosArray.getJSONObject(j);
                permission = infoObject.isNull("permissionID") ? "" : infoObject.getString("permissionID");
                restID = infoObject.isNull("resaturantChainID") ? "" : infoObject.getString("resaturantChainID");
                array.put(permission);
            }
            int l = array.length();
            for(int i=0; i<l; i++){
                String value = array.getString(i);

                if(value.contains("3")){
                    utilisateurs.setVisibility(View.VISIBLE);
                    terminals.setVisibility(View.VISIBLE);
                    historique.setVisibility(View.VISIBLE);
                    parents.setVisibility(View.VISIBLE);
                }else{
                    Restaurants fragment = new Restaurants();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    Bundle args = new Bundle();
                    args.putString("userID", userID);
                    fragment.setArguments(args);
                    ft.replace(R.id.content_frame, fragment);
                    ft.commit();
                }

            }

        }catch(JSONException e){

        }
        username.setText("Bonjour " + user);

        restaurants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Restaurants fragment = new Restaurants();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Bundle args = new Bundle();
                args.putString("userID", userID);
                fragment.setArguments(args);
                ft.replace(R.id.content_frame, fragment);
                ft.commit();
            }
        });
        utilisateurs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilisateurs fragment = new Utilisateurs();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Bundle args = new Bundle();
                args.putString("userID", userID);
                fragment.setArguments(args);
                ft.replace(R.id.content_frame, fragment);
                ft.commit();
            }
        });
        terminals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TerminalsFragment fragment = new TerminalsFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Bundle args = new Bundle();
                args.putString("userID", userID);
                fragment.setArguments(args);
                ft.replace(R.id.content_frame, fragment);
                ft.commit();
            }
        });
        historique.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoriqueFragment fragment = new HistoriqueFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Bundle args = new Bundle();
                args.putString("userID", userID);
                fragment.setArguments(args);
                ft.replace(R.id.content_frame, fragment);
                ft.commit();
            }
        });
        parents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Parents fragment = new Parents();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Bundle args = new Bundle();
                args.putString("userID", userID);
                fragment.setArguments(args);
                ft.replace(R.id.content_frame, fragment);
                ft.commit();
            }
        });

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
