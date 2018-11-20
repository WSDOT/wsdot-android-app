package gov.wa.wsdot.android.wsdot.ui.camera;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.cameras.CameraEntity;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.ui.home.FavoritesViewModel;

public class CameraViewPagerActivity extends BaseActivity {

    final static String TAG = CameraViewPagerActivity.class.getSimpleName();

    private Toolbar mToolbar;
    CameraCollectionPagerAdapter mCameraCollectionPagerAdapter;
    ViewPager mViewPager;

    static Snackbar tipSnackbar;

    private int selectedPage = -1;

    FavoritesViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    public void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera_view_pager);

        mToolbar = findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tipSnackbar = Snackbar.make(findViewById(R.id.placeSnackBar), "Swipe left or right to check your other favorite cameras", Snackbar.LENGTH_INDEFINITE);

        Bundle b = getIntent().getExtras();
        int selectedCameraId = b.getInt("id", 0);

        if (savedInstanceState != null){
            selectedPage =  savedInstanceState.getInt("selected_page", 0);
        }

        mViewPager = findViewById(R.id.pager);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if (position != selectedPage) {
                    tipSnackbar.dismiss();
                    // TODO: firebase camera swipe event

                }
                selectedPage = position;
                mToolbar.setTitle(mCameraCollectionPagerAdapter.getPageTitle(position));
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FavoritesViewModel.class);

        viewModel.getFavoriteCameras().observe(this, cameras -> {
            if (cameras != null) {

                if (mCameraCollectionPagerAdapter == null){
                    mCameraCollectionPagerAdapter = new CameraCollectionPagerAdapter(getSupportFragmentManager());
                    mCameraCollectionPagerAdapter.setItems(cameras);
                    mViewPager.setAdapter(mCameraCollectionPagerAdapter);
                } else {
                    mCameraCollectionPagerAdapter.setItems(cameras);
                }

                if (cameras.size() > 0) {

                    displayTipIfNeeded(cameras.size());

                    if (selectedPage == -1) {
                        selectedPage = getSelectedCameraIndex(selectedCameraId, cameras);
                    }
                    // set current item to camera user selected from favorites.
                    mViewPager.setCurrentItem(selectedPage);
                    mToolbar.setTitle(mCameraCollectionPagerAdapter.getPageTitle(selectedPage));

                }
            }
        });

        String adTarget = b.getString("advertisingTarget");
        enableAds(adTarget);

    }

    @Override
    public void onResume() {
        super.onResume();
        setFirebaseAnalyticsScreenName("CameraImagePager");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("selected_page", selectedPage);
    }

    private int getSelectedCameraIndex(int id, List<CameraEntity> cameras) {
        for (CameraEntity camera: cameras){
            if (camera.getCameraId() == id) {
                return cameras.indexOf(camera);
            }
        }
        return 0;
    }

    private void displayTipIfNeeded(int numCameras){

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean seenTip = settings.getBoolean("KEY_SEEN_CAMERA_SWIPE_TIP", false);

        if (!seenTip && numCameras > 1) {
            tipSnackbar.show();
            settings.edit().putBoolean("KEY_SEEN_CAMERA_SWIPE_TIP", true).apply();
        }
    }
}
