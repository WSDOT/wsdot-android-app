package gov.wa.wsdot.android.wsdot.database.tollrates.dynamic;

import java.util.List;

import androidx.room.Embedded;
import androidx.room.Relation;

/**
 *  POJO used by Room for retrieving a TollRateSignEntity with all of it's associated TollTripEntity.
 */
public class TollRateGroup {

    @Embedded
    public TollRateSignEntity tollRateSign;
    @Relation(parentColumn = "id", entityColumn = "sign_id", entity = TollRateGroupDao.TollTripEntity.class)
    public List<TollRateGroupDao.TollTripEntity> trips;

}
