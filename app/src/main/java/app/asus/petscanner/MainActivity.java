package app.asus.petscanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;


import javax.net.ssl.HttpsURLConnection;



public class MainActivity extends AppCompatActivity  {
    private static String Petname = "";
    private static String ownerName="";
    private static String phoneNumber="";
    private static String petId="";

    String scannedData;
    Button scanBtn;
    String strurl="http://192.168.1.14/PetFindingSystem/Member/displaydetailinapp.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        final Activity activity=this;

        scanBtn=(Button)findViewById(R.id.scan_btn);

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator=new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setBeepEnabled(false);
                integrator.setCameraId(0);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        IntentResult result=IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result!=null)
        {
            scannedData=result.getContents();
            if(scannedData !=null)
            {
                new SendRequest().execute();
            }else{}
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    public class SendRequest extends AsyncTask<String,Void,String>
    {
        protected void onPreExecute(){}
        protected String doInBackground(String... arg0)
        {
            try{
                URL url = new URL("http://192.168.1.14/PetFindingSystem/Member/displaydetailinapp.php");
                JSONObject postDataParams= new JSONObject();
                postDataParams.put("sdata",scannedData);


                Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(final String result) {

            RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
            StringRequest stringRequest= new StringRequest(Request.Method.POST, strurl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
//                        JSONArray jsonArray=new JSONArray(response);
//                        for(int i=0;i<jsonArray.length();i++) {
//                            JSONObject jsonObject=jsonArray.getJSONObject(i);
                            Petname = jsonObject.optString("petname");
                            ownerName = jsonObject.optString("ownername");
                            phoneNumber = jsonObject.optString("phonenumber");
                            petId=jsonObject.optString("petid");

//                        }

                    } catch (Exception ee) {
                        Log.e("onResponse", ee.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Log.e("Error Listener", error.getMessage());
                }
            });
            requestQueue.add(stringRequest);



            AlertDialog.Builder alertBuilder= new AlertDialog.Builder(MainActivity.this);
            String  desc ="Hi ,I am "+Petname+" . I am lost my way to back home.Please share my location to "+ownerName+" . If you worry about me , you can contact my owner via "+phoneNumber+" .";
            alertBuilder.setMessage(desc);
            alertBuilder.setPositiveButton("Share Location", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                        Intent intent=new Intent(getApplicationContext(),LocationShare.class);
                        intent.putExtra("PhoneNumber",phoneNumber);
                        intent.putExtra("QRData",scannedData);
                        intent.putExtra("PetId",petId);
                        startActivity(intent);

                }
            });

            alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            alertBuilder.show();



        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }





}


