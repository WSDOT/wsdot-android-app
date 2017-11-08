package gov.wa.wsdot.android.wsdot.database.borderwaits;

public interface BorderWaitDataSource {

    BorderWaitEntity[] getBorderWaitsFor(String direction);

    BorderWaitEntity[] getStarredBorderWaits();

    void insertOrUpdateBorderWaits(BorderWaitEntity[] borderWaits);

}
