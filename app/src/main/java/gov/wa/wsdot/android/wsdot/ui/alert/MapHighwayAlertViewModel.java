package gov.wa.wsdot.android.wsdot.ui.alert;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.highwayalerts.HighwayAlertEntity;
import gov.wa.wsdot.android.wsdot.repository.HighwayAlertRepository;
import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;
import gov.wa.wsdot.android.wsdot.util.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class MapHighwayAlertViewModel extends ViewModel {

    private static String TAG = MapHighwayAlertViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private List<HighwayAlertEntity> displayableAlerts;
    private MediatorLiveData<List<HighwayAlertsItem>> displayedAlerts;

    private AppExecutors appExecutors;

    private HighwayAlertRepository alertRepo;

    @Inject
    MapHighwayAlertViewModel( HighwayAlertRepository alertRepo, AppExecutors appExecutors) {
        this.mStatus = new MutableLiveData<>();
        this.displayableAlerts = new ArrayList<>();
        this.displayedAlerts = new MediatorLiveData<>();
        this.alertRepo = alertRepo;
        this.appExecutors = appExecutors;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public MediatorLiveData<List<HighwayAlertsItem>> getDisplayAlerts() {
        return this.displayedAlerts;
    }

    public void loadDisplayAlerts(LatLngBounds bounds){
        displayedAlerts.addSource(alertRepo.getHighwayAlerts(mStatus), alerts -> {
            this.displayableAlerts = alerts;
            refreshDisplayedAlerts(bounds);
        });
    }

    public void refreshDisplayedAlerts(LatLngBounds bounds) {
        appExecutors.taskIO().execute(() -> {
            ArrayList<HighwayAlertsItem> displayedAlertValues = new ArrayList<>();

            for (HighwayAlertEntity alert : this.displayableAlerts) {

                LatLng alertLocation = new LatLng(alert.getStartLatitude(), alert.getStartLongitude());

                if (bounds.contains(alertLocation)) {
                    displayedAlertValues.add(new HighwayAlertsItem(
                            String.valueOf(alert.getAlertId()),
                            alert.getStartLatitude(),
                            alert.getStartLongitude(),
                            alert.getEndLatitude(),
                            alert.getEndLongitude(),
                            alert.getCategory(),
                            alert.getHeadline(),
                            alert.getLastUpdated(),
                            alert.getPriority(),
                            getCategoryIcon(
                                    alert.getCategory(),
                                    alert.getPriority()
                            )
                    ));

                }
            }
            displayedAlerts.postValue(displayedAlertValues);
        });
    }

    /**
     * Get the correct icon given the priority and category of alert.
     *
     * @param category
     * @param priority
     * @return
     */
    private int getCategoryIcon(String category, String priority) {

        int alertClosed = R.drawable.closed;
        int alertHighest = R.drawable.alert_highest;
        int alertHigh = R.drawable.alert_high;
        int alertMedium = R.drawable.alert_moderate;
        int alertLow = R.drawable.alert_low;
        int constructionHighest = R.drawable.construction_highest;
        int constructionHigh = R.drawable.construction_high;
        int constructionMedium = R.drawable.construction_moderate;
        int constructionLow = R.drawable.construction_low;
        int defaultAlertImage = alertHighest;

        // Types of categories which result in one icon or another being displayed.
        String[] event_closure = {"closed", "closure"};
        String[] event_construction = {"construction", "maintenance", "lane closure"};

        HashMap<String, String[]> eventCategories = new HashMap<>();

        eventCategories.put("closure", event_closure);
        eventCategories.put("construction", event_construction);


        Set<Map.Entry<String, String[]>> set = eventCategories.entrySet();
        Iterator<Map.Entry<String, String[]>> i = set.iterator();

        if (category.equals("")) return defaultAlertImage;

        while(i.hasNext()) {
            Map.Entry<String, String[]> me = i.next();
            for (String phrase: (String[])me.getValue()) {
                String patternStr = phrase;
                Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(category);
                boolean matchFound = matcher.find();

                if (matchFound) {
                    String keyWord = me.getKey();

                    if (keyWord.equalsIgnoreCase("closure")) {
                        return alertClosed;
                    } else if (keyWord.equalsIgnoreCase("construction")) {
                        if (priority.equalsIgnoreCase("highest")) {
                            return constructionHighest;
                        } else if (priority.equalsIgnoreCase("high")) {
                            return constructionHigh;
                        } else if (priority.equalsIgnoreCase("medium")) {
                            return constructionMedium;
                        } else if (priority.equalsIgnoreCase("low")
                                || priority.equalsIgnoreCase("lowest")) {
                            return constructionLow;
                        }
                    }
                }
            }
        }

        // If we arrive here, it must be an accident or alert item.
        if (priority.equalsIgnoreCase("highest")) {
            return alertHighest;
        } else if (priority.equalsIgnoreCase("high")) {
            return alertHigh;
        } else if (priority.equalsIgnoreCase("medium")) {
            return alertMedium;
        } else if (priority.equalsIgnoreCase("low")
                || priority.equalsIgnoreCase("lowest")) {
            return alertLow;
        }

        return defaultAlertImage;
    }
}
