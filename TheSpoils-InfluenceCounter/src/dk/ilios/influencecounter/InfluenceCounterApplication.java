package dk.ilios.influencecounter;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class InfluenceCounterApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);	// Initialize if not done already
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	long influenceChangeGroupingTimer = Integer.parseInt(prefs.getString("influce_change_group_timer", "2000"));
		
		GameTracker.initialize(getApplicationContext(), influenceChangeGroupingTimer);
	}
}
