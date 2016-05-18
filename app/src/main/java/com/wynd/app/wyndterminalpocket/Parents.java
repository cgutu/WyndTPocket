package com.wynd.app.wyndterminalpocket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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


public class Parents extends Fragment {

    private View rootView;
    private RecyclerView recList;
    private ParentsAdapter pa;
    private OnFragmentInteractionListener mListener;
    private List<ParentInfo> parent;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String myuserID,EntityInfo, parentID;
    private JSONArray infosArray = new JSONArray();
    private JSONArray entities = new JSONArray();
    private FloatingActionButton fab;

    public Parents() {
        // Required empty public constructor
    }

    public static Parents newInstance(String param1, String param2) {
        Parents fragment = new Parents();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(getActivity(),
                Parents.class));
        pref = getContext().getSharedPreferences("Infos", 0);

        myuserID = pref.getString("myuserID", "");

        /**
         * get parents informations
         */
                JsonObjectRequest entityRequest = new JsonObjectRequest
                        (Request.Method.GET, Globales.baseUrl + "api/restaurant/get/all/parents/user/"+myuserID, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    JSONArray values = response.getJSONArray("data");
                                    for(int i=0; i<values.length();i++){
                                        JSONObject parent = values.getJSONObject(i);

                                        if(!entities.toString().contains("\"id\":\""+parent.getString("id")+"\"")){
                                            entities.put(parent);
                                        }

                                    }
                                    pa = new ParentsAdapter(createList(entities));
                                    recList.setAdapter(pa);

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

                //Volley.newRequestQueue(getContext()).add(entityRequest);
        ApplicationController.getInstance().addToRequestQueue(entityRequest, "entityRequest");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_parents, container, false);

        recList = (RecyclerView) rootView.findViewById(R.id.cardList);
        recList.setHasFixedSize(true);

        parent = new ArrayList<>();
        pa = new ParentsAdapter(parent);
        recList.setAdapter(pa);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), AddParent.class);
                startActivity(i);
            }
        });

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

    private List<ParentInfo> createList(JSONArray jsonArray) {

        List<ParentInfo> result = new ArrayList<ParentInfo>();

        try{
            for (int i = 0; i < jsonArray.length(); i++) {
                ParentInfo ri = new ParentInfo();
                JSONObject json_data = jsonArray.getJSONObject(i);

                ri.id = (json_data.isNull("id") ? "" :  json_data.getString("id"));
                ri.name = (json_data.isNull("parent_label") ? "" : json_data.getString("parent_label"));
                ri.label = (json_data.isNull("parent_label") ? "" : json_data.getString("parent_label"));
                ri.address = (json_data.isNull("parent_address") ? "" :json_data.getString("parent_address"));
                ri.phone = (json_data.isNull("parent_phone") ? "" :  json_data.getString("parent_phone"));
                ri.email = (json_data.isNull("parent_email") ? "" : json_data.getString("parent_email"));
                ri.status = (json_data.isNull("parent_status") ? "" : json_data.getString("parent_status"));

                result.add(ri);
            }

        }catch (JSONException e){
            Log.e("Erreur json ", e.toString());
        }

        return result;
    }
}
