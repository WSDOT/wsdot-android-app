package gov.wa.wsdot.android.wsdot.ui.socialmedia.flickr;

/*
 * Copyright (c) 2015 Washington State Department of Transportation
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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.FlickrItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.decoration.SpacesItemDecoration;


public class FlickrFragment extends BaseFragment implements
        LoaderManager.LoaderCallbacks<ArrayList<FlickrItem>>,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = FlickrFragment.class.getSimpleName();
    private static FlickrItemAdapter mAdapter;
    private static ArrayList<FlickrItem> mFlickrItems = null;
    private View mEmptyView;
    private static SwipeRefreshLayout swipeRefreshLayout;
    private static final int IO_BUFFER_SIZE = 4 * 1024;

    protected RecyclerView mRecyclerView;
    protected GridLayoutManager mLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FlickrItemAdapter(null);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(4));


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
        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    public Loader<ArrayList<FlickrItem>> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created. There
        // is only one Loader with no arguments, so it is simple
        return new FlickrItemsLoader(getActivity());
    }

    public void onLoadFinished(Loader<ArrayList<FlickrItem>> loader, ArrayList<FlickrItem> data) {

        mEmptyView.setVisibility(View.GONE);

        if (!data.isEmpty()) {
            mAdapter.setData(data);
        } else {
            TextView t = (TextView) mEmptyView;
            t.setText(R.string.no_connection);
            mEmptyView.setVisibility(View.VISIBLE);
        }

        swipeRefreshLayout.setRefreshing(false);
    }

    public void onLoaderReset(Loader<ArrayList<FlickrItem>> loader) {
        swipeRefreshLayout.setRefreshing(false);
        mAdapter.setData(null);
    }

    /**
     * A custom Loader that loads Facebook posts from the data server.
     */
    public static class FlickrItemsLoader extends AsyncTaskLoader<ArrayList<FlickrItem>> {

        public FlickrItemsLoader(Context context) {
            super(context);
        }


        @Override
        public ArrayList<FlickrItem> loadInBackground() {
            BufferedInputStream ins;
            BufferedOutputStream out;
            mFlickrItems = new ArrayList<FlickrItem>();
            String content;
            String tmpContent;

            try {
                URL url = new URL("http://data.wsdot.wa.gov/mobile/FlickrPhotos.js.gz");
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
                JSONArray items = obj.getJSONArray("items");
                mFlickrItems = new ArrayList<FlickrItem>();
                FlickrItem i = null;

                int numItems = items.length();
                for (int j=0; j < numItems; j++) {
                    if (!this.isLoadInBackgroundCanceled()) {
                        JSONObject item = items.getJSONObject(j);
                        i = new FlickrItem();
                        i.setTitle(item.getString("title"));
                        i.setLink(item.getString("link"));
                        JSONObject media = item.getJSONObject("media");
                        i.setMedia(media.getString("m"));
                        i.setPublished(item.getString("published"));
                        tmpContent = item.getString("description");
                        content = tmpContent.replace(" <p><a href=\"https://www.flickr.com/people/wsdot/\">WSDOT</a> posted a photo:</p> ", "");
                        i.setContent(content);

                        String imageSrc = i.getMedia();
                        ins = new BufferedInputStream(new URL(imageSrc).openStream(), IO_BUFFER_SIZE);
                        final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                        out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
                        copy(ins, out);
                        out.flush();
                        final byte[] data = dataStream.toByteArray();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                        @SuppressWarnings("deprecation")
                        final Drawable image = new BitmapDrawable(bitmap);
                        i.setImage(image);

                        mFlickrItems.add(i);
                    } else {
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing Flickr JSON feed", e);
            }
            return mFlickrItems;
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
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        @Override
        public void onCanceled(ArrayList<FlickrItem> data) {
            super.onCanceled(data);
        }

        @Override
        protected void onReset() {
            super.onReset();
            // Ensure the loader is stopped
            onStopLoading();
        }

    }

    /**
     * Custom adapter for items in recycler view.
     *
     * Extending RecyclerView adapter this adapter binds the custom ViewHolder
     * class to it's data.
     *
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class FlickrItemAdapter extends RecyclerView.Adapter<FlickrViewHolder> {

        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private List<FlickrItem> imageList;

        public FlickrItemAdapter(List<FlickrItem> posts){
            this.imageList = posts;
            notifyDataSetChanged();
        }

        @Override
        public FlickrViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.gridview_item, parent, false);
            return new FlickrViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(FlickrViewHolder holder, int position) {

            FlickrItem post = imageList.get(position);

            holder.picture.setVisibility(View.VISIBLE);
            holder.picture.setImageDrawable(post.getImage());

            holder.text.setText(post.getTitle());
            holder.text.setTypeface(tf);

            final String postLink = post.getLink();

            // Set onClickListener for holder's view
            holder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(postLink));
                            startActivity(intent);
                        }
                    }
            );
        }

        @Override
        public int getItemCount() {
            if (imageList == null) {
                return 0;
            }else {
                return imageList.size();
            }
        }

        public void clear(){
            if (imageList != null) {
                this.imageList.clear();
                notifyDataSetChanged();
            }
        }

        public void setData(List<FlickrItem> posts){
            this.imageList = posts;
            notifyDataSetChanged();
        }

    }

    public static class FlickrViewHolder extends RecyclerView.ViewHolder {
        protected TextView text;
        protected ImageView picture;


        public FlickrViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
            picture = (ImageView) itemView.findViewById(R.id.picture);
        }
    }

    public void onRefresh() {
        swipeRefreshLayout.post(new Runnable() {
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        getLoaderManager().restartLoader(0, null, this);
    }

/**
 * Copy the content of the input stream into the output stream, using a
 * temporary byte array buffer whose size is defined by
 * {@link #IO_BUFFER_SIZE}.
 *
 * @param in The input stream to copy from.
 * @param out The output stream to copy to.
 * @throws IOException If any error occurs during the copy.
 */
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }
}