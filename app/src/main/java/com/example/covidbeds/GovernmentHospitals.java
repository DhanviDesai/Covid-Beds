package com.example.covidbeds;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class GovernmentHospitals extends Fragment {

    private ArrayList<CovidBedsInfo> availabeCovidBedsList;
    private Document responseDoc;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Retrofit retrofit;
    private CovidBedsAdapter adapter;
    private Location location;
    private Geocoder geocoder;


    public GovernmentHospitals(Location location){
        this.location = location;
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        availabeCovidBedsList = new ArrayList<>();

        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

        retrofit = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .baseUrl("https://apps.bbmpgov.in/covidbedstatus/")
                .client(okHttpClient).build();

        geocoder = new Geocoder(getContext(), Locale.ENGLISH);

        populateData();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.government_hospitals,container,false);
        recyclerView = v.findViewById(R.id.recyclerView);
        progressBar = v.findViewById(R.id.progressBar);
        return v;
    }

    public void populateData(){

        final ApiService apiService = retrofit.create(ApiService.class);
        Call<String> stringCall = apiService.getStringResponse();
        stringCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    String responseString = response.body();
                    responseDoc = Jsoup.parse(responseString);
                    Element government_hospital = responseDoc.select("#governmenthospital").first();
                    Element government_medical_college = responseDoc.select("#government_medical_college").first();
                    Element government_hospital_tbody = government_hospital.select("tbody").first();
                    Element government_medical_college_tbody = government_medical_college.select("tbody").first();
                    Elements government_hospital_trs = government_hospital_tbody.select("tr");
                    Elements government_medical_college_trs = government_medical_college_tbody.select("tr");
                    setData(government_hospital_trs,"Hospital");
                    setData(government_medical_college_trs,"Medical College");
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter = new CovidBedsAdapter(getContext(),availabeCovidBedsList);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(adapter);

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });


    }

    public void setData(Elements parsable,String type){
        int j=0;
        for (int i =0;i<parsable.size();i++){
            if(i<3){
                continue;
            }
            if(i == parsable.size() -1 ){
                continue;
            }
            Elements tds = parsable.get(i).select("td");
            String name = tds.get(1).text();
            int avlGeneral = Integer.parseInt(tds.get(12).text());
            int avlHDU = Integer.parseInt(tds.get(13).text());
            int avlICU = Integer.parseInt(tds.get(14).text());
            int avlICUv = Integer.parseInt(tds.get(15).text());
            int avlTotal = Integer.parseInt(tds.get(16).text());

            CovidBedsInfo avlb = new CovidBedsInfo();
            avlb.setmFacilityName(name);
            double myLatitude = location.getLatitude();
            double myLongitude = location.getLongitude();
            try {
                Log.i("name",name+" "+String.valueOf(i));
                List<Address> address = geocoder.getFromLocationName(name,1);
                if(address.size() == 0){
                    avlb.setDist(availabeCovidBedsList.get(j-1).getDist());
                    continue;
                }
                double latitude = address.get(0).getLatitude();
                double longitude = address.get(0).getLongitude();
                double dist = distance(myLatitude,myLongitude,latitude,longitude);
                avlb.setDist(dist);
            } catch (IOException e) {
                e.printStackTrace();
            }
            avlb.setmGen(avlGeneral);
            avlb.setmHDU(avlHDU);
            avlb.setmICU(avlICU);
            avlb.setmICUv(avlICUv);
            avlb.setmTotal(avlTotal);
            avlb.setType(type);
            availabeCovidBedsList.add(avlb);
            j++;
        }

        Collections.sort(availabeCovidBedsList);

    }


    public void filterData(String s){
        adapter.getFilter().filter(s);
    }
}
