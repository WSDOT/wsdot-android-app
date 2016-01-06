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

package gov.wa.wsdot.android.wsdot.shared;

import java.io.Serializable;

public class ForecastItem implements Serializable {
	private static final long serialVersionUID = 3654512237762813454L;
	private String day;
	private String forecastText;
	private Integer weatherIcon;
	
	public String getDay() {
		return day;
	}
	public void setDay(String day) {
		this.day = day;
	}
	public String getForecastText() {
		return forecastText;
	}
	public void setForecastText(String forecastText) {
		this.forecastText = forecastText;
	}
	public Integer getWeatherIcon() {
		return weatherIcon;
	}
	public void setWeatherIcon(Integer weatherIcon) {
		this.weatherIcon = weatherIcon;
	}
}
