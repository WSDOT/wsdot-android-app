package gov.wa.wsdot.android.wsdot.database.traveltimes;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

/*
    POJO used by Room for retrieving a TravelTimeTripEntity with all of it's associated TravelTimeEntities.
 */
public class TravelTimeGroup {
    @Embedded
    public TravelTimeTripEntity trip;
    @Relation(parentColumn = "title", entityColumn = "trip_title", entity = TravelTimeEntity.class)
    public List<TravelTimeEntity> travelTimes;
}
