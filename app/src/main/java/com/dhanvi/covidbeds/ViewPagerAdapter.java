package com.dhanvi.covidbeds;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;


public class ViewPagerAdapter extends FragmentStateAdapter {

    private int totalCount;
    private int position;
    private CovidBeds government;
    private CovidBeds Private;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, int totalCount,CovidBeds government, CovidBeds Private) {
        super(fragmentActivity);
        this.totalCount = totalCount;
        this.government = government;
        this.Private = Private;
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        this.position = position;
        switch (position) {
            case 0:
                return government;
            case 1:
                return Private;
        }
        return government;
    }

    @Override
    public int getItemCount() {
        return totalCount;
    }


    public void filterResults(String s){
        if(position == 0){
            government.filterData(s);
        }else{
            Private.filterData(s);
        }
    }

    @Override
    public long getItemId(int position) {
        this.position = position;
        return super.getItemId(position);
    }

    public int getPosition(){
        return this.position;
    }

}
