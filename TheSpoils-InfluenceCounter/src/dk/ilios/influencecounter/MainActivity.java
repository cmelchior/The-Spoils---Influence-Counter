package dk.ilios.influencecounter;
/**
 * Main activity which primarily controls the view pager.
 * 
 * @author Christian Melchior
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import dk.ilios.influencecounter.views.DisableableViewPager;

public class MainActivity extends FragmentActivity {

	private static final int REQUEST_CODE_CONFIGURATION = 1;
	
	private SharedPreferences prefs;	
    private PagerViewsAdapter mAdapter;
    private DisableableViewPager mPager;
    
    private int mDefaultStartingInfluencePlayer1;
    private int mDefaultStartingInfluencePlayer2;

    private boolean mKeepScreenAlive;
    private int mLogTimer;
    private PowerManager.WakeLock mWakeLock;
    private boolean mSinglePlayerHintArrows;
    private boolean mTwoPlayerHintArrows; 
    private boolean mTextGlow;
    private boolean mBorder;

    // History
    private SQLiteDatabase mDb;		// Reference to the database connection for this activity
    private View mHistoryContainer; // Reference to a visible history container (if any)
    private int mHistoryContainerBottom; // Screen y-coordinate for history view
	private int mHistoryContainerTop; // Screen y-coordinate for history view
	
    // Colors
    private int mTextColor;
    private int mGlowColor;
    private int mBorderColor;
    
    // Themes
    private int mSinglePlayerTheme = 0;
    private int mTwoPlayerTopTheme = 0;
    private int mTwoPlayerBottomTheme = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);	// Initialize if not done already
    	prefs = PreferenceManager.getDefaultSharedPreferences(this);
        initializePreferences();
        initializeWakelock();
    
        mAdapter = new PagerViewsAdapter(getSupportFragmentManager());
        mPager = (DisableableViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
    	super.onStart();
    }
    
    @Override 
    protected void onStop() {
    	super.onStop();
    }
    
    private void initializePreferences() {
    	mDefaultStartingInfluencePlayer1 = Integer.parseInt(prefs.getString("default_starting_influence", "0"));
    	mDefaultStartingInfluencePlayer2 = Integer.parseInt(prefs.getString("default_starting_influence_player2", "0"));
    	mLogTimer = (int) Float.parseFloat(prefs.getString("history_grouping_timer", "2")) * 1000;
    	mKeepScreenAlive = prefs.getBoolean("wakelock", false);
    	mSinglePlayerHintArrows = prefs.getBoolean("single_player_hint_arrows", false);
    	mTwoPlayerHintArrows = prefs.getBoolean("two_player_hint_arrows", false);
    	mTextGlow = prefs.getBoolean("text_glow", true);
    	mTextColor = prefs.getInt("text_color", 0xffffffff);
    	mGlowColor = prefs.getInt("glow_color", 0xffffffbe);
    	mBorderColor = prefs.getInt("border_color", 0xff000000);
    	mBorder = prefs.getBoolean("text_border", true);
    	mSinglePlayerTheme = prefs.getInt("theme_single_player", 0);
    	mTwoPlayerTopTheme = prefs.getInt("theme_two_player_top", 1);
    	mTwoPlayerBottomTheme = prefs.getInt("theme_two_player_bottom", 2);
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
			GameTracker.initialize(this, mLogTimer);
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

	
	
	
//	@Override
//	public boolean dispatchTouchEvent(MotionEvent ev) {
//
//		// Only dispatch events to history view pager if it is visible and we
//		// don't hit any toolbars at the top or bottom of the page.
//		// 
//		// There is apparently some trouble with the ListView not getting some 
//		// events so dispatch to this view as well. Possible due to the viewpager
//		// eating some events.
//		if (mHistoryContainer != null) {
////			if (ev.getRawY() > mHistoryContainerTop && ev.getRawY() < mHistoryContainerBottom) {
//				mHistoryContainer.dispatchTouchEvent(ev);
//				return true;
////			}
//		}
//
//		return super.dispatchTouchEvent(ev);
//	}

	
	public void setVisibleHistoryContainer(View v) { 
		mHistoryContainer = v;

		if (mHistoryContainer != null) {
			mPager.setPagingEnabled(false);
			// Get size of bottom/top drawable so we know when not to dispatch
	        // events to the history view.
	        // All bottom drawables have the same size, so just use a random one.
	        int toolbarHeight = getResources().getDrawable(R.drawable.arcanist_bottom).getIntrinsicHeight(); 
			int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        
			mHistoryContainerBottom = screenHeight - toolbarHeight;
			mHistoryContainerTop = mHistoryContainerBottom - mHistoryContainer.getHeight();

		} else {
			mPager.setPagingEnabled(true);
			mHistoryContainerBottom = 0;
			mHistoryContainerTop = 0;
		}
	}
	
	public int getDefaultStartingInfluencePlayer1() {
		return mDefaultStartingInfluencePlayer1;
	}

	public int getDefaultStartingInfluencePlayer2() {
		return mDefaultStartingInfluencePlayer2;
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
	
	public boolean isTextBorderEnabled() {
		return mBorder;
	}

	public int getSinglePlayerTheme() {
		return mSinglePlayerTheme;
	}
	
	public int getTwoPlayerTopTheme() {
		return mTwoPlayerTopTheme;
	}
	
	public int getTwoPlayerBottomTheme() {
		return mTwoPlayerBottomTheme;
	}
	
	public void setSinglePlayerTheme(int theme) {
		mSinglePlayerTheme = theme;
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt("theme_single_player", theme);
        prefsEditor.commit();
	}
	
	public void setTwoPlayerTopTheme(int theme) {
		mTwoPlayerTopTheme = theme;
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt("theme_two_player_top", theme);
        prefsEditor.commit();
	}
	
	public void setTwoPlayerBottomTheme(int theme) {
		mTwoPlayerBottomTheme = theme;
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt("theme_two_player_bottom", theme);
        prefsEditor.commit();
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
