package gov.wa.wsdot.android.wsdot.util.sort;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Locale;

import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;

public class SortHighwayAlertItemsByDate implements Comparator<HighwayAlertsItem> {

    DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.US);

    public int compare(HighwayAlertsItem a, HighwayAlertsItem b) {
        try {
            if (dateFormat.parse(a.getLastUpdatedTime()).before(dateFormat.parse(b.getLastUpdatedTime())))
                return -1;
            else if (dateFormat.parse(a.getLastUpdatedTime()).after(dateFormat.parse(b.getLastUpdatedTime())))
                return 1;
            else return 0;
        } catch (ParseException e){
            return 0;
        }
    }

}
