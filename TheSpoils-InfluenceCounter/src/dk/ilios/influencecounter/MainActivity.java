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
    private boolean mSinglePlayerHintArrows;
    private boolean mTwoPlayerHintArrows; 
    private boolean mTextGlow;
    
    // Colors
    private int mTextColor;
    private int mGlowColor;
    private int mBorderColor;
    
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
    	mSinglePlayerHintArrows = prefs.getBoolean("single_player_hint_arrows", false);
    	mTwoPlayerHintArrows = prefs.getBoolean("two_player_hint_arrows", false);
    	mTextGlow = prefs.getBoolean("text_glow", true);
    	mTextColor = prefs.getInt("text_color", 0xffffffff);
    	mGlowColor = prefs.getInt("glow_color", 0xffffffbe);
    	mBorderColor = prefs.getInt("border_color", 0xff000000);
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
	
	public boolean showHintArrowsForSinglePlayer() {
		return mSinglePlayerHintArrows;
	}

	public boolean showHintArrowsForTwoPlayers() {
		return mTwoPlayerHintArrows;
	}

	public int getTextColor() {
		return mTextColor;
	}
	
	public boolean isTextGlowEnabled() {
		return mTextGlow;
	}
	
	public int getGlowColor() {
		return mGlowColor;
	}
	
	public int getBorderColor() {
		return mBorderColor;
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
