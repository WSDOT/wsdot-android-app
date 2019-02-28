package gov.wa.wsdot.android.wsdot.database.traveltimes;

import java.util.List;

import androidx.room.Embedded;
import androidx.room.Relation;

/**
 *  POJO used by Room for retrieving a TravelTimeTripEntity with all of it's associated TravelTimeEntities.
 */
public class TravelTimeGroup {
    @Embedded
    public TravelTimeTripEntity trip;
    @Relation(parentColumn = "title", entityColumn = "trip_title", entity = TravelTimeEntity.class)
    public List<TravelTimeEntity> travelTimes;
}
