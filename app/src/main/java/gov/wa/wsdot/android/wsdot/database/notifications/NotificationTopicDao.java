package gov.wa.wsdot.android.wsdot.database.notifications;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

@Dao
public abstract class NotificationTopicDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertNotificationTopics(NotificationTopicEntity... topics);

    @Query("SELECT * FROM notification_topic")
    public abstract LiveData<List<NotificationTopicEntity>> loadNotificationTopics();

    @Query("SELECT * FROM notification_topic")
    public abstract List<NotificationTopicEntity> getNotificationTopics();

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
