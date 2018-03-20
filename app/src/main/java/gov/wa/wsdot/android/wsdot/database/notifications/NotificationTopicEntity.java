package gov.wa.wsdot.android.wsdot.database.notifications;

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

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "subscribed")
    public Boolean subscribed;

    @ColumnInfo(name = "remove")
    public Boolean remove;

    public NotificationTopicEntity(@NonNull String topic, String title, String category, Boolean subscribed){
        this.topic = topic;
        this.title = title;
        this.category = category;
        this.subscribed = subscribed;
        this.remove = false;
    }

    public String getTopic() {
        return this.topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
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
