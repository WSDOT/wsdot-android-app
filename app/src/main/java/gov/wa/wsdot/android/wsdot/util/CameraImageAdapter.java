package gov.wa.wsdot.android.wsdot.util;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;

/**
 * Abstract custom adapter for items in recycler view.
 * onBindViewHolder() must be implemented in subclasses.
 * <p>
 * Extending RecyclerView adapter this adapter binds the custom ViewHolder
 * class to it's data.
 *
 * @see RecyclerView.Adapter
 */
public abstract class CameraImageAdapter extends RecyclerView.Adapter<CameraImageAdapter.CameraViewHolder> {

    protected ArrayList<CameraItem> items;
    protected ImageManager imageManager;

    public CameraImageAdapter(Context context, ArrayList<CameraItem> data) {
        this.items = data;
        imageManager = new ImageManager(context, 5 * DateUtils.MINUTE_IN_MILLIS); // Cache for 5 minutes.
    }

    @Override
    public CameraViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.list_item_resizeable_image, parent, false);
        return new CameraViewHolder(itemView);

    }

    @Override
    public int getItemCount() {
        if (items != null) {
            return items.size();
        }
        return 0;
    }

    public void setData(ArrayList<CameraItem> data) {
        this.items = data;
        notifyDataSetChanged();
    }

    public void clear() {
        if (items != null) {
            this.items.clear();
            notifyDataSetChanged();
        }
    }

    public static class CameraViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;

        public CameraViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
        }

        public void setImageTag (String tag){
            this.image.setTag(tag);
        }

        public ImageView getImage (){
            return this.image;
        }

    }
}

