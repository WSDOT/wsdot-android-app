package gov.wa.wsdot.android.wsdot.ui.trafficmap.besttimestotravel;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.TravelChartItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link TravelChartItem}
 */
public class MyTravelChartRecyclerViewAdapter extends RecyclerView.Adapter<MyTravelChartRecyclerViewAdapter.ViewHolder> {

    private List<TravelChartItem> mValues;

    public MyTravelChartRecyclerViewAdapter(List<TravelChartItem> items) {
        mValues = items;
    }

    public void setData(List<TravelChartItem> data){
        mValues = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_resizeable_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        TravelChartItem item = mValues.get(position);

        holder.mItem = item;
        holder.chart.setImageDrawable(item.getImage());
        holder.chart.setContentDescription(item.getAltText());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView chart;
        private TravelChartItem mItem;

        public ViewHolder(View view) {
            super(view);
            chart = (ImageView) view.findViewById(R.id.image);
        }
    }
}
