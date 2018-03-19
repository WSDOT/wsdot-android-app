package gov.wa.wsdot.android.wsdot.database.Notifications;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

@Dao
public abstract class NotificationTopicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertNotificationTopics(NotificationTopicEntity... topics);

    @Query("SELECT * FROM notification_topic")
    public abstract LiveData<List<NotificationTopicEntity>> loadNotificationTopics();

    @Query("UPDATE notification_topic SET subscribed = :subscribed WHERE topic = :topic")
    public abstract void updateSubscription(String topic, Boolean subscribed);

    @Query("DELETE FROM notification_topic")
    public abstract void deleteAll();

    @Transaction
    public void deleteAndInsertTransaction(NotificationTopicEntity... topics) {
        deleteAll();
        insertNotificationTopics(topics);
    }
}
