package com.wynd.app.wyndterminalpocket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
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
import java.util.List;
import java.util.Map;


public class ProfilFragment extends Fragment {

    private String userID, parentID;
    private View rootView;

    private TextView userName, Email, Phone, Permission, Restaurant;
    private String username, email, phone, permission, restaurant, restaurantID, EntityInfo;
    private JSONObject restaurantObject = new JSONObject();
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private ListView mListView;
    private List<String>  listItems = new ArrayList<String>();

    private OnFragmentInteractionListener mListener;

    public static ProfilFragment newInstance(String param1, String param2) {
        ProfilFragment fragment = new ProfilFragment();
        return fragment;
    }
    public ProfilFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        pref = getContext().getSharedPreferences("Infos", 0);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(getActivity(),
                ProfilFragment.class));

        userID = pref.getString("myuserID", "");
        EntityInfo = pref.getString("EntityInfo", "");

            JsonObjectRequest jsonRequest = new JsonObjectRequest
                    (Request.Method.GET, Globales.baseUrl+"api/user/get/info/"+userID, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                                try {
                                    response = response.getJSONObject("data");
                                    System.out.println("response "+response);

                                    username = response.isNull("username") ? "" : response.getString("username");
                                    email = response.isNull("email") ? "" : response.getString("email");
                                    phone = response.isNull("phone") ? "" : response.getString("phone");

                                    userName.setText(username);
                                    Email.setText(email);
                                    Phone.setText(phone);

                                    JSONArray userResto = response.getJSONArray("usersInResto");
                                    for(int i=0; i<userResto.length(); i++){
                                        JSONObject userRestInfo = userResto.getJSONObject(i);
                                        final String permissionID = userRestInfo.isNull("permissionID") ? "" : userRestInfo.getString("permissionID");
                                        restaurantID = userRestInfo.isNull("resaturantChainID") ? "" : userRestInfo.getString("resaturantChainID");

                                          JsonObjectRequest restaurantRequest = new JsonObjectRequest
                                                  (Request.Method.GET, Globales.baseUrl+"api/restaurant/get/by/id/"+restaurantID, null, new Response.Listener<JSONObject>() {
                                                        @Override
                                                        public void onResponse(JSONObject response) {

                                                            try {
                                                                response = response.getJSONObject("data");
                                                                System.out.println("permission before"+permissionID);
                                                                if(!permissionID.isEmpty() && permissionID.equals("1")){
                                                                    permission = "USER";
                                                                }else if(!permissionID.isEmpty() && permissionID.equals("2")){
                                                                    permission = "ADMIN";
                                                                }else if(!permissionID.isEmpty() && permissionID.equals("3")){
                                                                    permission = "SUPER_ADMIN";
                                                                }
                                                                restaurant = response.isNull("name") ? "" : response.getString("name");
                                                                listItems.add(permission + " <----------> "+restaurant);
                                                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                                                                        android.R.layout.simple_list_item_1, listItems);
                                                                mListView.setAdapter(adapter);
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

            Volley.newRequestQueue(getContext()).add(jsonRequest);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        rootView = inflater.inflate(R.layout.fragment_profil, container, false);

        userName = (TextView) rootView.findViewById(R.id.username);
        Email = (TextView) rootView.findViewById(R.id.email);
        Phone = (TextView) rootView.findViewById(R.id.phone);
        mListView = (ListView) rootView.findViewById(R.id.listView);


        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), EditMyProfil.class);
                i.putExtra("userID",userID);
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
