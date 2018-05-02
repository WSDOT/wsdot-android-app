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

package gov.wa.wsdot.android.wsdot.ui.ferries.schedules.sailings.departures;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraActivity;
import gov.wa.wsdot.android.wsdot.util.CameraImageAdapter;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class FerriesTerminalCameraFragment extends BaseFragment
        implements Injectable {

    final static String TAG = FerriesTerminalCameraFragment.class.getSimpleName();

    private static int mTerminalId;
    private View mEmptyView;
    private static View mLoadingSpinner;
    private static CameraImageAdapter mAdapter;
    private static ArrayList<CameraItem> cameraItems = new ArrayList<>();

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    FerryTerminalCameraViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getActivity().getIntent().getExtras();
        mTerminalId = args.getInt("terminalId");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_with_spinner, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FerryTerminalCameraImageAdapter(getActivity(), null);

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mLoadingSpinner = root.findViewById(R.id.loading_spinner);
        mEmptyView = root.findViewById( R.id.empty_list_view );

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FerryTerminalCameraViewModel.class);

        viewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        mLoadingSpinner.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        mLoadingSpinner.setVisibility(View.GONE);
                        break;
                    case ERROR:
                        mLoadingSpinner.setVisibility(View.GONE);
                        TextView t = (TextView) mEmptyView;
                        t.setText(R.string.no_connection);
                }
            }
        });

        viewModel.getTerminalCameras().observe(this, cameras -> {
            if (cameras != null) {
                Collections.sort(cameras, CameraItem.cameraDistanceComparator);
                cameraItems = new ArrayList<>(cameras);
                mAdapter.setData(cameraItems);
            }
        });

        viewModel.loadTerminalCameras(mTerminalId, "ferries");

        return root;
    }

    private class FerryTerminalCameraImageAdapter extends gov.wa.wsdot.android.wsdot.util.CameraImageAdapter {

        public FerryTerminalCameraImageAdapter(Context context, ArrayList<CameraItem> data) {
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
                        b.putInt("id", cameraItems.get(pos).getCameraId());
                        b.putString("advertisingTarget", "ferries");
                        intent.putExtras(b);

                        startActivity(intent);
                    }
            );
        }
    }
}
