package gov.wa.wsdot.android.wsdot.ui.notifications;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.notifications.NotificationTopicEntity;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.MyLogger;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class NotificationsFragment extends BaseFragment implements Injectable {

    private static final String TAG = NotificationsFragment.class.getSimpleName();
    private View mEmptyView;


    private static TopicAdapter mAdapter;
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;
    private View mLoadingSpinner;

    public static final int HEADER_VIEWTYPE = 0;
    public static final int TOPIC_VIEWTYPE = 1;

    private static NotificationsViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_with_spinner, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new TopicAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mLoadingSpinner = root.findViewById(R.id.loading_spinner);

        mEmptyView = root.findViewById( R.id.empty_list_view );
        mEmptyView.setVisibility(View.GONE);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(NotificationsViewModel.class);

        viewModel.init(FirebaseInstanceId.getInstance().getToken());

        viewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        mLoadingSpinner.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        mLoadingSpinner.setVisibility(View.INVISIBLE);
                        break;
                    case ERROR:
                        Toast.makeText(this.getContext(), "connection error", Toast.LENGTH_LONG).show();

                        mLoadingSpinner.setVisibility(View.INVISIBLE);
                }
            }
        });

        viewModel.getTopics().observe(this, topics -> {
            if (topics != null) {
                mAdapter.setData(topics);
            }
        });
        return root;
    }

    /**
     * Custom adapter for items in recycler view.
     *
     * Binds the custom ViewHolder class to it's data.
     *
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class TopicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        private Context context;
        private HashMap<String, List<NotificationTopicEntity>> mData = new HashMap<>();

        public TopicAdapter(Context context) {
            this.context = context;
        }

        public void setData(HashMap<String, List<NotificationTopicEntity>> data){
            this.mData = data;
            this.notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView;
            switch (viewType) {
                case TOPIC_VIEWTYPE:
                    View view = LayoutInflater.from(context).inflate(R.layout.list_item_with_checkbox, null);
                    TopicViewHolder viewholder = new TopicViewHolder(view);
                    view.setTag(viewholder);
                    return viewholder;
                case HEADER_VIEWTYPE:
                    itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_header, parent, false);
                    return new HeaderViewHolder(itemView);
                default:
                    MyLogger.crashlyticsLog("Notifications", "Error", "FavoritesFragment: No matching view type for type: " + viewType, 1);
                    Log.e(TAG, "No matching view type for type: " + viewType);
                }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

            if (viewHolder instanceof HeaderViewHolder){

                HeaderViewHolder viewholder = (HeaderViewHolder) viewHolder;

                String title = (String) mAdapter.getItem(position);

                viewholder.title.setText(title);
                viewholder.title.setTypeface(tfb);

                viewholder.divider.setVisibility(View.GONE);

            } else {

                TopicViewHolder topicViewHolder = (TopicViewHolder) viewHolder;

                NotificationTopicEntity topic = (NotificationTopicEntity) mAdapter.getItem(position);

                String title = topic.getTitle();
                topicViewHolder.title.setText(title);
                topicViewHolder.title.setTypeface(tfb);

                // Seems when Android recycles the views, the onCheckedChangeListener is still active
                // and the call to setChecked() causes that code within the listener to run repeatedly.
                // Assigning null to setOnCheckedChangeListener seems to fix it.
                topicViewHolder.subscribed_button.setOnCheckedChangeListener(null);
                topicViewHolder.subscribed_button.setContentDescription("subscribe");
                topicViewHolder.subscribed_button.setChecked(topic.getSubscribed());
                topicViewHolder.subscribed_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        Snackbar added_snackbar = Snackbar
                                .make(getView(), "subscribed to " + topic.getTitle(), Snackbar.LENGTH_SHORT);

                        Snackbar removed_snackbar = Snackbar
                                .make(getView(), "unsubscribed from " + topic.getTitle(), Snackbar.LENGTH_SHORT);

                        added_snackbar.addCallback(new Snackbar.Callback() {
                            @Override
                            public void onShown(Snackbar snackbar) {
                                super.onShown(snackbar);
                                snackbar.getView().setContentDescription("subscribed to " + topic.getTitle());
                                snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                            }
                        });

                        removed_snackbar.addCallback(new Snackbar.Callback() {
                            @Override
                            public void onShown(Snackbar snackbar) {
                                super.onShown(snackbar);
                                snackbar.getView().setContentDescription("unsubscribed from " + topic.getTitle());
                                snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                            }
                        });

                        if (isChecked) {
                            added_snackbar.show();
                        } else {
                            removed_snackbar.show();
                        }

                        viewModel.updateSubscription(topic.getTopic(), isChecked);
                    }
                });
            }
        }

        public Object getItem(int position){

            for (Map.Entry<String, List<NotificationTopicEntity>> entry : mData.entrySet()) {

                int size = entry.getValue().size() + 1;

                // check if position inside this section
                if (position == 0 && size > 0) return entry.getKey();
                if (position < size) return entry.getValue().get(position - 1);

                position -= size;
            }

            return -1;
        }

        @Override
        public int getItemCount() {

            int count = 0;

            // Iterating over values only
            for (List<NotificationTopicEntity> value : mData.values()) {
                count += value.size() + 1; // +1 for header
            }

            return count;
        }


        @Override
        public int getItemViewType(int position) {

            for (Map.Entry<String, List<NotificationTopicEntity>> entry : mData.entrySet()) {

                int size = entry.getValue().size() + 1;

                // check if position inside this section
                if (position == 0 && size > 0) return HEADER_VIEWTYPE;
                if (position < size) return TOPIC_VIEWTYPE;

                position -= size;
            }

            return -1;
        }

        private class TopicViewHolder extends RecyclerView.ViewHolder {

            TextView title;
            CheckBox subscribed_button;

            public TopicViewHolder(View view) {
                super(view);
                title = view.findViewById(R.id.title);
                subscribed_button = view.findViewById(R.id.checkbox);

            }
        }

        private class HeaderViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            LinearLayout divider;

            public HeaderViewHolder(View view) {
                super(view);
                title = view.findViewById(R.id.list_header_title);
                divider = view.findViewById(R.id.divider);
            }
        }
    }
}