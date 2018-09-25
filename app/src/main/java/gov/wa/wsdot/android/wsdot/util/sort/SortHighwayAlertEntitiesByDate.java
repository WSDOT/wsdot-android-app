package gov.wa.wsdot.android.wsdot.util.sort;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Locale;

import gov.wa.wsdot.android.wsdot.database.highwayalerts.HighwayAlertEntity;


public class SortHighwayAlertEntitiesByDate  implements Comparator<HighwayAlertEntity> {

    public enum SortOrder {
        ASCENDING, DESCENDING
    }

    SortOrder sortOrder;

    private DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.US);

    public SortHighwayAlertEntitiesByDate(SortOrder order){
        this.sortOrder = order;
    }

    public int compare(HighwayAlertEntity a, HighwayAlertEntity b) {

        try {
            switch (this.sortOrder) {
                case ASCENDING:
                    return dateFormat.parse(a.getLastUpdated()).after(dateFormat.parse(b.getLastUpdated())) ? 1 : -1;
                case DESCENDING:
                    return dateFormat.parse(a.getLastUpdated()).before(dateFormat.parse(b.getLastUpdated())) ? 1 : -1;
            }
            return 0;
        } catch (ParseException e){
            return 0;
        }
    }
}
