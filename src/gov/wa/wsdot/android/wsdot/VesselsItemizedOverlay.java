/*
 * Copyright (c) 2010 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class VesselsItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	Context mContext;
	private Rect touchableBounds = new Rect();
    private static final int MIN_TOUCHABLE_WIDTH  = 50;
    private static final int MIN_TOUCHABLE_HEIGHT = 50;	
	
	public VesselsItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	public VesselsItemizedOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
	}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    setLastFocusedIndex(-1);
	    populate();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	@Override
	protected boolean onTap(int index) {
		OverlayItem item = mOverlays.get(index);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.vesselwatch_dialog, null);
		((TextView)layout.findViewById(R.id.VesselName)).setText(item.getTitle());
		((TextView)layout.findViewById(R.id.VesselDetails)).setText(item.getSnippet());
		
		builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		builder.setView(layout);
		AlertDialog alertDialog = builder.create();
		alertDialog.show();		
		
		return true;
	}

	@Override
	protected boolean hitTest(OverlayItem item, Drawable marker, int hitX, int hitY) {
        Rect bounds = marker.getBounds();
        int width = bounds.width();
        int height = bounds.height();
        int centerX = bounds.centerX();
        int centerY = bounds.centerY();
        int touchWidth = Math.max(MIN_TOUCHABLE_WIDTH, width);
        int touchLeft = centerX - touchWidth / 2;
        int touchHeight = Math.max(MIN_TOUCHABLE_HEIGHT, height);
        int touchTop = centerY - touchHeight / 2;

        touchableBounds.set(touchLeft, touchTop, touchLeft + touchWidth, touchTop + touchHeight);

        return touchableBounds.contains(hitX, hitY); 
	}
}
