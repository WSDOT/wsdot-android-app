package gov.wa.wsdot.android.wsdot.util.sort;

import java.util.Comparator;

import gov.wa.wsdot.android.wsdot.database.tollrates.TollRateGroup;

public class SortTollGroupByMilepost implements Comparator<TollRateGroup> {

    public enum SortOrder {
        ASCENDING, DESCENDING
    }

    SortOrder order;

    public SortTollGroupByMilepost(SortOrder order){
        this.order = order;
    }

    public int compare(TollRateGroup a, TollRateGroup b) {
        switch (this.order) {
            case ASCENDING:
                return a.tollRateSign.getMilepost().compareTo(b.tollRateSign.getMilepost());
            case DESCENDING:
                return b.tollRateSign.getMilepost().compareTo(a.tollRateSign.getMilepost());
        }
        return 0;
    }
}