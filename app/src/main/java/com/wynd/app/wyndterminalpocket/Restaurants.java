package com.wynd.app.wyndterminalpocket;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Restaurants extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private OnFragmentInteractionListener mListener;
    private String userID, parentID, permission, rest_channel, EntityInfo, restID, myuserID;
    private JSONArray chains = new JSONArray();
    List<String> name, email, phone, channel;
    private RecyclerView recList;
    private RestaurantAdapter ra;
    private View rootView;
    private List<RestaurantInfo> resto;
    private LinearLayout vExpandable;
    private RelativeLayout vHeader;
    private JSONArray infosArray = new JSONArray();

    public Restaurants() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static Restaurants newInstance(String param1, String param2) {
        Restaurants fragment = new Restaurants();
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

        pref = getContext().getSharedPreferences("Infos", 0);

        userID = pref.getString("userID", "");
        EntityInfo = pref.getString("EntityInfo", "");
        permission = pref.getString("ROLE", "");
        //rest_channel = pref.getString("rest_channel", "");
        myuserID = pref.getString("myuserID", "");

        System.out.println("params! userid :" + userID + " parentid: " + EntityInfo + " roles: " + permission);

        //user can see the only attached restaurants, and if is ADMIN or SUPER_ADMIN: add a restaurant and add a new user with USER ROLES etc...

        try{

            //display only restaurants which I allow to see
            infosArray = new JSONArray(EntityInfo);
            JSONObject infoObject;

            for (int j = 0; j < infosArray.length(); j++) {
                infoObject = infosArray.getJSONObject(j);
                String restID= infoObject.isNull("resaturantChainID") ? "" : infoObject.getString("resaturantChainID");
                //get all restaurants
                JsonObjectRequest restaurantRequest = new JsonObjectRequest
                        (Request.Method.GET, Globales.baseUrl+"api/restaurant/get/by/id/"+restID, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    response = response.getJSONObject("data");
                                    System.out.println("response "+response);

                                        //check if the restaurant id is the same on with i have permissions to see
                                        //display only restaurants which I allow to see
                                        chains.put(response);

                                    System.out.println("rest " + chains);

                                    ra = new RestaurantAdapter(createList(chains));
                                    recList.setAdapter(ra);



                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {

                                error.printStackTrace();
                            }
                        }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String>  params = new HashMap<String, String>();

                        System.out.println("api infos sent" + Globales.API_USER + " "+Globales.API_HASH);
                        params.put("Api-User", Globales.API_USER);
                        params.put("Api-Hash", Globales.API_HASH);

                        return params;
                    }
                };

                Volley.newRequestQueue(getContext()).add(restaurantRequest);
            }
        }catch (JSONException e){

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        rootView =  inflater.inflate(R.layout.fragment_restaurants, container, false);
        setHasOptionsMenu(true);
        recList = (RecyclerView) rootView.findViewById(R.id.cardList);
        recList.setHasFixedSize(true);

        resto = new ArrayList<>();
        ra = new RestaurantAdapter(resto);
        recList.setAdapter(ra);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        try{
            infosArray = new JSONArray(EntityInfo);
            for (int j = 0; j < infosArray.length(); j++) {
                JSONObject infoObject = infosArray.getJSONObject(j);
                permission = infoObject.isNull("permissionID") ? "" : infoObject.getString("permissionID");
                restID = infoObject.isNull("resaturantChainID") ? "" : infoObject.getString("resaturantChainID");
                System.out.println("rest et role "+permission  +" "+restID);

                if(permission.equals("5")){
                    fab.setVisibility(View.VISIBLE);
                }
            }

        }catch(JSONException e){

        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Ajouter un nouveau restaurant", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
    private List<RestaurantInfo> createList(JSONArray jsonArray) {

        List<RestaurantInfo> result = new ArrayList<RestaurantInfo>();

        try{
            for (int i = 0; i < jsonArray.length(); i++) {
                RestaurantInfo ri = new RestaurantInfo();
                JSONObject json_data = jsonArray.getJSONObject(i);

                ri.id = (json_data.isNull("id") ? "" : RestaurantInfo.ID_PREFIX +  json_data.getString("id"));
                ri.name = (json_data.isNull("name") ? "" : RestaurantInfo.NAME_PREFIX +  json_data.getString("name"));
                ri.email = (json_data.isNull("email") ? "" : RestaurantInfo.EMAIL_PREFIX +  json_data.getString("email"));
                ri.phone = (json_data.isNull("phone") ? "" : RestaurantInfo.PHONE_PREFIX +  json_data.getString("phone"));
                ri.channel = (json_data.isNull("channel") ? "" : RestaurantInfo.CHANNEL_PREFIX +  json_data.getString("channel"));

                try{
                    infosArray = new JSONArray(EntityInfo);
                    for (int j = 0; j < infosArray.length(); j++) {
                        JSONObject infoObject = infosArray.getJSONObject(j);
                        permission = infoObject.isNull("permissionID") ? "" : infoObject.getString("permissionID");
                        restID = infoObject.isNull("resaturantChainID") ? "" : infoObject.getString("resaturantChainID");
                        System.out.println("rest et role "+permission  +" "+restID);

                        if(!ri.id.isEmpty() && ri.id.equals(restID)){
                            ri.userPermission = permission;
                        }
                    }

                }catch(JSONException e){

                }



                result.add(ri);
            }

        }catch (JSONException e){
            System.out.println("Erreur json "+e);
        }

        return result;
    }
}
