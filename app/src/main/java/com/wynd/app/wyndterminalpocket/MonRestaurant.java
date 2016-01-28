package com.wynd.app.wyndterminalpocket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MonRestaurant.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MonRestaurant#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonRestaurant extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String userID, rest_channel, permission, parentID;
    private String name, email, phone, channel;
    private TextView Name, Email, Phone, Channel;
    private View rootView;
    private Button terminals;

    private OnFragmentInteractionListener mListener;

    public MonRestaurant() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MonRestaurant.
     */
    // TODO: Rename and change types and number of parameters
    public static MonRestaurant newInstance(String param1, String param2) {
        MonRestaurant fragment = new MonRestaurant();
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
        parentID = pref.getString("parentID", "");
        permission = pref.getString("roles", "");
        rest_channel = pref.getString("rest_channel", "");

        if(!permission.isEmpty() && permission.equals("USER")){
            //get all restaurants
            JsonObjectRequest getRestaurant = new JsonObjectRequest
                    (Request.Method.GET, Globales.baseUrl+"api/restaurant/get/by/id/"+rest_channel, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                response = response.getJSONObject("data");
                                System.out.println("response " + response);

                                Name.setText(response.isNull("name") ? "" : response.getString("name"));
                                Email.setText(response.isNull("email") ? "" : response.getString("email"));
                                Phone.setText(response.isNull("phone") ? "" : response.getString("phone"));
                                Channel.setText(response.isNull("channel") ? "" : response.getString("channel"));

                                channel = response.isNull("channel") ? "" : response.getString("channel");

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

            Volley.newRequestQueue(getContext()).add(getRestaurant);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_mon_restaurant, container, false);

        Name = (TextView) rootView.findViewById(R.id.name);
        Email = (TextView) rootView.findViewById(R.id.email);
        Phone = (TextView) rootView.findViewById(R.id.phone);
        Channel = (TextView) rootView.findViewById(R.id.channel);
        terminals = (Button) rootView.findViewById(R.id.btnTerminals);

        terminals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), Terminals.class);
                i.putExtra("channel",channel);
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
