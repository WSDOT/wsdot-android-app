package gov.wa.wsdot.android.wsdot.database.Notifications;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "notification_topic")
public class NotificationTopicEntity {

    @ColumnInfo(name = "topic")
    @PrimaryKey()
    @NonNull
    public String topic;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "subscribed")
    public Boolean subscribed;


    public NotificationTopicEntity(String topic, String category, Boolean subscribed){
        this.topic = topic;
        this.category = category;
        this.subscribed = subscribed;
    }

    public String getTopic() {
        return this.topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getCategory() {
        return this.category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getSubscribed() {
        return this.subscribed;
    }
    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }

}
