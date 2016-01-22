package com.wynd.app.wyndterminalpocket;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.HashMap;
import java.util.Map;


public class ProfilFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String userID, parentID;
    private View rootView;

    private TextView userName, Email, Phone, Permission, Restaurant;
    private String username, email, phone, permission, restaurant, restaurantID;
    private JSONObject restaurantObject = new JSONObject();
    private SharedPreferences pref;


    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfilFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfilFragment newInstance(String param1, String param2) {
        ProfilFragment fragment = new ProfilFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public ProfilFragment() {
        // Required empty public constructor
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
        parentID = pref.getString("parentID", "");

        System.out.println("params: userid -" + userID + " parentid - " + parentID);

        //for getting the all role for example
        /* ------------------------------------------- */
//        try {
//            JSONArray values = response.getJSONArray("data");
//            System.out.println("response "+response);
//
//            for (int i = 0; i < values.length(); i++) {
//
//                JSONObject role = values.getJSONObject(i);
//
//                permission = role.getString("permission_name");
//                System.out.println("permission "+permission);
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

         /* ------------------------------------------- */

            JsonObjectRequest jsonRequest = new JsonObjectRequest
                    (Request.Method.GET, "http://5.196.44.136/wyndTapi/api/user/get/info/"+userID, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // the response is already constructed as a JSONObject!
                                try {
                                    response = response.getJSONObject("data");
                                    System.out.println("response "+response);

                                    username = response.isNull("username") ? "" : response.getString("username");
                                    email = response.isNull("email") ? "" : response.getString("email");
                                    phone = response.isNull("phone") ? "" : response.getString("phone");
                                    permission = response.isNull("permission") ? "" : response.getString("permission");
                                    restaurantID = response.isNull("rest_channel") ? "" : response.getString("rest_channel");


                                    userName.setText(username);
                                    Email.setText(email);
                                    Phone.setText(phone);
                                    Permission.setText(permission);

                                    if(!restaurantID.isEmpty()){
                                        final JsonObjectRequest restaurantRequest = new JsonObjectRequest
                                                (Request.Method.GET, "http://5.196.44.136/wyndTapi/api/restaurant/get/by/id/"+restaurantID, null, new Response.Listener<JSONObject>() {
                                                    @Override
                                                    public void onResponse(JSONObject response) {

                                                        try {
                                                            response = response.getJSONObject("data");
                                                            System.out.println("response " + response);

                                                            restaurantObject.put("id", response.isNull("id") ? "" : response.getString("id"));
                                                            restaurantObject.put("name", response.isNull("name") ? "" : response.getString("name"));
                                                            restaurantObject.put("email", response.isNull("email") ? "" : response.getString("email"));
                                                            restaurantObject.put("phone", response.isNull("phone") ? "" : response.getString("phone"));
                                                            restaurantObject.put("channel", response.isNull("channel") ? "" : response.getString("channel"));

                                                            System.out.println("restaurant object " + restaurantObject);

                                                            restaurant = response.isNull("name") ? "" : response.getString("name");
                                                            Restaurant.setText(restaurant);

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

                                                System.out.println("api infos sent" + LoginActivity.API_USER + " "+LoginActivity.API_HASH);
                                                params.put("Api-User", LoginActivity.API_USER);
                                                params.put("Api-Hash", LoginActivity.API_HASH);

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

                    System.out.println("api infos sent" + LoginActivity.API_USER + " "+LoginActivity.API_HASH);
                    params.put("Api-User", LoginActivity.API_USER);
                    params.put("Api-Hash", LoginActivity.API_HASH);

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
        Permission = (TextView) rootView.findViewById(R.id.permission);
        Restaurant = (TextView) rootView.findViewById(R.id.restaurant);

        Restaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), " "+restaurantObject.toString(), Toast.LENGTH_LONG).show();
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