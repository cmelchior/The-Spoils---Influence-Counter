package dk.ilios.influencecounter;
/**
 * Main activity which primarily controls the view pager.
 * 
 * @author Christian Melchior
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity {

	private static final int REQUEST_CODE_CONFIGURATION = 1;
	
	private SharedPreferences prefs;	
    private PagerViewsAdapter mAdapter;
    private ViewPager mPager;
    
    private int mDefaultStartingInfluence;
    private boolean mKeepScreenAlive;
    private PowerManager.WakeLock mWakeLock;
     
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);	// Initialize if not done already
    	prefs = PreferenceManager.getDefaultSharedPreferences(this);
        initializePreferences();
        initializeWakelock();
        
        mAdapter = new PagerViewsAdapter(getSupportFragmentManager());

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
    }

    private void initializePreferences() {
    	mDefaultStartingInfluence = Integer.parseInt(prefs.getString("default_starting_influence", "0"));
    	mKeepScreenAlive = prefs.getBoolean("wakelock", false);
    }

    private void initializeWakelock() {
        if (mKeepScreenAlive) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "InfluenceCounterWakeLock");
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    	if (mKeepScreenAlive) {
    		mWakeLock.acquire();
    	}
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	if (mKeepScreenAlive) {
    		mWakeLock.release();
    	}
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// Re-initialize preferences
		if (requestCode == REQUEST_CODE_CONFIGURATION) {
			initializePreferences();
			initializeWakelock();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.clear();
		String menuTitle = getString(R.string.configuration);
		MenuItem menuItem = menu.add(0, 0, 0, menuTitle);
		menuItem.setEnabled(true);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Only 1 option, so just show config page
		Intent intent = new Intent();
		intent.setClass(this, ConfigurationActivity.class);
		startActivityForResult(intent, REQUEST_CODE_CONFIGURATION);
		return true;
	}

	public int getDefaultStartingInfluence() {
		return mDefaultStartingInfluence;
	}
	
	
/*******************************************************************************
 * PAGE ADAPTER                                                                *
 ******************************************************************************/	
	public static class PagerViewsAdapter extends FragmentPagerAdapter {
        public PagerViewsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2; // 2 pages - Single and two-player view
        }

        @Override
        public Fragment getItem(int position) {
        	if (position == 0) {
        		return new SinglePlayerFragment();
        		
        	} else if (position == 1) {
        		return new TwoPlayerFragment();
        	} 
        	
        	return null;
        }
    }
}
