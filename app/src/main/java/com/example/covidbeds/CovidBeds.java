package com.example.covidbeds;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class CovidBeds extends Fragment {
    private ArrayList<CovidBedsInfo> availabeCovidBedsList;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Retrofit retrofit;
    private CovidBedsAdapter adapter;
    private Location location;
    private Geocoder geocoder;
    private Context context;
    private Elements hospital_trs,medical_college_trs;
    private Executor executor;
    private boolean isCompleted = false;
    private Handler uiHandler = new Handler();


    interface CompleteCallback{
        void onComplete();
    }


    public CovidBeds(Context c, Location location, Elements hospital_trs, Elements medical_college_trs, Executor executor){
        this.availabeCovidBedsList = new ArrayList<>();
        this.context = c;
        this.location = location;
        this.hospital_trs = hospital_trs;
        this.medical_college_trs = medical_college_trs;
        geocoder = new Geocoder(c,Locale.ENGLISH);
        this.executor = executor;
        populateData(new CompleteCallback() {
            @Override
            public void onComplete() {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(progressBar == null){
                            isCompleted = true;
                        }else {
                            toggleViews();
                        }
                    }
                });
            }
        });
    }

    public void toggleViews(){
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        adapter = new CovidBedsAdapter(context, availabeCovidBedsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
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


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.government_hospitals,container,false);
        recyclerView = v.findViewById(R.id.recyclerView);
        progressBar = v.findViewById(R.id.progressBar);
        if(isCompleted){
            toggleViews();
        }
        return v;
    }

    public void populateData(final CompleteCallback callback){

        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i("Here","Started "+hospital_trs.size());
                setData(hospital_trs,"Hospital");
                setData(medical_college_trs,"Medical College");
                callback.onComplete();
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
//            double myLatitude = location.getLatitude();
//            double myLongitude = location.getLongitude();
//            try {
//                Log.i("name",name+" "+String.valueOf(i));
//                List<Address> address = geocoder.getFromLocationName(name,1);
//                if(address.size() == 0){
//                    avlb.setDist(availabeCovidBedsList.get(j-1).getDist());
//                    continue;
//                }
//                double latitude = address.get(0).getLatitude();
//                double longitude = address.get(0).getLongitude();
//                double dist = distance(myLatitude,myLongitude,latitude,longitude);
//                avlb.setDist(dist);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            avlb.setmGen(avlGeneral);
            avlb.setmHDU(avlHDU);
            avlb.setmICU(avlICU);
            avlb.setmICUv(avlICUv);
            avlb.setmTotal(avlTotal);
            avlb.setType(type);
            availabeCovidBedsList.add(avlb);
            j++;
        }

//        Collections.sort(availabeCovidBedsList);

    }


    public void filterData(String s){
        adapter.getFilter().filter(s);
    }
}
