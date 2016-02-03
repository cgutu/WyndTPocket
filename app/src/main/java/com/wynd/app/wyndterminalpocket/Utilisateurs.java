package com.wynd.app.wyndterminalpocket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.List;
import java.util.Map;


public class Utilisateurs extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private Spinner restSpinner, userSpinner;
    private View rootView;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    String userID, parentID, permission, rest_channel;
    private JSONArray chains = new JSONArray();
    private JSONArray users = null;
    private String selectedID, EntityInfo, role;
    private ArrayAdapter<String> dataAdapter;
    private RecyclerView recList;
    private UserAdapter ra;
    private List<UserInfo> user;
    private FloatingActionButton fab;

    public Utilisateurs() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Utilisateurs newInstance(String param1, String param2) {
        Utilisateurs fragment = new Utilisateurs();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        pref = getContext().getSharedPreferences("Infos", 0);

        userID = pref.getString("userID", "");
        parentID = pref.getString("parentID", "");
        permission = pref.getString("roles", "");
        rest_channel = pref.getString("rest_channel", "");

        role = pref.getString("ROLE", "");

        System.out.println("params! userid :" + userID + " parentid: " + parentID + " roles: " + permission);

        if(!role.isEmpty() && role.equals("ADMIN")) {

            //get all restaurants
            JsonObjectRequest restaurantRequest = new JsonObjectRequest
                    (Request.Method.GET, Globales.baseUrl + "api/restaurant/get/all/chains", null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                JSONArray values = response.getJSONArray("data");
                                System.out.println("response " + response);

                                for (int i = 0; i < values.length(); i++) {

                                    JSONObject restaurants = values.getJSONObject(i);
                                    chains.put(restaurants);

                                }
                                System.out.println("rest " + chains);
                                addList(chains);


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

            Volley.newRequestQueue(getContext()).add(restaurantRequest);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         rootView = inflater.inflate(R.layout.fragment_utilisateurs, container, false);

        //add them to spinner
        restSpinner = (Spinner) rootView.findViewById(R.id.rest_channel_id);

        recList = (RecyclerView) rootView.findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        user = new ArrayList<>();
        ra = new UserAdapter(user);
        recList.setAdapter(ra);

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent i = new Intent(getActivity(), AddUser.class);
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
    private void addList(JSONArray jsonArray){

        List<String> list = new ArrayList<String>();

        list.add("Select a restaurant");
        for (int i = 0; i < chains.length(); i++) {
            try {
                String name = chains.getJSONObject(i).getString("name");
                list.add("" + name);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        System.out.println("list " + "listist.size() : " + list.size());

        dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        restSpinner.setAdapter(dataAdapter);


        restSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                Object item = arg0.getItemAtPosition(arg2);
                if (item != null) {
//                    Toast.makeText(getActivity(), item.toString(),
//                            Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < chains.length(); i++) {
                        try {
                            String name = chains.getJSONObject(i).getString("name");
                            if(item.equals(name)){
                                System.out.println("name ok "+item);
                                selectedID = chains.getJSONObject(i).getString("id");
                                System.out.println("item id "+selectedID);

                                users = new JSONArray();
                                //get user of selected restaurant
                                JsonObjectRequest userRequest = new JsonObjectRequest
                                        (Request.Method.GET, Globales.baseUrl + "api/restaurant/get/user/in_restaurant/"+selectedID, null, new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {

                                                try {
                                                    JSONArray values = response.getJSONArray("data");
                                                    System.out.println("response " + response);

                                                    for (int i = 0; i < values.length(); i++) {

                                                        JSONObject restaurants = values.getJSONObject(i);
                                                        users.put(restaurants);

                                                    }
                                                    System.out.println("users " + users);
                                                    ra = new UserAdapter(createList(users));
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

                                Volley.newRequestQueue(getContext()).add(userRequest);
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

    private List<UserInfo> createList(JSONArray jsonArray) {

        List<UserInfo> result = new ArrayList<UserInfo>();

        try{
            for (int i = 0; i < jsonArray.length(); i++) {
                UserInfo ui = new UserInfo();
                JSONObject json_data = jsonArray.getJSONObject(i);

                String id = (json_data.isNull("id") ? "" : UserInfo.ID_PREFIX +  json_data.getString("id"));
                    ui.id = (json_data.isNull("id") ? "" : UserInfo.ID_PREFIX +  json_data.getString("id"));
                    ui.username = (json_data.isNull("username") ? "" : UserInfo.USERNAME_PREFIX +  json_data.getString("username"));
                    ui.email = (json_data.isNull("email") ? "" : UserInfo.EMAIL_PREFIX +  json_data.getString("email"));
                    ui.phone = (json_data.isNull("phone") ? "" : UserInfo.PHONE_PREFIX +  json_data.getString("phone"));
                    ui.rest_channel = (json_data.isNull("rest_channel") ? "" : UserInfo.RESTCHANNEL_PREFIX +  json_data.getString("rest_channel"));

                    String permission = (json_data.isNull("permission") ? "" : json_data.getString("permission"));

                    if(!permission.isEmpty() && permission.equals("2")){
                        ui.permission = "ADMIN";
                    }else if(!permission.isEmpty() && permission.equals("1")){
                        ui.permission = "USER";
                    }else{
                        ui.permission = "SUPER_ADMIN";
                    }

                    result.add(ui);
                }

        }catch (JSONException e){
            System.out.println("Erreur json "+e);
        }

        return result;
    }

}
