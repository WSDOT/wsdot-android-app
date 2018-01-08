/*
 * Copyright (c) 2017 Washington State Department of Transportation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package gov.wa.wsdot.android.wsdot.ui.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.CameraImageAdapter;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class CameraListFragment extends BaseFragment implements
        LoaderManager.LoaderCallbacks<ArrayList<CameraItem>> {

    private static final String TAG = CameraListFragment.class.getSimpleName();
    private static ArrayList<CameraItem> bitmapImages;
    private View mEmptyView;

    private static int[] cameraIds;
    private static String[] cameraUrls;

    private static CameraGroupImageAdapter mAdapter;
    private static View mLoadingSpinner;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle args = getActivity().getIntent().getExtras();
        cameraIds = args.getIntArray("cameraIds");
        cameraUrls = args.getStringArray("cameraUrls");

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_with_spinner, null);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CameraGroupImageAdapter(getActivity(), null);

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mLoadingSpinner = root.findViewById(R.id.loading_spinner);
        mEmptyView = root.findViewById(R.id.empty_list_view);

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
        return new gov.wa.wsdot.android.wsdot.ui.camera.CameraListFragment.CameraImagesLoader(getActivity());
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

            int numCameras = cameraIds.length;
            for (int k = 0; k < numCameras; k++) {
                c = new CameraItem();
                c.setImageUrl(cameraUrls[k]);
                c.setCameraId(cameraIds[k]);
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
