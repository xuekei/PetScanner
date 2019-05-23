package app.asus.petscanner;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import android.view.View;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class LocationShare extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener {

    TextView petlocation, strlat,strlong;
    Button btnSend;
    public static final int RequestPermissionCode = 1;
    protected GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationProviderClient;

    Geocoder geocoder;
    List<Address> addresses;
    String fullAddress="";
    Intent intent=getIntent();
    String phoneNum=intent.getStringExtra("PhoneNumber");
    String qr_id=intent.getStringExtra("QRData");
    String pet_id=intent.getStringExtra("PetId");
    String url="http://192.168.1.14/PetFindingSystem/Member/sharelocation.php";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_share);

        petlocation=(TextView)findViewById(R.id.petlocation);
        btnSend=(Button)findViewById(R.id.btnSend);
        strlat=(TextView)findViewById(R.id.StrLat);
        strlong=(TextView)findViewById(R.id.Strlong);
        geocoder=new Geocoder(this, Locale.getDefault());

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);



    }


    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                strlat.setText(String.valueOf(location.getLatitude()));
                                strlong.setText(String.valueOf(location.getLongitude()));

                                String lon = strlong.getText().toString();
                                double longtitude = Double.parseDouble(lon);

                                String lat = strlat.getText().toString();
                                double latitude = Double.parseDouble(lat);

                                try {

                                    addresses=geocoder.getFromLocation(latitude,longtitude,1);
                                    String address= addresses.get(0).getAddressLine(0);
                                    String area= addresses.get(0).getLocality();
                                    String city= addresses.get(0).getAdminArea();
                                    String country= addresses.get(0).getCountryName();
                                    String postalcode= addresses.get(0).getPostalCode();

                                    fullAddress= address+", "+area+", "+city+", "+country+", "+postalcode;

                                    petlocation.setText(fullAddress);



                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(LocationShare.this, new
                String[]{ACCESS_FINE_LOCATION}, RequestPermissionCode);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("LocationShare", "Connection failed: " + connectionResult.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("LocationShare", "Connection suspended");
    }

    public void fnSend(View view)
    {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response)
                    {
                    }



                },new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error){
                Log.e("ErrorListener",error.getMessage());
            }

        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("sendFn","fnSendSMS");
                params.put("location",fullAddress);
                params.put("strphoneNum",phoneNum);
                params.put("Pet_Id",pet_id);
                params.put("QR_ID",qr_id);


                return params;
            }
        };
        requestQueue.add(stringRequest);

    }

}
