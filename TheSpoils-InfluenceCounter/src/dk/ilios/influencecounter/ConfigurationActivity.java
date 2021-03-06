package dk.ilios.influencecounter;
/**
 * Activity that controls the configuration page.
 * 
 * @author Christian Melchior <christian@ilios.dk>
 */
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class ConfigurationActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        // Show value as summary for starting influence
        Preference pref = findPreference("default_starting_influence");
        pref.setSummary((pref.getSharedPreferences().getString("default_starting_influence", "0")));
        pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary((String) newValue);
				return true;
			}
		});

        Preference pref2 = findPreference("default_starting_influence_player2");
        pref2.setSummary((pref.getSharedPreferences().getString("default_starting_influence_player2", "0")));
        pref2.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary((String) newValue);
				return true;
			}
		});

        Preference pref3 = findPreference("history_grouping_timer");
        pref3.setSummary(createHistoryTimerSummary((pref.getSharedPreferences().getString("history_grouping_timer", "2"))));
        pref3.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(createHistoryTimerSummary((String) newValue));
				return true;
			}
		});
    }

    private String createHistoryTimerSummary(String value) {
    	return String.format(getText(R.string.config_history_grouping_timer_summary).toString(), value);
    }
    
    
}
