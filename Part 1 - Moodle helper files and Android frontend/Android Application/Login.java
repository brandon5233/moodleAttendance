package com.example.brandon.attendance;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.*;

import java.util.List;

import cz.msebera.android.httpclient.Header;
import pl.droidsonroids.gif.GifImageView;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class Login extends AppCompatActivity implements LocationListener {
    static final String url = "http://192.168.1.24/moodle/";
    private TextView textView;


    // login loading gif
    private GifImageView login_loader;

    private LocationManager locationManager;
    String gpsCoordinates, networkCoordinates;
    float dist_gps, dist_network;
    final int ACCESS_FINE_LOCATION_PERMISSION_CODE = 1;
    final Location center = new Location("");
    private EditText loginText, password;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        button = findViewById(R.id.button);
        loginText = findViewById(R.id.editText);
        password = findViewById(R.id.editText2);
        textView = findViewById(R.id.textView);
        center.setLatitude(53.405714);
        center.setLongitude(-6.170532);

        // get reference to login loader view
        login_loader = (GifImageView) findViewById(R.id.login_loading);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (loginText.getText().toString().trim().equals("") || password.getText().toString().trim().equals("")) {
                    Toast.makeText(Login.this, R.string.blankinput, Toast.LENGTH_SHORT).show();
                } else {
                    if (checkMockApps(getApplicationContext())) {
                        Log.i("Mock App: ", "Detected");
                        Toast.makeText(Login.this, R.string.spooferdetected, Toast.LENGTH_SHORT).show();
                    } else {

                        hideLoginButton();
                        if (gpsPermissionCheck()) {
                            Log.i("center lat:", String.valueOf(center.getLatitude()));
                            Log.i("center lon:", String.valueOf(center.getLongitude()));
                            if (checkCoordinates()) {

                                login(loginText.getText().toString(), password.getText().toString());

                            } else {
                                Log.i("gps result", "false");
                                showLoginButton();
                            }
                        }

                    }

                }

            }
        });
        //Used for debugging and DEMO only,
        //REMOVE before final submission
        Log.i("login", loginText.getText().toString().trim());
        Log.i("Password", password.getText().toString().trim());
    }

    public void showLoginButton() {
        button.setVisibility(View.VISIBLE);
        login_loader.setVisibility(View.GONE);
    }

    public void hideLoginButton() {
        button.setVisibility(View.GONE);
        login_loader.setVisibility(View.VISIBLE);
    }


    public void login(final String username, String password) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("username", username);
        params.put("password", password);


        client.post(url + "login/index.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    showLoginButton();
                    String stringResponse = new String(responseBody, "UTF-8");
                    if (stringResponse.contains("successs")) {
                        textView.setText("login success");
                        Intent myIntent = new Intent(Login.this, MainActivity.class);
                        myIntent.putExtra("username", username);
                        //myIntent.putExtra("secondKeyName","SecondKeyValue");
                        startActivity(myIntent);

                    } else {
                        //textView.setText("no login");
                        Toast.makeText(Login.this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i("code :" + statusCode + " asynchttpfail:", error.toString());
                Toast.makeText(Login.this, "Could not connect to server", Toast.LENGTH_SHORT).show();
                showLoginButton();
            }
        });
    }


    public boolean gpsPermissionCheck() {

        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Login.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                builder.setTitle("FINE LOCATION Permission")
                        .setMessage("You need to allow FINE Location in order for attendance to be marked.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERMISSION_CODE);
                            }
                        }).show();


            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERMISSION_CODE);
            }

        } else {
            return true;
        }
        return false;
    }

    public void onRequestPermissionsResult(int code, String[] permissions, int[] grantResults) {
        switch (code) {
            case ACCESS_FINE_LOCATION_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("Location Permission", "Granted");
                    if (checkCoordinates()) {
                        Log.i("gps result", "true");
                        login(loginText.getText().toString(), password.getText().toString());
                    }

                } else {
                    Log.i("Location Permission1", "Not Granted");
                    showLoginButton();
                }
        }
    }

    public boolean checkCoordinates() {
        double latitude = 0, longitude = 0;
        boolean noProblemFlag = TRUE;
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            Log.i("IS GPS ENABLED", String.valueOf(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)));
            //If GPS_Permission is given, but phone is set to Battery_Saving mode - check if GPS is currently enabled
            if ((locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) && (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) 0, (long) 0, Login.this);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Log.i("Mock Provider GPS ", String.valueOf(location.isFromMockProvider()));
                if (location.isFromMockProvider()) noProblemFlag = FALSE;
                //try - incase a location spoofer was used before, and location returns NULL values
                try {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.i("Accuracy GPS", String.valueOf(location.getAccuracy()));
                    gpsCoordinates = String.valueOf(latitude) + " " + String.valueOf(longitude);
                    dist_gps = center.distanceTo(location);
                    Log.i("dist_gps", String.valueOf(dist_gps));
                } catch (Exception e) {
                    e.printStackTrace();
                    noProblemFlag = FALSE;
                    return false;
                }


                Log.i("IS NETWORK ENABLED", String.valueOf(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)));


                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, (long) 0, (long) 0, Login.this);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Log.i("Mock Provider Network ", String.valueOf(location.isFromMockProvider()));
                try {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    float network_accuracy = location.getAccuracy();
                    if (network_accuracy > 100) {
                        Toast.makeText(Login.this, "Network accuracy is too low, please connect to college wifi", Toast.LENGTH_SHORT).show();
                    }
                    Log.i("Accuracy Network", String.valueOf(location.getAccuracy()));
                    if (location.isFromMockProvider()) noProblemFlag = FALSE;

                    networkCoordinates = String.valueOf(latitude) + " " + String.valueOf(longitude);
                    dist_network = center.distanceTo(location);
                    Log.i("dist_network", String.valueOf(dist_network));

                } catch (Exception e) {
                    e.printStackTrace();
                    noProblemFlag = FALSE;
                    return false;

                }

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
                builder.setTitle("High Accuracy Mode required!")
                        .setMessage("Please set location to 'High Accuracy' mode")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .show();

            }
        } catch (SecurityException e) {
        }

        //determine if user is in college
        if (noProblemFlag) {
            if (Math.abs(dist_gps - dist_network) <= 100 && dist_gps <= 70) {
                //Toast.makeText(getBaseContext(),"You seem to be at HOME !", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                Toast.makeText(getBaseContext(), "You DO NOT seem to be within the campus", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            Toast.makeText(getBaseContext(), "Something Went wrong \n" +
                    "If you are using a location spoofer, please turn it off", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static boolean checkMockApps(Context context) {
        int count = 0;
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> packages =
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(applicationInfo.packageName,
                        PackageManager.GET_PERMISSIONS);

                String[] requestedPermissions = packageInfo.requestedPermissions;

                if (requestedPermissions != null) {
                    for (int i = 0; i < requestedPermissions.length; i++) {
                        if (requestedPermissions[i]
                                .equals("android.permission.ACCESS_MOCK_LOCATION")
                                && !applicationInfo.packageName.equals(context.getPackageName())) {
                            count++;
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("Exception", e.getMessage());
            }
        }

        if (count > 0)
            return true;
        return false;
    }

    /*
    Overridden methods for location Listener
     */
    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


}


