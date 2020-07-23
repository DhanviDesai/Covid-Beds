package com.dhanvi.covidbeds;

public class CovidBedsInfo implements Comparable<CovidBedsInfo> {
    private String mFacilityName;
    private int mGen;
    private int mHDU;
    private int mICU;
    private int mICUv;
    private int mTotal;
    private String type;
    private double dist;

    public CovidBedsInfo(){
        this.dist = 9999;
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getmFacilityName() {
        return mFacilityName;
    }

    public void setmFacilityName(String mFacilityName) {
        this.mFacilityName = mFacilityName;
    }

    public String getmGen() {
        return String.valueOf(mGen);
    }

    public void setmGen(int mGen) {
        this.mGen = mGen;
    }

    public String getmHDU() {
        return String.valueOf(mHDU);
    }

    public void setmHDU(int mHDU) {
        this.mHDU = mHDU;
    }

    public String getmICU() {
        return String.valueOf(mICU);
    }

    public void setmICU(int mICU) {
        this.mICU = mICU;
    }

    public String getmICUv() {
        return String.valueOf(mICUv);
    }

    public void setmICUv(int mICUv) {
        this.mICUv = mICUv;
    }

    public String getmTotal() {
        return String.valueOf(mTotal);
    }

    public int getGeneralTextColor(){
        if(mGen > 0){
            return R.color.good;
        }else{
            return R.color.bad;
        }
    }

    public int getIcuTextColor(){
        if(mICU > 0){
            return R.color.good;
        }else{
            return R.color.bad;
        }
    }

    public int getIcuvTextColor(){
        if(mICUv > 0){
            return R.color.good;
        }else{
            return R.color.bad;
        }
    }

    public int getHduTextColor(){
        if(mHDU > 0){
            return R.color.good;
        }else{
            return R.color.bad;
        }
    }

    public int getTotalTextColor(){
        if(mTotal > 0){
            return R.color.good;
        }else{
            return R.color.bad;
        }
    }

    public void setmTotal(int mTotal) {
        this.mTotal = mTotal;
    }

    @Override
    public int compareTo(CovidBedsInfo covidBedsInfo) {
        int current = (int)(Math.ceil(this.dist));
        int incoming = (int) (Math.ceil(covidBedsInfo.getDist()));

        return current-incoming;
    }
}
