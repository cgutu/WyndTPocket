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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
        myuserID = pref.getString("myuserID", "");
        System.out.println("params! userid :" + userID + " parentid: " + EntityInfo + " roles: " + permission);


        try{
            infosArray = new JSONArray(EntityInfo);
            JSONObject infoObject;

            for (int j = 0; j < infosArray.length(); j++) {
                infoObject = infosArray.getJSONObject(j);
                final String parentID= infoObject.isNull("res_parent_id") ? "" : infoObject.getString("res_parent_id");

                System.out.println("parentID "+parentID);

                //show parents which I am allow to see
                JsonObjectRequest parentRequest = new JsonObjectRequest
                        (Request.Method.GET, Globales.baseUrl+"api/user/get/parent/info/"+parentID+"/user/"+myuserID, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    JSONArray values = response.getJSONArray("data");

                                    for(int i=0; i<values.length(); i++){
                                            parents.put(values.getJSONObject(i));
                                    }

                                    System.out.println("parents " + parents);
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

                        System.out.println("api infos sent" + Globales.API_USER + " "+Globales.API_HASH);
                        params.put("Api-User", Globales.API_USER);
                        params.put("Api-Hash", Globales.API_HASH);

                        return params;
                    }
                };

                Volley.newRequestQueue(getContext()).add(parentRequest);
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
        parentSpinner = (Spinner) rootView.findViewById(R.id.parent);
        spinnerLayout = (LinearLayout) rootView.findViewById(R.id.bodyspinner);

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
                JSONObject infoObject = infosArray.getJSONObject(j);
                parentID = infoObject.isNull("res_parent_id") ? "" : infoObject.getString("res_parent_id");
                permission = infoObject.isNull("permissionID") ? "" : infoObject.getString("permissionID");
                restID = infoObject.isNull("resaturantChainID") ? "" : infoObject.getString("resaturantChainID");

                //ssi permission SUPER ADMIN et plusieurs parents
                if(permission.equals("5")){

                    System.out.println("parentID " + parentID);

                    parentSpinner.setVisibility(View.VISIBLE);

                    //si super admin, afficher une dropdown pour afficher tous les entités par parent séléctionné
                    //get parents infos where parent id = user parent id and display all entity which have the same channelParentID

                }else{
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

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String name = jsonArray.getJSONObject(i).getString("resturant_name");
                if(!list.contains(name)){
                    list.add("" + name);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        System.out.println("list " + "listist.size() : " + list.size());

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
                System.out.println("item "+item);
                if (item != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            String name = jsonArray.getJSONObject(i).getString("resturant_name");
                            if(item.equals(name)){
                                final String selectedID = jsonArray.getJSONObject(i).getString("id");
                                choosedParentID = selectedID;
                                fab.setVisibility(View.VISIBLE);
                                //get entity of selected parent
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
                                                    System.out.println("entities " + entities);
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

                                        System.out.println("api infos sent" + Globales.API_USER + " " + Globales.API_HASH);
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
                ri.name = (json_data.isNull("name") ? "" : RestaurantInfo.NAME_PREFIX +  json_data.getString("name"));
                ri.email = (json_data.isNull("email") ? "" : RestaurantInfo.EMAIL_PREFIX +  json_data.getString("email"));
                ri.phone = (json_data.isNull("phone") ? "" : RestaurantInfo.PHONE_PREFIX +  json_data.getString("phone"));
                ri.channel = (json_data.isNull("channel") ? "" : RestaurantInfo.CHANNEL_PREFIX +  json_data.getString("channel"));

                String restId = (json_data.isNull("id") ? "" : RestaurantInfo.ID_PREFIX +  json_data.getString("id"));
                System.out.println("restId "+restId);
                ri.userPermission = "1";
                try{
                    infosArray = new JSONArray(EntityInfo);
                    for (int j = 0; j < infosArray.length(); j++) {
                        JSONObject infoObject = infosArray.getJSONObject(j);

                        System.out.println("infoObject "+infoObject);

                        permission = infoObject.isNull("permissionID") ? "" : infoObject.getString("permissionID");
                        restID = infoObject.isNull("resaturantChainID") ? "" : infoObject.getString("resaturantChainID");

                        if(!restId.isEmpty() && restId.equals(restID)){
                            System.out.println("rest et role ok"+infoObject);
                            ri.userPermission = permission;
                            result.add(ri);
                        }
                    }

                }catch(JSONException e){

                }




            }

        }catch (JSONException e){
            System.out.println("Erreur json "+e);
        }

        return result;
    }
}
