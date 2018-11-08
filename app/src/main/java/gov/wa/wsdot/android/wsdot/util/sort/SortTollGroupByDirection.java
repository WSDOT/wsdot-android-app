package gov.wa.wsdot.android.wsdot.util.sort;

import java.util.Comparator;

import gov.wa.wsdot.android.wsdot.database.tollrates.TollRateGroup;

/**
 *  Comparator class for sorting toll groups by travel direction
 */
public class SortTollGroupByDirection implements Comparator<TollRateGroup> {
    public int compare(TollRateGroup a, TollRateGroup b) {
        if ( a.tollRateSign.getTravelDirection().compareTo(b.tollRateSign.getTravelDirection()) < 0) return -1;
        else if ( a.tollRateSign.getTravelDirection().compareTo(b.tollRateSign.getTravelDirection()) > 0) return 0;
        else return 1;
    }
}