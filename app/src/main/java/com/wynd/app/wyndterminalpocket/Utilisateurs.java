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


public class Utilisateurs extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private Spinner restSpinner, parentSpinner;
    private View rootView;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    String userID, parentID, permission, rest_channel, restID, myuserID;
    private JSONArray chains = new JSONArray();
    private JSONArray users = null;
    private String selectedID, EntityInfo, role;
    private ArrayAdapter<String> dataAdapter;
    private RecyclerView recList;
    private UserAdapter ra;
    private List<UserInfo> user;
    private FloatingActionButton fab;
    private JSONArray infosArray = new JSONArray();
    private JSONArray parents = new JSONArray();
    private TextView empty;

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
        myuserID =  pref.getString("myuserID", "");

        EntityInfo = pref.getString("EntityInfo", "");

        System.out.println("params! userid :" + userID + " parentid: " + parentID + " roles: " + permission);

        try{
            infosArray = new JSONArray(EntityInfo);
            JSONObject infoObject;

            for (int j = 0; j < infosArray.length(); j++) {
                infoObject = infosArray.getJSONObject(j);
                final String parentID= infoObject.isNull("res_parent_id") ? "" : infoObject.getString("res_parent_id");

                System.out.println("parentID "+parentID);

                //show parents which I am allow to see
                JsonObjectRequest parentRequest = new JsonObjectRequest
                        (Request.Method.GET, Globales.baseUrl+"api/restaurant/get/all/parents/user/"+myuserID, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    JSONArray values = response.getJSONArray("data");

                                    for(int i=0; i<values.length(); i++){
                                        parents.put(values.getJSONObject(i));
                                    }

                                    System.out.println("parents " + parents);
                                    addParent(parents);
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
         rootView = inflater.inflate(R.layout.fragment_utilisateurs, container, false);

        //add them to spinner
        restSpinner = (Spinner) rootView.findViewById(R.id.rest_channel_id);
        parentSpinner = (Spinner) rootView.findViewById(R.id.parent);
        recList = (RecyclerView) rootView.findViewById(R.id.cardList);
        empty = (TextView) rootView.findViewById(R.id.empty);
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
    private void addList(final JSONArray jsonArray){

        List<String> list = new ArrayList<String>();

        list.add(0, "Select a restaurant");
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String name = jsonArray.getJSONObject(i).getString("name");
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
                if(arg2 == 0){
                    recList.setVisibility(View.GONE);
                }else{
                    recList.setVisibility(View.VISIBLE);
                }
                if (item != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            String name = jsonArray.getJSONObject(i).getString("name");
                            if(item.equals(name)){
                                System.out.println("name ok "+item);
                                selectedID = jsonArray.getJSONObject(i).getString("id");
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

                String id = (json_data.isNull("id") ? "" : UserInfo.ID_PREFIX + json_data.getString("id"));
                if (!id.isEmpty() && !id.equalsIgnoreCase(myuserID)) {
                    ui.id = (json_data.isNull("id") ? "" : UserInfo.ID_PREFIX + json_data.getString("id"));
                    ui.username = (json_data.isNull("username") ? "" : UserInfo.USERNAME_PREFIX + json_data.getString("username"));
                    ui.email = (json_data.isNull("email") ? "" : UserInfo.EMAIL_PREFIX + json_data.getString("email"));
                    ui.phone = (json_data.isNull("phone") ? "" : UserInfo.PHONE_PREFIX + json_data.getString("phone"));

                    ui.permission = (json_data.isNull("permission") ? "" : json_data.getString("permission"));

                    result.add(ui);
                }
            }

        }catch (JSONException e){
            System.out.println("Erreur json "+e);
        }

        return result;
    }
    private void addParent(final JSONArray jsonArray){

        List<String> list = new ArrayList<String>();

        list.add(0, "Select franchise");
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
                System.out.println("item "+item + " position "+arg2);
                if(arg2 == 0){
                    recList.setVisibility(View.GONE);
                    restSpinner.setVisibility(View.GONE);
                }else{
                    recList.setVisibility(View.VISIBLE);
                    restSpinner.setVisibility(View.VISIBLE);
                }
                if (item != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            String name = jsonArray.getJSONObject(i).getString("parent_name");
                            if(item.equals(name)){
                                final String selectedID = jsonArray.getJSONObject(i).getString("id");
                                //autocompleter la liste des restaurants après avoir sélctionner le parent

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
                                                    addList(entities);

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


}
