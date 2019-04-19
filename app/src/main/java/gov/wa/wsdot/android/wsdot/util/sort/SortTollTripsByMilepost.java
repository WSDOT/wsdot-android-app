package gov.wa.wsdot.android.wsdot.util.sort;

import java.util.Comparator;

import gov.wa.wsdot.android.wsdot.database.tollrates.dynamic.TollRateGroupDao;

public class SortTollTripsByMilepost implements Comparator<TollRateGroupDao.TollTripEntity> {

    public enum SortOrder {
        ASCENDING, DESCENDING
    }

    SortOrder order;

    public SortTollTripsByMilepost(SortOrder order){
        this.order = order;
    }

    public int compare(TollRateGroupDao.TollTripEntity a, TollRateGroupDao.TollTripEntity b) {
        switch (this.order) {
            case ASCENDING:
                return a.getEndMilepost().compareTo(b.getEndMilepost());
            case DESCENDING:
                return b.getEndMilepost().compareTo(a.getEndMilepost());
        }
        return 0;
    }
}
