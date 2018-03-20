package gov.wa.wsdot.android.wsdot.database.notifications;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public abstract class NotificationTopicDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertNotificationTopics(NotificationTopicEntity... topics);

    @Query("SELECT * FROM notification_topic")
    public abstract LiveData<List<NotificationTopicEntity>> loadNotificationTopics();

    @Query("UPDATE notification_topic SET subscribed = :subscribed WHERE topic = :topic")
    public abstract void updateSubscription(String topic, Boolean subscribed);

    @Query("DELETE FROM notification_topic WHERE remove = 1")
    public abstract void clean();

    @Query("UPDATE notification_topic SET remove = 1")
    public abstract void markAllForClean();

    @Query("UPDATE notification_topic SET remove = 0 WHERE topic = :topic")
    public abstract void updateNotificationTopics(String topic);

    @Transaction
    public void updateAndCleanTransaction(NotificationTopicEntity... topics) {
        // mark all current topics for removal
        markAllForClean();

        // insert any new topics, removal flag will be false
        insertNotificationTopics(topics);

        // Set removal flag false for topics still available
        for (NotificationTopicEntity topicEntity : topics){
            updateNotificationTopics(topicEntity.topic);
        }

        // remove any old topics not received from the server.
        clean();
    }
}
