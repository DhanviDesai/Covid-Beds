package com.example.covidbeds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {
    private Retrofit retrofit;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private String[] tab_names;
    private Document responseDoc;
    private Toolbar toolbar;
    private TextView statusUpdate, disclaimer;
    private ProgressBar mainProgressBar;
    private static final int INTERNET_PERMISSION = 1111;
    private static final int FINE_PERMISSION = 2222;
    private boolean isLocationGiven = false;
    private boolean isInternetPermissionGiven = false;
    private ViewPagerAdapter adapter;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private CovidBeds governmentFragment;
    private CovidBeds privateFragment;
    private Elements government_hospital_trs,government_medical_college_trs,private_hospital_trs,private_medical_college_trs;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.viewPager);
        statusUpdate = findViewById(R.id.statusUpdate);
        disclaimer = findViewById(R.id.disclaimer);
        mainProgressBar = findViewById(R.id.mainProgressBar);

        setSupportActionBar(toolbar);

        requestPermissionsApp();

        isInternetPermissionGiven = ContextCompat.checkSelfPermission(this,Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
        isLocationGiven = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if(!isInternetPermissionGiven){
            new AlertDialog.Builder(this)
                    .setTitle("Internet Permission")
                    .setMessage("Please let the app use your internet, it needs it to access the website")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.INTERNET},INTERNET_PERMISSION);
        }
        if(!isLocationGiven){
            new AlertDialog.Builder(this)
                    .setTitle("Location Permission")
                    .setMessage("Please let the app access your location. This is needed fot the app to filter facilities based on your location")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},FINE_PERMISSION);
        }


        getGpsEnabled();

        if(isNetworkAvailable() && isLocationGiven && isInternetPermissionGiven){
            laterWork();
        }else{
            new AlertDialog.Builder(this)
                    .setTitle("Internet Connectivity")
                    .setMessage("The app needs internet connection to connect to website and display data. Please come back when you have internet")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .show();
        }


    }

    public void getGpsEnabled(){
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {
            Log.i("error",ex.toString());
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {
            Log.i("error",ex.toString());
        }

        if(!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(this)
                    .setMessage("GPS must be on to get your location")
                    .setPositiveButton("Open", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel",null)
                    .show();
        }

    }

    public void requestPermissionsApp(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION);
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},FINE_PERMISSION);
        }
    }

    public void laterWork(){
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .build();

        retrofit = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .baseUrl("https://apps.bbmpgov.in/covidbedstatus/")
                .client(okHttpClient).build();

        tab_names = getResources().getStringArray(R.array.tab_names);

        getResponseDoc();

        disclaimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://apps.bbmpgov.in/covidbedstatus/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public void setLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},FINE_PERMISSION);
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location == null){
                            return;
                        }
                        privateFragment = new CovidBeds(MainActivity.this,location,private_hospital_trs,private_medical_college_trs,executorService);
                        governmentFragment = new CovidBeds(MainActivity.this,location,government_hospital_trs,government_medical_college_trs,executorService);
                        mainProgressBar.setVisibility(View.GONE);
                        tabLayout.setVisibility(View.VISIBLE);
                        statusUpdate.setVisibility(View.VISIBLE);
                        disclaimer.setVisibility(View.VISIBLE);
                        adapter = new ViewPagerAdapter(MainActivity.this, tab_names.length,governmentFragment,privateFragment);
                        viewPager.setAdapter(adapter);
                        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
                            @Override
                            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                                TextView tv = (TextView) LayoutInflater.from(MainActivity.this).inflate(R.layout.tab_text, null, false);
                                tab.setCustomView(tv);
                                tab.setText(tab_names[position]);
                            }
                        }).attach();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        int position = adapter.getPosition();
        if(position == 0){
            super.onBackPressed();
        }else{
            TabLayout.Tab tab = tabLayout.getTabAt(0);
            tab.select();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == INTERNET_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isInternetPermissionGiven = true;
            }else{
                Toast.makeText(this, "Please give internet permission to access the website", Toast.LENGTH_SHORT).show();
                requestPermissionsApp();
            }
        }
        if(requestCode == FINE_PERMISSION){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                isLocationGiven = true;
            }else{
                Toast.makeText(this, "Please give Location permission to filter based on your location", Toast.LENGTH_SHORT).show();
                requestPermissionsApp();
            }
        }

    }

    public void getResponseDoc(){
        final ApiService apiService = retrofit.create(ApiService.class);
        Call<String> stringCall = apiService.getStringResponse();
        stringCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    String responseString = response.body();
                    responseDoc = Jsoup.parse(responseString);
                    Element section = responseDoc.select("section").first();
                    Element h2 = section.selectFirst("h2");
                    Element government_hospital = responseDoc.select("#governmenthospital").first();
                    Element government_medical_college = responseDoc.select("#government_medical_college").first();
                    Element government_hospital_tbody = government_hospital.select("tbody").first();
                    Element government_medical_college_tbody = government_medical_college.select("tbody").first();
                    government_hospital_trs = government_hospital_tbody.select("tr");
                    government_medical_college_trs = government_medical_college_tbody.select("tr");
                    Element private_hospital = responseDoc.select("#private_hospital").first();
                    Element private_medical_college = responseDoc.select("#private_medical_college").first();
                    Element private_hospital_tbody = private_hospital.select("tbody").first();
                    Element private_medical_college_tbody = private_medical_college.select("tbody").first();
                    private_hospital_trs = private_hospital_tbody.select("tr");
                    private_medical_college_trs = private_medical_college_tbody.select("tr");
                    setLocation();
                    statusUpdate.setText(h2.text());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_file,menu);
        MenuItem mSearchItem = menu.findItem(R.id.search_menu);
        SearchView mSearchView = (SearchView) mSearchItem.getActionView();
        mSearchView.setQueryHint("Search Hospital");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.filterResults(s);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}