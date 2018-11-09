package gov.wa.wsdot.android.wsdot.shared.livedata;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.highwayalerts.HighwayAlertEntity;
import gov.wa.wsdot.android.wsdot.shared.HighwayAlertsItem;

/**
 *  LiveData class that transforms db entity Live data into a displayable
 *  object on a thread.
 *
 *  Used in favor of transformations.map since that method runs on main Thread.
 */
public class HighwayAlertsItemLiveData extends LiveData<List<HighwayAlertsItem>>
        implements Observer<List<HighwayAlertEntity>> {

    @NonNull
    private LiveData<List<HighwayAlertEntity>> sourceLiveData;

    public HighwayAlertsItemLiveData(@NonNull LiveData<List<HighwayAlertEntity>> sourceLiveData) {
        this.sourceLiveData = sourceLiveData;
    }

    @Override protected void onActive()   { sourceLiveData.observeForever(this); }
    @Override protected void onInactive() { sourceLiveData.removeObserver(this); }

    @Override public void onChanged(@Nullable List<HighwayAlertEntity> alerts) {
        AsyncTask.execute(() -> postValue(processDates(alerts)));
    }

    private List<HighwayAlertsItem> processDates(List<HighwayAlertEntity> alerts){

        ArrayList<HighwayAlertsItem> displayableAlertItemValues = new ArrayList<>();

        if (alerts != null) {
            for (HighwayAlertEntity alert : alerts) {
                displayableAlertItemValues.add(new HighwayAlertsItem(
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
        return displayableAlertItemValues;
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
            for (String phrase: me.getValue()) {
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
