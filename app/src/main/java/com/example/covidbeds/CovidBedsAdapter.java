package com.example.covidbeds;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class CovidBedsAdapter extends RecyclerView.Adapter<CovidBedsAdapter.CovidBedsHolder> implements Filterable {

    private Context mContext;
    private Resources resources;
    private ArrayList<CovidBedsInfo> availabeCovidBedsList;
    private ArrayList<CovidBedsInfo> availableCovidBedsListFull;

    @NonNull
    @Override
    public CovidBedsAdapter.CovidBedsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CovidBedsHolder(LayoutInflater.from(mContext).inflate(R.layout.beds_layout_file,parent,false));
    }

    public CovidBedsAdapter(Context context, ArrayList<CovidBedsInfo> availabeCovidBedsList){

        this.mContext = context;
        this.resources = context.getResources();
        this.availabeCovidBedsList = availabeCovidBedsList;
        this.availableCovidBedsListFull = new ArrayList<>(availabeCovidBedsList);

    }

    @Override
    public void onBindViewHolder(@NonNull CovidBedsAdapter.CovidBedsHolder holder, int position) {

        holder.name.setText(availabeCovidBedsList.get(position).getmFacilityName());
        holder.generalValue.setText(availabeCovidBedsList.get(position).getmGen());
        holder.generalValue.setTextColor(resources.getColor(availabeCovidBedsList.get(position).getGeneralTextColor()));
        holder.hudValue.setText(availabeCovidBedsList.get(position).getmHDU());
        holder.hudValue.setTextColor(resources.getColor(availabeCovidBedsList.get(position).getHduTextColor()));
        holder.icuValue.setText(availabeCovidBedsList.get(position).getmICU());
        holder.icuValue.setTextColor(resources.getColor(availabeCovidBedsList.get(position).getIcuTextColor()));
        holder.icuvValue.setText(availabeCovidBedsList.get(position).getmICUv());
        holder.icuvValue.setTextColor(resources.getColor(availabeCovidBedsList.get(position).getIcuvTextColor()));
        holder.totalValue.setText(availabeCovidBedsList.get(position).getmTotal());
        holder.totalValue.setTextColor(resources.getColor(availabeCovidBedsList.get(position).getTotalTextColor()));

    }

    @Override
    public int getItemCount() {
        return availabeCovidBedsList.size() ;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<CovidBedsInfo> filteredList = new ArrayList<>();
                if(constraint == null || constraint.length() == 0 || constraint.equals("")){
                    filteredList.addAll(availableCovidBedsListFull);
                }else{
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for(CovidBedsInfo c : availableCovidBedsListFull){
                        if(c.getmFacilityName().toLowerCase().contains(filterPattern)){
                            filteredList.add(c);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                availabeCovidBedsList.clear();
                availabeCovidBedsList.addAll((ArrayList) filterResults.values);
                notifyDataSetChanged();

            }
        };
    }

    class CovidBedsHolder extends RecyclerView.ViewHolder{
        private TextView name,generalValue,hudValue,icuValue,icuvValue,totalValue;

        public CovidBedsHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.facilityName);
            generalValue = itemView.findViewById(R.id.generalValue);
            hudValue = itemView.findViewById(R.id.hudValue);
            icuValue = itemView.findViewById(R.id.icuValue);
            icuvValue = itemView.findViewById(R.id.icuvValue);
            totalValue = itemView.findViewById(R.id.totalValue);
        }
    }

}
