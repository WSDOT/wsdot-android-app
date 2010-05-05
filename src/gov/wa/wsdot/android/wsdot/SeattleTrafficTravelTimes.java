package gov.wa.wsdot.android.wsdot;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class SeattleTrafficTravelTimes extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("This is the Seattle traffic travel times tab");
        setContentView(textview);
    }
}