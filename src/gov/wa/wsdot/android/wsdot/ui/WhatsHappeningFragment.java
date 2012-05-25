package gov.wa.wsdot.android.wsdot.ui;

import gov.wa.wsdot.android.wsdot.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class WhatsHappeningFragment extends SherlockFragment {

    private ViewGroup mRootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_whats_happening, container);
        refresh();
        return mRootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void refresh() {
        mRootView.removeAllViews();

        setupDefault();

        /*
        if (!UIUtils.isHoneycombTablet(getActivity())) {
            View separator = new View(getActivity());
            separator.setLayoutParams(
                    new ViewGroup.LayoutParams(1, ViewGroup.LayoutParams.FILL_PARENT));
            separator.setBackgroundResource(R.drawable.whats_on_separator);
            mRootView.addView(separator);

            View view = getActivity().getLayoutInflater().inflate(
                    R.layout.whats_on_stream, mRootView, false);
            view.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    AnalyticsUtils.getInstance(getActivity()).trackEvent(
                            "Home Screen Dashboard", "Click", "Realtime Stream", 0);
                    Intent intent = new Intent(getActivity(), TagStreamActivity.class);
                    startActivity(intent);
                }
            });
            mRootView.addView(view);
        }
        */
    }

    private void setupDefault() {
        getActivity().getLayoutInflater().inflate(
                R.layout.whats_happening_default, mRootView, true);
    }

}