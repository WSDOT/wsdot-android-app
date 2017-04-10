package gov.wa.wsdot.android.wsdot.ui.myroute;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

    private Listener mListener;

    public static RouteOptionsDialogFragment newInstance(long routeID, String[] menuOptions, int[] menuIconIds) {
        final RouteOptionsDialogFragment fragment = new RouteOptionsDialogFragment();
        final Bundle args = new Bundle();
        args.putLong(ARG_ROUTE_ID, routeID);
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
        void onOptionClicked(long routeID, int position);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        final TextView text;
        final ImageView icon;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.fragment_option_list_dialog_item, parent, false));
            text = (TextView) itemView.findViewById(R.id.text);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onOptionClicked(getArguments().getLong(ARG_ROUTE_ID), getAdapterPosition());
                        dismiss();
                    }
                }
            });

            icon = (ImageView) itemView.findViewById(R.id.icon);
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
