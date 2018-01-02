package gov.wa.wsdot.android.wsdot.ui.myroute.newroute;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import gov.wa.wsdot.android.wsdot.R;

public class TrackingRouteDialogFragment extends DialogFragment implements View.OnClickListener {


    public interface TrackingRouteDialogListener {
        void onFinishTrackingDialog();
    }

    public TrackingRouteDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static TrackingRouteDialogFragment newInstance(String title) {
        TrackingRouteDialogFragment frag = new TrackingRouteDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tracking_route, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addOnClickListeners();

        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.finish_button) {
            // show confirm
            showConfirmView();
        } else if (v.getId() == R.id.confirm_finish_button) {
            TrackingRouteDialogListener listener = (TrackingRouteDialogListener) getActivity();
            listener.onFinishTrackingDialog();
            dismiss();
        } else if (v.getId() == R.id.cancel_button) {
            // hide confirm
            showTrackingView();
        }
    }

    private void showConfirmView() {
        getView().findViewById(R.id.confirm_view).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.tracking_view).setVisibility(View.GONE);
    }

    private void showTrackingView() {
        getView().findViewById(R.id.confirm_view).setVisibility(View.GONE);
        getView().findViewById(R.id.tracking_view).setVisibility(View.VISIBLE);
    }

    private void addOnClickListeners() {
        Button finishButton = getView().findViewById(R.id.finish_button);
        finishButton.setOnClickListener(this);

        Button confirmFinishButton = getView().findViewById(R.id.confirm_finish_button);
        confirmFinishButton.setOnClickListener(this);

        Button cancelButton = getView().findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(this);
    }
}
