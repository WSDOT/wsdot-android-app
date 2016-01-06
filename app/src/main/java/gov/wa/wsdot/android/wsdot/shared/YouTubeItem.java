/*
 * Copyright (c) 2011 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot.shared;

import android.graphics.drawable.Drawable;

public class YouTubeItem {
	private String id;
	private String uploaded;
	private String title;
	private String description;
	private Drawable thumbNail;
	private String viewCount;
	private String thumbNailUrl;
	
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public void setUploaded(String uploaded) {
		this.uploaded = uploaded;
	}
	public String getUploaded() {
		return uploaded;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitle() {
		return title;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
	public void setThumbNail(Drawable thumbNail) {
		this.thumbNail = thumbNail;
	}
	public Drawable getThumbNail() {
		return thumbNail;
	}
	public void setViewCount(String viewCount) {
		this.viewCount = viewCount;
	}
	public String getViewCount() {
		return viewCount;
	}
	public String getThumbNailUrl() {
		return thumbNailUrl;
	}
	public void setThumbNailUrl(String thumbNailUrl) {
		this.thumbNailUrl = thumbNailUrl;
	}
}
