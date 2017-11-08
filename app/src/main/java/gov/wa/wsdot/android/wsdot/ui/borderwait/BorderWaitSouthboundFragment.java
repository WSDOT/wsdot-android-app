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

package gov.wa.wsdot.android.wsdot.ui.borderwait;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitEntity;
import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitRepository;
import gov.wa.wsdot.android.wsdot.database.borderwaits.LoadBorderWaitsCallback;
import gov.wa.wsdot.android.wsdot.database.borderwaits.LocalBorderWaitDataSource;
import gov.wa.wsdot.android.wsdot.tasks.BorderWaitsTaskReceiverFragment;
import gov.wa.wsdot.android.wsdot.tasks.DownloadBorderWaitsAsyncTask;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.UIUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class BorderWaitSouthboundFragment extends BaseFragment implements
		SwipeRefreshLayout.OnRefreshListener, BorderWaitsTaskReceiverFragment {

	private static final String TAG = BorderWaitNorthboundFragment.class.getSimpleName();

	@SuppressLint("UseSparseArrays")
	private static HashMap<Integer, Integer> routeImage = new HashMap<Integer, Integer>();

	private View mEmptyView;
	private static SwipeRefreshLayout swipeRefreshLayout;

	private static BorderWaitSouthboundFragment.BorderWaitAdapter mAdapter;
	protected RecyclerView mRecyclerView;
	protected LinearLayoutManager mLayoutManager;

	DownloadBorderWaitsAsyncTask downloadTask;

	private static BorderWaitEntity[] mBorderWaits;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		downloadTask = new DownloadBorderWaitsAsyncTask(this);
		downloadTask.execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

		mRecyclerView = root.findViewById(R.id.my_recycler_view);
		mRecyclerView.setHasFixedSize(true);
		mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mRecyclerView.setLayoutManager(mLayoutManager);

		mAdapter = new BorderWaitSouthboundFragment.BorderWaitAdapter(getActivity());
		mRecyclerView.setAdapter(mAdapter);

		mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

		// For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
		// FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
		root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_container);
		swipeRefreshLayout.setOnRefreshListener(this);
		swipeRefreshLayout.setColorSchemeResources(
				R.color.holo_blue_bright,
				R.color.holo_green_light,
				R.color.holo_orange_light,
				R.color.holo_red_light);

		mEmptyView = root.findViewById( R.id.empty_list_view );

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		routeImage.put(5, R.drawable.ic_list_i5);
		routeImage.put(9, R.drawable.ic_list_sr9);
		routeImage.put(539, R.drawable.ic_list_sr539);
		routeImage.put(543, R.drawable.ic_list_sr543);
		routeImage.put(97, R.drawable.ic_list_us97);
	}

	@Override
	public void receiveBorderWaitsDownloadResponse(String response) {

		String responseString = response;

		mEmptyView.setVisibility(View.GONE);

		if (responseString != null) {
			if (responseString.equals("OK")) {

				LoadBorderWaitsCallback callback = new LoadBorderWaitsCallback() {
					@Override
					public void onBorderWaitsLoaded(BorderWaitEntity[] borderWaits) {
						mBorderWaits = borderWaits;
						swipeRefreshLayout.setRefreshing(false);
					}

					@Override
					public void onDataNotAvailable() {
						swipeRefreshLayout.setRefreshing(false);
					}
				};

				BorderWaitRepository borderWaitRepo = new BorderWaitRepository(LocalBorderWaitDataSource.getInstance());
				borderWaitRepo.getBorderWaitsFor("southbound", callback);

			} else if (responseString.equals("NOP")) {
				swipeRefreshLayout.setRefreshing(false);
			} else {
				swipeRefreshLayout.setRefreshing(false);
				Log.e("BorderWaitAsyncTask", responseString);

				if (!UIUtils.isNetworkAvailable(this.getContext())) {
					responseString = getString(R.string.no_connection);
				}

				if (mAdapter.getItemCount() > 0) {
					Toast.makeText(this.getContext(), responseString, Toast.LENGTH_LONG).show();
				} else {
					TextView t = (TextView) mEmptyView;
					t.setText(responseString);
					mEmptyView.setVisibility(View.VISIBLE);
				}
			}
		} else {
			swipeRefreshLayout.setRefreshing(false);
		}
	}

	/**
	 * Binds the custom ViewHolder class to it's data.
	 *
	 * @see android.support.v7.widget.RecyclerView.Adapter
	 */
	private class BorderWaitAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

		private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
		private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
		private Context context;
		private List<BorderWaitSouthboundFragment.BorderWaitAdapter.BorderWaitVH> mItems = new ArrayList<>();

		public BorderWaitAdapter(Context context) {
			super();
			this.context = context;
		}


		@Override
		public BorderWaitSouthboundFragment.BorderWaitAdapter.BorderWaitVH onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(context).inflate(R.layout.borderwait_row, null);
			BorderWaitSouthboundFragment.BorderWaitAdapter.BorderWaitVH viewholder = new BorderWaitSouthboundFragment.BorderWaitAdapter.BorderWaitVH(view);
			view.setTag(viewholder);
			mItems.add(viewholder);
			return viewholder;
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

			BorderWaitSouthboundFragment.BorderWaitAdapter.BorderWaitVH borderVH = (BorderWaitSouthboundFragment.BorderWaitAdapter.BorderWaitVH) viewHolder;

			String title = mBorderWaits[i].getTitle();
			String lane = mBorderWaits[i].getLane();

			borderVH.tt.setText(title + " (" + lane + ")");
			borderVH.tt.setTypeface(tfb);

			String created_at = mBorderWaits[i].getUpdated();
			borderVH.bt.setText(ParserUtils.relativeTime(created_at, "yyyy-MM-dd h:mm a", true));
			borderVH.bt.setTypeface(tf);

			int wait = mBorderWaits[i].getWait();
			if (wait == -1) {
				borderVH.rt.setText("N/A");
			} else if (wait < 5) {
				borderVH.rt.setText("< 5 min");
			} else {
				borderVH.rt.setText(wait + " min");
			}
			borderVH.rt.setTypeface(tfb);

			borderVH.iv.setImageResource(routeImage.get(mBorderWaits[i].getRoute()));
		}

		@Override
		public int getItemCount() {
			return 0;
		}

		// View Holder for list items.
		private class BorderWaitVH extends RecyclerView.ViewHolder {
			TextView tt;
			TextView bt;
			TextView rt;
			ImageView iv;

			public BorderWaitVH(View view) {
				super(view);
				tt = view.findViewById(R.id.toptext);
				bt = view.findViewById(R.id.bottomtext);
				rt = view.findViewById(R.id.righttext);
				iv = view.findViewById(R.id.icon);
			}
		}
	}

	public void onRefresh() {
		swipeRefreshLayout.post(new Runnable() {
			public void run() {
				swipeRefreshLayout.setRefreshing(true);
			}
		});
		downloadTask.cancel(false);
		downloadTask = new DownloadBorderWaitsAsyncTask(this);
		downloadTask.execute();
	}

}

