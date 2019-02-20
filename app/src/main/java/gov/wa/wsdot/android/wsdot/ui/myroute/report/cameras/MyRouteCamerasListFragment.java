package gov.wa.wsdot.android.wsdot.ui.myroute.report.cameras;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.cameras.CameraEntity;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraActivity;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraViewModel;
import gov.wa.wsdot.android.wsdot.ui.myroute.MyRouteViewModel;
import gov.wa.wsdot.android.wsdot.util.CameraImageAdapter;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class MyRouteCamerasListFragment extends BaseFragment implements
        LoaderManager.LoaderCallbacks<ArrayList<CameraItem>>, Injectable {

    private static final String TAG = MyRouteCamerasListFragment.class.getSimpleName();
    private static ArrayList<CameraItem> bitmapImages;
    private View mEmptyView;

    private long mRouteId = -1;

    private static MyRouteCamerasListFragment.CameraGroupImageAdapter mAdapter;
    private static View mLoadingSpinner;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    private CameraViewModel cameraViewModel;
    private MyRouteViewModel myRouteViewModel;

    private static ArrayList<CameraEntity> cameras = new ArrayList<>();

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

        mLoadingSpinner = root.findViewById(R.id.progress_bar);
        mEmptyView = root.findViewById(R.id.empty_list_view);

        myRouteViewModel = ViewModelProviders.of(this, viewModelFactory).get(MyRouteViewModel.class);
        cameraViewModel = ViewModelProviders.of(this, viewModelFactory).get(CameraViewModel.class);

        myRouteViewModel.loadMyRoute(mRouteId).observe(this, myRoute -> {
            if (myRoute != null){

                // TODO: if foundCamerasOnRoute?
                // myRoute.getCamerasIds();

                int[] cameraIds = new int[0];

                cameraViewModel.loadCamerasForIds(cameraIds).observe(this, cameras -> {




                });
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    public Loader<ArrayList<CameraItem>> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple.
        return new CameraImagesLoader(getActivity());
    }

    public void onLoadFinished(Loader<ArrayList<CameraItem>> loader, ArrayList<CameraItem> data) {
        mLoadingSpinner.setVisibility(View.GONE);

        mEmptyView.setVisibility(View.GONE);

        if (!data.isEmpty()) {
            mAdapter.setData(data);
        } else {
            TextView t = (TextView) mEmptyView;
            t.setText(R.string.no_connection);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    public void onLoaderReset(Loader<ArrayList<CameraItem>> loader) {
        mAdapter.setData(null);
    }

    /**
     * A custom Loader that loads all of the camera images.
     */
    public static class CameraImagesLoader extends AsyncTaskLoader<ArrayList<CameraItem>> {

        public CameraImagesLoader(Context context) {
            super(context);
        }

        @Override
        public ArrayList<CameraItem> loadInBackground() {
            bitmapImages = new ArrayList<>();
            CameraItem c;

            int numCameras = cameras.size();
            for (int k = 0; k < numCameras; k++) {
                c = new CameraItem();
                c.setImageUrl(cameras.get(k).getUrl());
                c.setCameraId(cameras.get(k).getCameraId());
                bitmapImages.add(c);
            }

            return bitmapImages;

        }

        /**
         * Called when there is new data to deliver to the client. The
         * super class will take care of delivering it; the implementation
         * here just adds a little more logic.
         */
        @Override
        public void deliverResult(ArrayList<CameraItem> data) {
            super.deliverResult(data);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();

            mAdapter.clear();
            mLoadingSpinner.setVisibility(View.VISIBLE);
            forceLoad();
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();

            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        @Override
        public void onCanceled(ArrayList<CameraItem> data) {
            super.onCanceled(data);
        }

        @Override
        protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();
        }
    }

    private class CameraGroupImageAdapter extends CameraImageAdapter {

        public CameraGroupImageAdapter(Context context, ArrayList<CameraItem> data) {
            super(context, data);
        }

        @Override
        public void onBindViewHolder(CameraViewHolder viewholder, int position) {

            CameraItem item = items.get(position);
            viewholder.setImageTag(item.getImageUrl());
            imageManager.displayImage(item.getImageUrl(), getActivity(), viewholder.getImage());
            final int pos = position;

            viewholder.itemView.setOnClickListener(
                    v -> {
                        Bundle b = new Bundle();
                        Intent intent = new Intent(getActivity(), CameraActivity.class);
                        b.putInt("id", bitmapImages.get(pos).getCameraId());
                        intent.putExtras(b);

                        startActivity(intent);
                    }
            );
        }
    }
}
