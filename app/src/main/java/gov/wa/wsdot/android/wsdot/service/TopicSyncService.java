package gov.wa.wsdot.android.wsdot.service;

import android.arch.lifecycle.LifecycleService;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import gov.wa.wsdot.android.wsdot.database.notifications.NotificationTopicEntity;
import gov.wa.wsdot.android.wsdot.repository.NotificationTopicsRepository;
import gov.wa.wsdot.android.wsdot.ui.notifications.NotificationsViewModel;


public class TopicSyncService extends LifecycleService {

    private static final String TAG = TopicSyncService.class.getSimpleName();

    private TopicViewModel viewModel;


    @Inject NotificationTopicsRepository repo;

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidInjection.inject(this);
        viewModel = new TopicViewModel(repo);
        viewModel.init(FirebaseInstanceId.getInstance().getId());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        viewModel.getTopics(FirebaseInstanceId.getInstance().getId()).observe(this, topics -> {
            if (topics != null) {
                for (NotificationTopicEntity topic: topics){
                    if (topic.subscribed){
                        FirebaseMessaging.getInstance().subscribeToTopic(topic.topic);
                    }
                }
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }
}