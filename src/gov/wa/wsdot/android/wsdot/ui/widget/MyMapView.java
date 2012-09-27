/*
 * Copyright 2012 Bricolsoft Consulting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Reference:
 * 
 * Extending MapView to Add a Change Event
 * 
 * http://bricolsoftconsulting.com/2011/10/31/extending-mapview-to-add-a-change-event/
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
	private long mEventsTimeout = 100L;
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