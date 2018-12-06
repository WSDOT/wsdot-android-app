package gov.wa.wsdot.android.wsdot.ui.trafficmap;

import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.trafficmap.MapLocationEntity;
import gov.wa.wsdot.android.wsdot.repository.MapLocationRepository;

public class FavoriteMapLocationViewModel extends ViewModel {

    MapLocationRepository mapLocationRepo;

    @Inject
    FavoriteMapLocationViewModel(MapLocationRepository mapLocationRepo){
        this.mapLocationRepo = mapLocationRepo;
    }

    public void addMapLocation(MapLocationEntity mapLocation) {
        this.mapLocationRepo.addMapLocation(mapLocation);
    }
}
