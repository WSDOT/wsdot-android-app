package gov.wa.wsdot.android.wsdot.ui.camera;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import gov.wa.wsdot.android.wsdot.database.cameras.CameraEntity;

public class CameraCollectionPagerAdapter extends FragmentStatePagerAdapter {

    private List<CameraEntity> cameras;

    public CameraCollectionPagerAdapter(FragmentManager fm) {
        super(fm);
        cameras = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new CameraImageFragment();
        Bundle args = new Bundle();
        args.putInt("id", cameras.get(i).getCameraId());
        args.putBoolean("show_star", false);
        fragment.setArguments(args);
        return fragment;
    }

    public void setItems(List<CameraEntity> cameras) {
        this.cameras = cameras;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.cameras.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return this.cameras.get(position).getTitle();
    }
}