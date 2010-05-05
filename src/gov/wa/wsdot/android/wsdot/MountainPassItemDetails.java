package gov.wa.wsdot.android.wsdot;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MountainPassItemDetails extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mountainpass_item_details);
		Bundle b = getIntent().getExtras();
		
		String weatherCondition;
		String temperatureInFahrenheit;
		
		weatherCondition = b.getString("WeatherCondition");
		temperatureInFahrenheit = b.getString("TemperatureInFahrenheit");
		
		if (weatherCondition.equals("")) weatherCondition = "Not available";
		if (temperatureInFahrenheit.equals("null")) {
			temperatureInFahrenheit = "Not available";
		} else {
			temperatureInFahrenheit = temperatureInFahrenheit + "¡F";
		}
	
		((TextView)findViewById(R.id.MountainPassName)).setText(b.getString("MountainPassName"));
		((TextView)findViewById(R.id.WeatherCondition)).setText(weatherCondition);
		((TextView)findViewById(R.id.TemperatureInFahrenheit)).setText(temperatureInFahrenheit);
		((TextView)findViewById(R.id.ElevationInFeet)).setText(b.getString("ElevationInFeet") + " ft");
		((TextView)findViewById(R.id.RoadCondition)).setText(b.getString("RoadCondition"));
		((TextView)findViewById(R.id.heading_RestrictionOneTravelDirection)).setText("Restrictions " + b.getString("RestrictionOneTravelDirection") + ":");
		((TextView)findViewById(R.id.RestrictionOneText)).setText(b.getString("RestrictionOneText"));
		((TextView)findViewById(R.id.heading_RestrictionTwoTravelDirection)).setText("Restrictions " + b.getString("RestrictionTwoTravelDirection") + ":");
		((TextView)findViewById(R.id.RestrictionTwoText)).setText(b.getString("RestrictionTwoText"));
	}
}
