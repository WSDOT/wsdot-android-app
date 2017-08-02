package gov.wa.wsdot.android.wsdot.ui.trafficmap.besttimestotravel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.TravelChartItem;
import gov.wa.wsdot.android.wsdot.shared.TravelChartRouteItem;
import gov.wa.wsdot.android.wsdot.util.APIEndPoints;
import gov.wa.wsdot.android.wsdot.util.Utils;

/**
 * A fragment representing a list of travel chart items.
 */
public class TravelChartsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<ArrayList<TravelChartRouteItem>>,
        AdapterView.OnItemSelectedListener {

    private static final String TAG = TravelChartsFragment.class.getSimpleName();

    private ArrayList<TravelChartRouteItem> travelChartData = new ArrayList<>();

    private static final int IO_BUFFER_SIZE = 4 * 1024;

    private static SwipeRefreshLayout swipeRefreshLayout;
    private View mEmptyView;

    private static MyTravelChartRecyclerViewAdapter mAdapter;
    private int routeIndex = 0;

    public static ArrayList<CharSequence> spinnerOptions = new ArrayList<>();
    private Spinner routeSpinner;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TravelChartsFragment() {}

    @SuppressWarnings("unused")
    public static TravelChartsFragment newInstance(int routeIndex) {
        TravelChartsFragment fragment = new TravelChartsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recycler_with_spinner_swipe_refresh, container, false);

        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        mAdapter = new MyTravelChartRecyclerViewAdapter(new ArrayList<TravelChartItem>());

        recyclerView.setAdapter(mAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.holo_blue_bright,
                R.color.holo_green_light,
                R.color.holo_orange_light,
                R.color.holo_red_light);

        mEmptyView = view.findViewById( R.id.empty_list_view );
        mEmptyView.setVisibility(View.INVISIBLE);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    public Loader<ArrayList<TravelChartRouteItem>> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
        return new TravelChartsFragment.TravelChartItemsLoader(getActivity());
    }

    public void onLoadFinished(Loader<ArrayList<TravelChartRouteItem>> loader, ArrayList<TravelChartRouteItem> data) {

        mEmptyView.setVisibility(View.GONE);
        swipeRefreshLayout.setEnabled(false);

        if (!data.isEmpty()) {
            mEmptyView.setVisibility(View.INVISIBLE);
            mAdapter.setData(data.get(routeIndex).getCharts());

            travelChartData = data;

            routeSpinner = (Spinner) getActivity().findViewById(R.id.day_spinner);

            ArrayAdapter<CharSequence> routeArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, spinnerOptions);;
            routeArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            routeSpinner.setAdapter(routeArrayAdapter);

            routeSpinner.setOnItemSelectedListener(this);

            routeSpinner.setSelection(routeIndex, false);
        } else {
            TextView t = (TextView) mEmptyView;
            t.setText("Travel charts are unavailable at this time.");
            mEmptyView.setVisibility(View.VISIBLE);
        }

        swipeRefreshLayout.setRefreshing(false);
    }

    public void onLoaderReset(Loader<ArrayList<TravelChartRouteItem>> loader) {
        swipeRefreshLayout.setRefreshing(false);
        mAdapter.setData(null);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        routeIndex = position;
        mAdapter.setData(travelChartData.get(position).getCharts());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    /**
     * A custom Loader that loads travel charts from the data server.
     */
    public static class TravelChartItemsLoader extends AsyncTaskLoader<ArrayList<TravelChartRouteItem>> {

        public TravelChartItemsLoader(Context context) {
            super(context);
        }

        @Override
        public ArrayList<TravelChartRouteItem> loadInBackground() {
            BufferedInputStream ins;
            BufferedOutputStream out;
            ArrayList<TravelChartRouteItem> mTravelChartRoutes = new ArrayList<>();

            try {
                URL url = new URL(APIEndPoints.TRAVEL_CHARTS);
                URLConnection urlConn = url.openConnection();

                BufferedInputStream bis = new BufferedInputStream(urlConn.getInputStream());
                GZIPInputStream gzin = new GZIPInputStream(bis);
                InputStreamReader is = new InputStreamReader(gzin);
                BufferedReader in = new BufferedReader(is);

                String jsonFile = "";
                String line;
                while ((line = in.readLine()) != null)
                    jsonFile += line;
                in.close();

                JSONObject obj = new JSONObject(jsonFile);

                spinnerOptions.clear();

                if (!obj.getBoolean("available")){
                    return new ArrayList<>();
                }

                JSONArray items = obj.getJSONArray("routes");

                int numItems = items.length();
                for (int j=0; j < numItems; j++) {
                    if (!this.isLoadInBackgroundCanceled()) {
                        JSONObject item = items.getJSONObject(j);

                        TravelChartRouteItem  routeItem = new TravelChartRouteItem();

                        routeItem.setName(item.getString("name"));
                        spinnerOptions.add(j, item.getString("name"));

                        ArrayList<TravelChartItem> chartItems = new ArrayList<>();

                        JSONArray charts = item.getJSONArray("charts");

                        for (int k = 0; k < charts.length(); k++){
                            TravelChartItem chartItem = new TravelChartItem();

                            String imageSrc = charts.getJSONObject(k).getString("url");
                            ins = new BufferedInputStream(new URL(imageSrc).openStream(), IO_BUFFER_SIZE);
                            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                            out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
                            Utils.copy(ins, out, IO_BUFFER_SIZE);
                            out.flush();
                            final byte[] data = dataStream.toByteArray();
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                            @SuppressWarnings("deprecation")
                            final Drawable image = new BitmapDrawable(bitmap);
                            chartItem.setImage(image);


                            chartItem.setAltText(charts.getJSONObject(k).getString("altText"));
                            chartItems.add(chartItem);
                        }

                        routeItem.setCharts(chartItems);

                        mTravelChartRoutes.add(routeItem);

                    } else {
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing travel chart JSON feed", e);
            }
            swipeRefreshLayout.post(
                    new Runnable() {
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
            return mTravelChartRoutes;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            swipeRefreshLayout.post(
                    new Runnable() {
                        public void run() {
                            swipeRefreshLayout.setRefreshing(true);
                        }
                    });
            forceLoad();
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();
            swipeRefreshLayout.post(
                    new Runnable() {
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        @Override
        public void onCanceled(ArrayList<TravelChartRouteItem> data) {
            super.onCanceled(data);
        }

        @Override
        protected void onReset() {
            super.onReset();
            // Ensure the loader is stopped
            onStopLoading();
        }
    }
}