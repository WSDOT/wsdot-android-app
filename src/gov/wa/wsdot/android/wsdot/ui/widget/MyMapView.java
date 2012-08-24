/*
 * Copyright (c) 2012 Washington State Department of Transportation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package gov.wa.wsdot.android.wsdot.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class MyMapView extends MapView {
	private long lastTouchTime = -1;
	private MyMapView mMyMapView;
	private long mEventsTimeout = 250L;
	private boolean mIsTouched = false;
	private GeoPoint mLastCenterPosition;
	private int mLastZoomLevel;
	private MyMapView.OnChangeListener mChangeListener = null;
	
	public interface OnChangeListener {
		public void onChange(MapView view, GeoPoint newCenter,
				GeoPoint oldCenter, int newZoom, int oldZoom);
	}
	
	private Runnable mOnChangeTask = new Runnable() {
		public void run() {
			if (mChangeListener != null) {
				mChangeListener.onChange(mMyMapView, getMapCenter(),
						mLastCenterPosition, getZoomLevel(), mLastZoomLevel);
			}
			mLastCenterPosition = getMapCenter();
			mLastZoomLevel = getZoomLevel();
		}
	};
	

	public MyMapView(Context context, String apiKey) {
		super(context, apiKey);
		init();
	}

	public MyMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MyMapView(Context context, AttributeSet attrs, int defStyle)	{
		super(context, attrs, defStyle);
		init();
	}

	private void init()	{
		mMyMapView = this;
		mLastCenterPosition = this.getMapCenter();
		mLastZoomLevel = this.getZoomLevel();
	}
	
	public void setOnChangeListener(MyMapView.OnChangeListener listener) {
		mChangeListener = listener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)	{		
		// Set touch internal
		mIsTouched = (ev.getAction() != MotionEvent.ACTION_UP);
		
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			long thisTime = System.currentTimeMillis();
			if (thisTime - lastTouchTime < ViewConfiguration.getDoubleTapTimeout()) {
				// Double tap
				this.getController().zoomInFixing((int) ev.getX(), (int) ev.getY());
				lastTouchTime = -1;
			} else {
				// Too slow
				lastTouchTime = thisTime;
			}
		}
		
		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll()	{
		super.computeScroll();

		// Check for change
		if (isSpanChange() || isZoomChange()) {
			// If computeScroll called before timer counts down we should drop it and 
			// start counter over again
			resetMapChangeTimer();
		}
	}	
	
	private void resetMapChangeTimer() {
		MyMapView.this.removeCallbacks(mOnChangeTask);
		MyMapView.this.postDelayed(mOnChangeTask, mEventsTimeout);
	}
	
	private boolean isSpanChange() {
		return !mIsTouched && !getMapCenter().equals(mLastCenterPosition);
	}

	private boolean isZoomChange() {
		return (getZoomLevel() != mLastZoomLevel);
	}
	
}