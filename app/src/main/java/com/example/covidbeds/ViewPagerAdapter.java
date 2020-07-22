package com.example.covidbeds;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.jsoup.nodes.Document;


public class ViewPagerAdapter extends FragmentStateAdapter {

    private int totalCount;
    private Document responseDoc;
    private int position;
    private GovernmentHospitals g;
    private PrivateHospitals p;
    private Location location;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, int totalCount, Document responseDoc,Location location) {
        super(fragmentActivity);
        this.totalCount = totalCount;
        this.responseDoc = responseDoc;
        this.location = location;
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        this.position = position;
        Log.i("Check",String.valueOf(position));
        switch (position) {
            case 0:
               g = new GovernmentHospitals(location);
                return g;
            case 1:
                p = new PrivateHospitals(location);
                return p;
        }
         g = new GovernmentHospitals(location);
        return g;
    }

    @Override
    public int getItemCount() {
        return totalCount;
    }


    public void filterResults(String s){
        if(position == 0){
            g.filterData(s);
        }else{
            p.filterData(s);
        }
    }

    @Override
    public long getItemId(int position) {
        Log.i("ChechId",String.valueOf(position));
        this.position = position;
        return super.getItemId(position);
    }
}
