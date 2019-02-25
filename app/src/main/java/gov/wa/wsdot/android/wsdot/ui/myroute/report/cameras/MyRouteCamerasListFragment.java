package gov.wa.wsdot.android.wsdot.ui.myroute.report.cameras;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraActivity;
import gov.wa.wsdot.android.wsdot.ui.myroute.MyRouteViewModel;
import gov.wa.wsdot.android.wsdot.util.CameraImageAdapter;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class MyRouteCamerasListFragment extends BaseFragment implements Injectable {

    private static final String TAG = MyRouteCamerasListFragment.class.getSimpleName();

    private View mEmptyView;

    private long mRouteId = -1;

    private static MyRouteCamerasListFragment.CameraGroupImageAdapter mAdapter;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    private MyRouteCamerasViewModel cameraViewModel;
    private MyRouteViewModel myRouteViewModel;


    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getActivity() != null) {
            Bundle args = getActivity().getIntent().getExtras();
            if (args != null) {
                mRouteId = args.getLong("route_id");
            }
        }

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_with_progress_bar, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CameraGroupImageAdapter(getActivity(), null);

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mEmptyView = root.findViewById(R.id.empty_list_view);

        myRouteViewModel = ViewModelProviders.of(this, viewModelFactory).get(MyRouteViewModel.class);
        cameraViewModel = ViewModelProviders.of(this, viewModelFactory).get(MyRouteCamerasViewModel.class);

        myRouteViewModel.loadMyRoute(mRouteId).observe(this, myRoute -> {
            if (myRoute != null){

                if (myRoute.getFoundCameras() == 0){
                    myRouteViewModel.findCamerasOnRoute(mRouteId);
                } else {
                    try {

                        JSONArray idsJSON = new JSONArray(myRoute.getCameraIdsJSON());

                        int[] cameraIds = new int[idsJSON.length()];

                        for (int i=0; i < idsJSON.length(); i++){
                            cameraIds[i] = idsJSON.getInt(i);
                        }

                        cameraViewModel.loadCamerasForIds(cameraIds).observe(this, cameras -> {

                            if (cameras.size() > 0) {
                                mRecyclerView.setVisibility(View.VISIBLE);
                                mEmptyView.setVisibility(View.GONE);
                                mAdapter.setData(new ArrayList<>(cameras));
                            } else {
                                mRecyclerView.setVisibility(View.GONE);
                                TextView t = (TextView) mEmptyView;
                                t.setText("No cameras on route");
                                mEmptyView.setVisibility(View.VISIBLE);
                            }
                        });

                    } catch (JSONException e) {
                        mRecyclerView.setVisibility(View.GONE);
                        TextView t = (TextView) mEmptyView;
                        t.setText("error loading cameras");
                        mEmptyView.setVisibility(View.VISIBLE);

                    }
                }
            }
        });

        return root;
    }

    private class CameraGroupImageAdapter extends CameraImageAdapter {

        CameraGroupImageAdapter(Context context, ArrayList<CameraItem> data) {
            super(context, data);
        }

        @Override
        public void onBindViewHolder(CameraViewHolder viewholder, int position) {

            CameraItem item = items.get(position);
            viewholder.setImageTag(item.getImageUrl());
            imageManager.displayImage(item.getImageUrl(), getActivity(), viewholder.getImage());

            viewholder.itemView.setOnClickListener(
                    v -> {
                        Bundle b = new Bundle();
                        Intent intent = new Intent(getActivity(), CameraActivity.class);
                        b.putInt("id", item.getCameraId());
                        intent.putExtras(b);

                        startActivity(intent);
                    }
            );
        }
    }
}
