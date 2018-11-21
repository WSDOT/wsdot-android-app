package gov.wa.wsdot.android.wsdot.ui.notifications;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.notifications.NotificationTopicEntity;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;

public class NotificationsFragment extends BaseFragment implements Injectable {

    private static final String TAG = NotificationsFragment.class.getSimpleName();

    private View mEmptyView;

    private static TopicAdapter mAdapter;
    protected ExpandableListView mExpandableView;
    private View mLoadingSpinner;

    public static final int HEADER_VIEWTYPE = 0;
    public static final int TOPIC_VIEWTYPE = 1;

    private static NotificationsViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_expandable, null);

        mExpandableView = root.findViewById(R.id.my_expandable_view);

        mAdapter = new TopicAdapter(getActivity());
        mExpandableView.setAdapter(mAdapter);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mLoadingSpinner = root.findViewById(R.id.progress_bar);

        mEmptyView = root.findViewById( R.id.empty_list_view );
        mEmptyView.setVisibility(View.GONE);

        TextView emptyText = (TextView) mEmptyView;
        emptyText.setText("Notifications unavailable.");

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
                if (topics.size() == 0){
                    if (mLoadingSpinner.getVisibility() == View.INVISIBLE) {
                        mEmptyView.setVisibility(View.VISIBLE);
                    }
                } else {
                    mEmptyView.setVisibility(View.GONE);
                }
            }
        });
        return root;
    }

    private class TopicAdapter extends BaseExpandableListAdapter {

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
        public Object getChild(int groupPosition, int childPosition) {
            String key = (String) mData.keySet().toArray()[groupPosition];
            return mData.get(key).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View view, ViewGroup parent) {

            NotificationTopicEntity topic = (NotificationTopicEntity) mAdapter.getChild(groupPosition, childPosition);

            if (view == null) {
                LayoutInflater infalInflater = (LayoutInflater)
                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = infalInflater.inflate(R.layout.list_item_with_checkbox, null);
            }

            TopicViewHolder topicViewHolder = new TopicViewHolder(view);

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
                            .make(getView(), "Subscribed to " + topic.getTitle(), Snackbar.LENGTH_SHORT);
                    Snackbar removed_snackbar = Snackbar
                            .make(getView(), "Unsubscribed from " + topic.getTitle(), Snackbar.LENGTH_SHORT);
                    added_snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onShown(Snackbar snackbar) {
                            super.onShown(snackbar);
                            snackbar.getView().setContentDescription("Subscribed to " + topic.getTitle());
                            snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                        }
                    });
                    removed_snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onShown(Snackbar snackbar) {
                            super.onShown(snackbar);
                            snackbar.getView().setContentDescription("Unsubscribed from " + topic.getTitle());
                            snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                        }
                    });

                    if (isChecked) {
                        viewModel.updateSubscription(topic.getTopic(), true);
                        added_snackbar.show();
                        ((BaseActivity)getActivity()).setFirebaseAnalyticsEvent("notifications","update_subscription","subscribed");
                        FirebaseMessaging.getInstance().subscribeToTopic(topic.getTopic())
                                .addOnCompleteListener(task -> {
                                    // If the operation fails revert saved sub status
                                    // let user know
                                    if (!task.isSuccessful()) {
                                        viewModel.updateSubscription(topic.getTopic(), false);
                                    }
                                });

                    } else {
                        viewModel.updateSubscription(topic.getTopic(), false);
                        removed_snackbar.show();
                        ((BaseActivity)getActivity()).setFirebaseAnalyticsEvent("notifications","update_subscription","unsubscribed");
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic.getTopic())
                                .addOnCompleteListener(task -> {
                                    if (!task.isSuccessful()) {
                                        viewModel.updateSubscription(topic.getTopic(), true);
                                    }
                                });
                    }
                }
            });

            return view;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            String key = (String) mData.keySet().toArray()[groupPosition];
            return mData.get(key).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            String key = (String) mData.keySet().toArray()[groupPosition];
            return key;
        }

        @Override
        public int getGroupCount() {
            return mData.keySet().size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isLastChild, View view,
                                 ViewGroup parent) {

            String header = (String) getGroup(groupPosition);

            if (view == null) {
                LayoutInflater inf = (LayoutInflater)
                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inf.inflate(R.layout.expandable_list_header_title, parent, false);
            }

            HeaderViewHolder headerViewHolder = new HeaderViewHolder(view);
            headerViewHolder.title.setText(header);

            return view;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
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

            public HeaderViewHolder(View view) {
                super(view);
                title = view.findViewById(R.id.list_header_title);
            }
        }
    }
}