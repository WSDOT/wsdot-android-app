package gov.wa.wsdot.android.wsdot.util.sort;

import java.util.Comparator;

import gov.wa.wsdot.android.wsdot.database.tollrates.dynamic.TollRateGroup;

/**
 *  Comparator class for sorting toll groups by location name
 */
public class SortTollGroupByLocation implements Comparator<TollRateGroup> {
    public int compare(TollRateGroup a, TollRateGroup b) {
        if ( a.tollRateSign.getLocationName().compareTo(b.tollRateSign.getLocationName()) < 0) return -1;
        else if ( a.tollRateSign.getLocationName().compareTo(b.tollRateSign.getLocationName()) > 0) return 0;
        else return 1;
    }
}
