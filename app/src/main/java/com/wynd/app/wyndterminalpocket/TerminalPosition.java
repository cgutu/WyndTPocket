package com.wynd.app.wyndterminalpocket;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class TerminalPosition extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private String channel, uuid, id, channel_id, phone;
    private String LAT = "";
    private String LNG = "";
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    private final static int DELAY = 20000;
    private final Handler handler = new Handler();
    private final Timer timer = new Timer();
    private Marker marker;
    List<String> Uuid, lat, lng, utime, latlng;
    private final TimerTask task = new TimerTask() {
        private int counter = 0;

        public void run() {
            handler.post(new Runnable() {
                public void run() {
                    getMarkers();
                    // new ReadMarkers().execute();
                    //Toast.makeText(TerminalPosition.this, "GPS INFO "+LAT + " "+LNG, Toast.LENGTH_SHORT).show();
                }
            });
            if (++counter == 6) {
                timer.cancel();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal_position);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                TerminalPosition.class));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

        Intent intent = getIntent();
        uuid = intent.getStringExtra("terminalUuid");
        id = intent.getStringExtra("terminalID");
        channel = intent.getStringExtra("terminalChannel");
        channel_id = intent.getStringExtra("channelID");
        phone = intent.getStringExtra("phone");

        System.out.println("terminal info " + uuid + " id " + id + " channel " + channel);

      //  getMarkers();
        timer.schedule(task, DELAY, DELAY);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CALL);

                intent.setData(Uri.parse("tel:" + phone));
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(intent);
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        CameraUpdate center =
                CameraUpdateFactory.newLatLng(new LatLng(48.856614,
                        2.3522219000000177));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(5);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);

        //get all restaurant
        JsonObjectRequest positionRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/terminal/get/location/by/channel/"+channel_id, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray data = response.getJSONArray("data");
                            System.out.println("response " + response);

                            for(int i=0; i<data.length(); i++){
                                JSONObject result = data.getJSONObject(i);
                                String macadd = result.getString("t_name");
                                if(!macadd.isEmpty() && macadd.equals(uuid)){
                                    String Position = result.getString("t_lat_lng");
                                    JSONObject objet = new JSONObject(Position);
                                    System.out.println("array " + objet);
                                    LAT = objet.getString("lat");
                                    LNG = objet.getString("lng");
                                    System.out.println("array " + LAT + LNG);
                                }

                            }

                            Toast.makeText(TerminalPosition.this, "GPS INFO "+LAT + " "+LNG, Toast.LENGTH_SHORT).show();

                            // Add a marker and move the camera
                            Float lat = Float.parseFloat(LAT);
                            Float lng = Float.parseFloat(LNG);
                            LatLng position = new LatLng(lat, lng);
                            marker = mMap.addMarker(new MarkerOptions().position(position).title("TEST"));

                            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                            CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
                            mMap.animateCamera(zoom);


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
                params.put("Api-User", Globales.API_TERMINAL);
                params.put("Api-Hash", Globales.API_HASH);

                return params;
            }
        };

        Volley.newRequestQueue(getApplicationContext()).add(positionRequest);

    }
    private void getMarkers() {

        //get all restaurant
        JsonObjectRequest positionRequest = new JsonObjectRequest
                (Request.Method.GET, Globales.baseUrl+"api/terminal/get/location/by/channel/"+channel_id, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray data = response.getJSONArray("data");
                            System.out.println("response " + response);

                            for(int i=0; i<data.length(); i++){
                                JSONObject result = data.getJSONObject(i);
                                String macadd = result.getString("t_name");
                                if(!macadd.isEmpty() && macadd.equals(uuid)){
                                    String Position = result.getString("t_lat_lng");
                                    JSONObject objet = new JSONObject(Position);
                                    System.out.println("array " + objet);
                                    LAT = objet.getString("lat");
                                    LNG = objet.getString("lng");
                                    System.out.println("array " + LAT + LNG);
                                }

                            }

                            Toast.makeText(TerminalPosition.this, "GPS INFO "+LAT + " "+LNG, Toast.LENGTH_SHORT).show();

                            // Add a marker and move the camera
                            Float lat = Float.parseFloat(LAT);
                            Float lng = Float.parseFloat(LNG);
                            LatLng position = new LatLng(lat, lng);
                            animateMarker(marker, position, false);

                            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 16);
                            mMap.animateCamera(cameraUpdate);


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
                params.put("Api-User", Globales.API_TERMINAL);
                params.put("Api-Hash", Globales.API_HASH);

                return params;
            }
        };

        Volley.newRequestQueue(getApplicationContext()).add(positionRequest);


    }

    @Override
    public void onLocationChanged(Location location) {
//        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
//        mMap.animateCamera(cameraUpdate);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 10000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }


}
