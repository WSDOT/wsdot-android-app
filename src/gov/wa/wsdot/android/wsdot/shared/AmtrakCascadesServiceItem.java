package gov.wa.wsdot.android.wsdot.shared;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AmtrakCascadesServiceItem implements Serializable {

    private static final long serialVersionUID = 6993276477077392742L;
    
    private Date scheduledDepartureTime;
    private List<Map<String, AmtrakCascadesScheduleItem>> location;

    public AmtrakCascadesServiceItem(Date scheduledDepartureTime, List<Map<String, AmtrakCascadesScheduleItem>> location) {
        this.scheduledDepartureTime = scheduledDepartureTime;
        this.location = location;
    }
    
    public AmtrakCascadesServiceItem() {
    }

    public Date getScheduledDepartureTime() {
        return scheduledDepartureTime;
    }

    public void setScheduledDepartureTime(Date scheduledDepartureTime) {
        this.scheduledDepartureTime = scheduledDepartureTime;
    }

    public List<Map<String, AmtrakCascadesScheduleItem>> getLocation() {
        return location;
    }

    public void setLocation(List<Map<String, AmtrakCascadesScheduleItem>> location) {
        this.location = location;
    }

    public static Comparator<AmtrakCascadesServiceItem> scheduledDepartureTimeComparator = new Comparator<AmtrakCascadesServiceItem>() {

        public int compare(AmtrakCascadesServiceItem o1, AmtrakCascadesServiceItem o2) {
            return o1.getScheduledDepartureTime().compareTo(o2.getScheduledDepartureTime());
        }
    };

}
