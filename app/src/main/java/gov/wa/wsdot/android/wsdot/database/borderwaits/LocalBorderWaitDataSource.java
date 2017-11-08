package gov.wa.wsdot.android.wsdot.database.borderwaits;

import android.support.annotation.VisibleForTesting;

import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;

public class LocalBorderWaitDataSource implements BorderWaitDataSource {

    private static volatile LocalBorderWaitDataSource INSTANCE;

    private BorderWaitDao mBorderWaitDao;

    @VisibleForTesting
    LocalBorderWaitDataSource(BorderWaitDao borderWaitDao) {
        mBorderWaitDao = borderWaitDao;
    }

    public static LocalBorderWaitDataSource getInstance() {
        if (INSTANCE == null) {
            synchronized (LocalBorderWaitDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LocalBorderWaitDataSource(WsdotApplication.database.userDao());
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public BorderWaitEntity[] getBorderWaitsFor(String direction) {
        return mBorderWaitDao.loadBorderWaitsFor(direction);
    }

    @Override
    public BorderWaitEntity[] getStarredBorderWaits() {
        return mBorderWaitDao.loadStarredBorderWaits();
    }

    @Override
    public void insertOrUpdateBorderWaits(BorderWaitEntity[] borderWaits) {
        mBorderWaitDao.insertBorderWaits(borderWaits);
    }
}
