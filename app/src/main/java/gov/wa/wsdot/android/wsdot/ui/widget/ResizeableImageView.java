/*
 * Copyright (c) 2017 Washington State Department of Transportation
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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ResizeableImageView extends ImageView {

	public ResizeableImageView(Context context) {
		super(context);
	}

    public ResizeableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResizeableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }	
	
	private Drawable mDrawable;

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		if (mDrawable != null) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = width * mDrawable.getIntrinsicHeight() / mDrawable.getIntrinsicWidth();
            setMeasuredDimension(width, height);
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);				
		}

	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		mDrawable = drawable;
		super.setImageDrawable(drawable);
	}	
}
