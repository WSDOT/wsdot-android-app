package gov.wa.wsdot.android.wsdot.ui.myroute;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import gov.wa.wsdot.android.wsdot.R;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     RouteOptionsDialogFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 * <p>You activity (or fragment) needs to implement {@link RouteOptionsDialogFragment.Listener}.</p>
 */
public class RouteOptionsDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_OPTIONS = "option_strings";
    private static final String ARG_OPTION_ICONS = "optino_icons";
    private static final String ARG_ROUTE_ID = "route_id";
    private static final String ARG_ROUTE_NAME = "route_name";

    private Listener mListener;

    public static RouteOptionsDialogFragment newInstance(long routeID, String routeName, String[] menuOptions, int[] menuIconIds) {
        final RouteOptionsDialogFragment fragment = new RouteOptionsDialogFragment();
        final Bundle args = new Bundle();
        args.putLong(ARG_ROUTE_ID, routeID);
        args.putString(ARG_ROUTE_NAME, routeName);
        args.putStringArray(ARG_OPTIONS, menuOptions);
        args.putIntArray(ARG_OPTION_ICONS, menuIconIds);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_option_list_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        try {
            recyclerView.setAdapter(new OptionAdapter(getArguments().getStringArray(ARG_OPTIONS).length));
        } catch (NullPointerException e){
            recyclerView.setAdapter(new OptionAdapter(0));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        final Fragment parent = getParentFragment();
        if (parent != null) {
            mListener = (Listener) parent;
        } else {
            mListener = (Listener) context;
        }
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    public interface Listener {
        void onOptionClicked(long routeID, String routeName, int position);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        final TextView text;
        final ImageView icon;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.fragment_option_list_dialog_item, parent, false));
            text = itemView.findViewById(R.id.text);
            text.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onOptionClicked(getArguments().getLong(ARG_ROUTE_ID), getArguments().getString(ARG_ROUTE_NAME), getAdapterPosition());
                    dismiss();
                }
            });
            icon = itemView.findViewById(R.id.icon);
        }
    }

    private class OptionAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final int mItemCount;

        OptionAdapter(int itemCount) {
            mItemCount = itemCount;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            try {
                holder.text.setText(getArguments().getStringArray(ARG_OPTIONS)[position]);
                holder.icon.setImageResource(getArguments().getIntArray(ARG_OPTION_ICONS)[position]);
            } catch (NullPointerException e){
                holder.text.setText("Error getting menu option");
            }
        }

        @Override
        public int getItemCount() {
            return mItemCount;
        }
    }
}
