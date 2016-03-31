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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
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

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private OnFragmentInteractionListener mListener;
    private String userID, parentID, permission, rest_channel, EntityInfo, restID, myuserID, UserInfo, choosedParentID;
    private JSONArray chains = new JSONArray();
    List<String> name, email, phone, channel;
    private RecyclerView recList;
    private RestaurantAdapter ra;
    private View rootView;
    private List<RestaurantInfo> resto;
    private LinearLayout vExpandable;
    private RelativeLayout vHeader;
    private JSONArray infosArray = new JSONArray();
    private Spinner parentSpinner;
    private ArrayAdapter<String> dataAdapter;
    private LinearLayout spinnerLayout;
    private JSONArray parents = new JSONArray();
    private FloatingActionButton fab;
    private TextView empty;
    private Integer terminalCount;

    public Restaurants() {
        // Required empty public constructor
    }

    public static Restaurants newInstance(String param1, String param2) {
        Restaurants fragment = new Restaurants();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(getActivity(),
                Restaurants.class));
        pref = getContext().getSharedPreferences("Infos", 0);

        EntityInfo = pref.getString("EntityInfo", "");
        myuserID = pref.getString("myuserID", "");

        boolean authorized = false;
        try{
            infosArray = new JSONArray(EntityInfo);

            for (int j = 0; j < infosArray.length(); j++) {
                JSONObject info = infosArray.getJSONObject(j);
                final String parentID= info.isNull("res_parent_id") ? "" : info.getString("res_parent_id");
                if(info.getString("permissionID").equals("3")){
                    //show parents which I am allow to see
                    //If I am not allowed, permission denied
                    authorized = true;
                }


            }
        }catch (JSONException e){

        }

        if(authorized){
            JsonObjectRequest parentRequest = new JsonObjectRequest
                    (Request.Method.GET, Globales.baseUrl+"api/restaurant/get/all/parents/user/"+myuserID, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                JSONArray values = response.getJSONArray("data");
                                for(int i=0; i<values.length(); i++){
                                    parents.put(values.getJSONObject(i));
                                }
                                addList(parents);
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
                    params.put("Api-User", Globales.API_USER);
                    params.put("Api-Hash", Globales.API_HASH);

                    return params;
                }
            };

            Volley.newRequestQueue(getContext()).add(parentRequest);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView =  inflater.inflate(R.layout.fragment_restaurants, container, false);
        setHasOptionsMenu(true);
        recList = (RecyclerView) rootView.findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        parentSpinner = (Spinner) rootView.findViewById(R.id.parent);
        spinnerLayout = (LinearLayout) rootView.findViewById(R.id.bodyspinner);
        empty = (TextView) rootView.findViewById(R.id.empty);
        LinearLayout back = (LinearLayout) rootView.findViewById(R.id.lback);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor = pref.edit();
                editor.putString("Check", "0");
                editor.apply();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, new HomeFragment());
                ft.commit();
            }
        });

        resto = new ArrayList<>();
        ra = new RestaurantAdapter(resto);
        recList.setAdapter(ra);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        try{
            infosArray = new JSONArray(EntityInfo);
            for (int j = 0; j < infosArray.length(); j++) {
                JSONObject object = infosArray.getJSONObject(j);
                parentID = object.isNull("res_parent_id") ? "" : object.getString("res_parent_id");
                permission = object.isNull("permissionID") ? "" : object.getString("permissionID");
                restID = object.isNull("resaturantChainID") ? "" : object.getString("resaturantChainID");

                if(permission.equals("3")){
                    parentSpinner.setVisibility(View.VISIBLE);
                }else{
                JsonObjectRequest restaurantRequest = new JsonObjectRequest
                        (Request.Method.GET, Globales.baseUrl+"api/restaurant/get/by/id/"+restID, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    response = response.getJSONObject("data");
                                    chains.put(response);
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
                        params.put("Api-User", Globales.API_USER);
                        params.put("Api-Hash", Globales.API_HASH);

                        return params;
                    }
                };

                Volley.newRequestQueue(getContext()).add(restaurantRequest);
                }
            }


        }catch(JSONException e){

        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), AddEntity.class);
                i.putExtra("parentID", choosedParentID);
                startActivity(i);
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
    private void addList(final JSONArray jsonArray){

        List<String> list = new ArrayList<String>();

        list.add(0, "Séléctionner une franchise");
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String name = jsonArray.getJSONObject(i).getString("parent_name");
                if(!list.contains(name)){
                    list.add("" + name);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        parentSpinner.setAdapter(dataAdapter);


        parentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub

                Object item = arg0.getItemAtPosition(arg2);
                if(arg2 == 0){
                    recList.setVisibility(View.GONE);
                    fab.setVisibility(View.GONE);
                }else{
                    recList.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.VISIBLE);
                }
                if (item != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            String name = jsonArray.getJSONObject(i).getString("parent_name");
                            if(item.equals(name)){
                                final String selectedID = jsonArray.getJSONObject(i).getString("id");
                                choosedParentID = selectedID;

                                final JSONArray entities = new JSONArray();
                                JsonObjectRequest entityRequest = new JsonObjectRequest
                                        (Request.Method.GET, Globales.baseUrl + "api/restaurant/get/by/parent/"+selectedID+"/user/"+myuserID, null, new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {

                                                try {
                                                    JSONArray values = response.getJSONArray("data");
                                                    for (int i = 0; i < values.length(); i++) {
                                                        JSONObject restaurants = values.getJSONObject(i);
                                                        entities.put(restaurants);
                                                    }
                                                    ra = new RestaurantAdapter(createList(entities));
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
                                        Map<String, String> params = new HashMap<String, String>();

                                        params.put("Api-User", Globales.API_USER);
                                        params.put("Api-Hash", Globales.API_HASH);

                                        return params;
                                    }
                                };

                                Volley.newRequestQueue(getContext()).add(entityRequest);

                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });


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

                ri.email = (json_data.isNull("email") ? "" : RestaurantInfo.EMAIL_PREFIX +  json_data.getString("email"));
                ri.phone = (json_data.isNull("phone") ? "" : RestaurantInfo.PHONE_PREFIX +  json_data.getString("phone"));
                ri.channel = (json_data.isNull("channel") ? "" : RestaurantInfo.CHANNEL_PREFIX +  json_data.getString("channel"));
                ri.address = (json_data.isNull("address") ? "" : json_data.getString("address"));

                String restId = (json_data.isNull("id") ? "" : RestaurantInfo.ID_PREFIX +  json_data.getString("id"));
                ri.userPermission = "2";

                ri.status = (json_data.isNull("active") ? "" : RestaurantInfo.ID_PREFIX +  json_data.getString("active"));

                ri.name = (json_data.isNull("name") ? "" : RestaurantInfo.NAME_PREFIX +  json_data.getString("name"));
                try{
                    infosArray = new JSONArray(EntityInfo);
                    for (int j = 0; j < infosArray.length(); j++) {
                        JSONObject infoObject = infosArray.getJSONObject(j);

                        permission = infoObject.isNull("permissionID") ? "" : infoObject.getString("permissionID");
                        restID = infoObject.isNull("resaturantChainID") ? "" : infoObject.getString("resaturantChainID");

                        if(!restId.isEmpty() && restId.equals(restID)){
                            if((permission.contains("3") && ri.status.equals("0")) || (permission.contains("3") && ri.status.equals("1")) ){
                                ri.userPermission = permission;
                                result.add(ri);
                            }else if(!permission.contains("3") && ri.status.equals("1")){
                                ri.userPermission = permission;
                                result.add(ri);
                            }

                        }
                    }

                }catch(JSONException e){

                }

            }

        }catch (JSONException e){
           Log.e("Erreur json ", e.toString());
        }

        return result;
    }
}
