package com.example.covidbeds;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

import retrofit2.Retrofit;

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
    private boolean isDataGenerated = false;


    interface CompleteCallback{
        void onComplete();
    }

    interface CompleteCallbackInt{
        void onComplete(int done);
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
                trialMethod(new CompleteCallbackInt() {
                    @Override
                    public void onComplete(int done) {
                        if(done == 4){
                            isDataGenerated = true;
                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    filterBy();
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    public void toggleViews(){
        if(progressBar == null){
            return;
        }
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        adapter = new CovidBedsAdapter(context, availabeCovidBedsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_base_layout,container,false);
        recyclerView = v.findViewById(R.id.recyclerView);
        progressBar = v.findViewById(R.id.progressBar);
        if(isDataGenerated){
            toggleViews();
        }
        return v;
    }

    public void populateData(final CompleteCallback callback){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                setData(hospital_trs,"Hospital");
                setData(medical_college_trs,"Medical College");
                callback.onComplete();
            }
        });


    }

    public void setData(Elements parsable,String type){
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
            avlb.setmGen(avlGeneral);
            avlb.setmHDU(avlHDU);
            avlb.setmICU(avlICU);
            avlb.setmICUv(avlICUv);
            avlb.setmTotal(avlTotal);
            avlb.setType(type);
            availabeCovidBedsList.add(avlb);
        }

    }


    public void filterData(String s){
        adapter.getFilter().filter(s);
    }

    public void filterBy() {
        if(isDataGenerated){
            Collections.sort(availabeCovidBedsList);
            toggleViews();
        }
    }

    private int done = 0;

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

    public void trialMethod(final CompleteCallbackInt callback){
        final int size = availabeCovidBedsList.size();
        final int shareSize = size/4;
        final int firstRange = shareSize;
        final int secondRange = shareSize*2;
        final int thirdRange = shareSize*3;
        final int fourthRange = size;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                double myLatitude = location.getLatitude();
                double myLongitude = location.getLongitude();
                int j = 0;
                for (int i=0;i<firstRange;i++){
                    CovidBedsInfo c = availabeCovidBedsList.get(i);
                    String name = c.getmFacilityName();
                    j++;
                    if(c.getDist() != 9999){
                        continue;
                    }
                    try {
                        List<Address> address = geocoder.getFromLocationName(name, 1);
                        if (address.size() == 0 && j > 0) {
                            c.setDist(availabeCovidBedsList.get(j-1).getDist());
                        }else{
                            double latitude = address.get(0).getLatitude();
                            double longitude = address.get(0).getLongitude();
                            Location location2 = new Location("");
                            location2.setLatitude(latitude);
                            location2.setLongitude(longitude);
                            c.setDist(distance(myLatitude,myLongitude,latitude,longitude));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                done++;
                callback.onComplete(done);
            }
        });
        executor.execute(new Runnable() {
            @Override
            public void run() {
                double myLatitude = location.getLatitude();
                double myLongitude = location.getLongitude();
                int j = firstRange;
                for (int i=firstRange;i<secondRange;i++){
                    CovidBedsInfo c = availabeCovidBedsList.get(i);
                    String name = c.getmFacilityName();
                    j++;
                    if(c.getDist() != 9999){
                        continue;
                    }
                    try {
                        List<Address> address = geocoder.getFromLocationName(name, 1);
                        if (address.size() == 0 && j > 0) {
                            c.setDist(availabeCovidBedsList.get(j-1).getDist());
                        }else{
                            double latitude = address.get(0).getLatitude();
                            double longitude = address.get(0).getLongitude();
                            Location location2 = new Location("");
                            location2.setLatitude(latitude);
                            location2.setLongitude(longitude);
                            c.setDist(distance(myLatitude,myLongitude,latitude,longitude));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                done++;
                callback.onComplete(done);
            }
        });

        executor.execute(new Runnable() {
            @Override
            public void run() {
                double myLatitude = location.getLatitude();
                double myLongitude = location.getLongitude();
                int j = secondRange;
                for (int i=secondRange;i<thirdRange;i++){
                    CovidBedsInfo c = availabeCovidBedsList.get(i);
                    String name = c.getmFacilityName();
                    j++;
                    if(c.getDist() != 9999){
                        continue;
                    }
                    try {
                        List<Address> address = geocoder.getFromLocationName(name, 1);
                        if (address.size() == 0 && j > 0) {
                            c.setDist(availabeCovidBedsList.get(j-1).getDist());
                        }else{
                            double latitude = address.get(0).getLatitude();
                            double longitude = address.get(0).getLongitude();
                            Location location2 = new Location("");
                            location2.setLatitude(latitude);
                            location2.setLongitude(longitude);
                            c.setDist(distance(myLatitude,myLongitude,latitude,longitude));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                done++;
                callback.onComplete(done);
            }
        });

        executor.execute(new Runnable() {
            @Override
            public void run() {
                double myLatitude = location.getLatitude();
                double myLongitude = location.getLongitude();
                int j = thirdRange;
                for (int i=thirdRange;i<fourthRange;i++){
                    CovidBedsInfo c = availabeCovidBedsList.get(i);
                    String name = c.getmFacilityName();
                    j++;
                    if(c.getDist() != 9999){
                        continue;
                    }
                    try {
                        List<Address> address = geocoder.getFromLocationName(name, 1);
                        if (address.size() == 0 && j > 0) {
                            c.setDist(availabeCovidBedsList.get(j-1).getDist());
                        }else{
                            double latitude = address.get(0).getLatitude();
                            double longitude = address.get(0).getLongitude();
                            Location location2 = new Location("");
                            location2.setLatitude(latitude);
                            location2.setLongitude(longitude);
                            c.setDist(distance(myLatitude,myLongitude,latitude,longitude));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                done++;
                callback.onComplete(done);
            }
        });
    }
}
