package dk.ilios.influencecounter;
/**
 * Activity that controls a single player Influence Counter
 * 
 * @author Christian Melchior <christian@ilios.dk>
 */
import java.util.ArrayList;

import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import dk.ilios.influencecounter.history.GamesListAdapter;
import dk.ilios.influencecounter.history.GamesListCursorLoader;
import dk.ilios.influencecounter.views.OutlinedTextView;

public class SinglePlayerFragment extends Fragment implements LoaderCallbacks<Cursor> {

	private static final int LOADER_ID = 0x01;
	
	private MainActivity mParent;
	
	private int mInfluence = 25;
	private OutlinedTextView mCounterView;
	private View mTopbar;
	private View mBottombar;
	private View mHistoryContainer;
	
	private View mUpArrow;
	private View mDownArrow;

	private boolean mIsAnimationInProgress;
	private int currentStyle;
	private ArrayList<StyleTemplate> styles = new ArrayList<StyleTemplate>();
	
	// History properties
	private boolean mIsHistoryVisible;
	private ViewPager mPager;
	private GamesListAdapter mAdapter;
	private TextView mPageNumber;
	private Button mDeleteGameButton;
	private Button mDeleteAllButton;
	private TextView mEmptyHistoryMsg;
	private Cursor mCurrentCursor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mParent  = (MainActivity) getActivity();
		mAdapter = new GamesListAdapter(getFragmentManager());
		GameTracker.setHistoryListAdapter(this);
		
		currentStyle = mParent.getSinglePlayerTheme() - 1;

		styles.add(new StyleTemplate(R.drawable.warlords_top, R.drawable.warlords_bottom));
		styles.add(new StyleTemplate(R.drawable.banker_top, R.drawable.banker_bottom));
		styles.add(new StyleTemplate(R.drawable.rogue_top, R.drawable.rogue_bottom));
		styles.add(new StyleTemplate(R.drawable.arcanist_top, R.drawable.arcanist_bottom));
		styles.add(new StyleTemplate(R.drawable.gearsmith_top, R.drawable.gearsmith_bottom));

		getLoaderManager().initLoader(LOADER_ID, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.single_player_view, container, false);
		mInfluence = mParent.getDefaultStartingInfluencePlayer1();
		
		// Set reference to views
		initHistory(v);

		mCounterView = (OutlinedTextView) v.findViewById(R.id.counter);
		mCounterView.setText(new Integer(mInfluence).toString());

		mTopbar = v.findViewById(R.id.top_bar);
		mBottombar = v.findViewById(R.id.control_bar);
		mUpArrow = v.findViewById(R.id.up_arrow);
		mDownArrow = v.findViewById(R.id.down_arrow);
		
		
		// Set event handlers
		v.findViewById(R.id.increase_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mInfluence++;
				GameTracker.setInfluence(0, mInfluence);
				updateCounter();
			}
		});

		v.findViewById(R.id.increase_influence_button).setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!mParent.showHintArrowsForSinglePlayer()) return false;
				
				switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mUpArrow.setVisibility(View.VISIBLE);
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					mUpArrow.setVisibility(View.GONE);
					break;
				default: /* Ignore */
				}

				return false;
			}
		});

		v.findViewById(R.id.history_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				toggleHistory();
			}
		});
		
		v.findViewById(R.id.decrease_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mInfluence--;
				GameTracker.setInfluence(0, mInfluence);
				updateCounter();
			}
		});

		v.findViewById(R.id.decrease_influence_button).setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!mParent.showHintArrowsForSinglePlayer()) return false;

				switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mDownArrow.setVisibility(View.VISIBLE);
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					mDownArrow.setVisibility(View.GONE);
					break;
				default: /* Ignore */
				}

				return false;
			}
		});

		v.findViewById(R.id.refresh_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mInfluence = ((MainActivity) getActivity()).getDefaultStartingInfluencePlayer1();
				GameTracker.startGame(mInfluence, 0);
				updateCounter();
			}
		});
		
		v.findViewById(R.id.style_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggleStyle();
			}
		});
		
		toggleStyle();
		return v;
	}

	private void initHistory(View v) {
		mHistoryContainer = v.findViewById(R.id.history);
		
		mPageNumber = (TextView) mHistoryContainer.findViewById(R.id.page_number);
		mPageNumber.setText("");
		
		mEmptyHistoryMsg = (TextView) mHistoryContainer.findViewById(R.id.empty_history_message);
		mEmptyHistoryMsg.setVisibility(View.GONE);
		
		mPager = (ViewPager) v.findViewById(R.id.history_pager);
		new setAdapterTask().execute(); // Fix to avoid crash when nesting fragments
		mPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				if (mAdapter != null) {
					mPageNumber.setText((position+1) + "/" + mAdapter.getCount());
				}
			}
			
			@Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
			@Override public void onPageScrollStateChanged(int state) {}
		});
		
		// Buttons
		mDeleteGameButton = (Button) mHistoryContainer.findViewById(R.id.delete_game_button);
		mDeleteGameButton.setOnTouchListener(toggleButtonTextShadow); // Shadow state changes not support in XML
		mDeleteGameButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAdapter.getCount() > 0) {
					int gameId = ((GameHistoryFragment) mAdapter.getItem(mPager.getCurrentItem())).getGameId();
					GameTracker.deleteGame(gameId);
				
//					if (mAdapter.getCount() == 1) {
//						mPager.setCurrentItem(0);
//					} else if (mPager.getCurrentItem() == 0) {
//						mPager.setCurrentItem(mPager.getCurrentItem() + 1);
//					} else {
//						mPager.setCurrentItem(mPager.getCurrentItem() - 1);
//					}
				}
			}
		});
		
		
		
		
		mDeleteAllButton = (Button) mHistoryContainer.findViewById(R.id.delete_history_button);
		mDeleteAllButton.setOnTouchListener(toggleButtonTextShadow); // Shadow state changes not support in XML
		mDeleteAllButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				GameTracker.clearHistory();
			}
		});
	}
	
	private class setAdapterTask extends AsyncTask<Void,Void,Void>{
	      protected Void doInBackground(Void... params) {
	            return null;
	        }

	        @Override
	        protected void onPostExecute(Void result) {
	        	mPager.setAdapter(mAdapter);
	        	int selected = mAdapter.getCount() > 0 ? mAdapter.getCount() - 1 : 0;
	        	mPager.setCurrentItem(selected);
	        }
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setColors();
	}

	@Override	
	public void onDestroy() {
		getLoaderManager().destroyLoader(LOADER_ID);
		if (mCurrentCursor != null && !mCurrentCursor.isClosed()) {
			mCurrentCursor.close();
		}
		
		super.onDestroy();
	}
	
	
	private void updateCounter() {
		mCounterView.setText(new Integer(mInfluence).toString());
	}
	
	/**
	 * Switch to the next style
	 */
	private void toggleStyle() {
		currentStyle = (currentStyle + 1) % styles.size();
		mParent.setSinglePlayerTheme(currentStyle);
		mTopbar.setBackgroundDrawable(getResources().getDrawable(styles.get(currentStyle).top));
		mBottombar.setBackgroundDrawable(getResources().getDrawable(styles.get(currentStyle).bottom));
	}

	/**
	 * Set configured colores
	 */
	public void setColors() {
		mCounterView.setTextColor(mParent.getTextColor());
		mCounterView.setGlowColor(mParent.getGlowColor());
		mCounterView.setGlowEnabled(mParent.isTextGlowEnabled());
		mCounterView.setBorderColor(mParent.getBorderColor());
		mCounterView.setBorderEnabled(mParent.isTextBorderEnabled());
	}

	/**
	 * 
	 */
	private void toggleHistory() {
		if (mIsAnimationInProgress) return;
		
		if (mIsHistoryVisible) {
			hideHistory();
		} else {
			showHistory();
		}
	}
	
	private void showHistory() {
		Animation anim = AnimationUtils.loadAnimation(mParent, R.anim.show_history);
		anim.setAnimationListener(new AnimationListener() {
			
			@Override public void onAnimationStart(Animation animation) {}
			@Override public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mIsAnimationInProgress = false;
				mIsHistoryVisible = true;
				mHistoryContainer.setVisibility(View.VISIBLE);
			}
		});

		mHistoryContainer.setAnimation(anim);
		mParent.setVisibleHistoryContainer(mHistoryContainer);
		mIsAnimationInProgress = true;
		anim.start();
	}
	
	private void hideHistory() {
		Animation anim = AnimationUtils.loadAnimation(mParent, R.anim.hide_history);
		anim.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mIsAnimationInProgress = false;
				mIsHistoryVisible = false;
				mHistoryContainer.setVisibility(View.INVISIBLE);
			}
		});
		mHistoryContainer.setAnimation(anim);

		mParent.setVisibleHistoryContainer(null);
		mIsAnimationInProgress = true;
		anim.start();
	}
	
	
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	//Bug fix: http://code.google.com/p/android/issues/detail?id=19917
    	outState.putString("bugFix", "bugFix");
    	super.onSaveInstanceState(outState);
    }
    
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new GamesListCursorLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (mCurrentCursor != null) {
			mCurrentCursor.unregisterContentObserver(observer);
		}

		mCurrentCursor = data;
		mCurrentCursor.registerContentObserver(observer);
		Cursor c = mAdapter.swapCursor(mCurrentCursor);
		if (c != null && !c.isClosed()) {
			c.close();
		}
		
		mPager.setCurrentItem(mAdapter.getCount() - 1);
		mAdapter.notifyDataSetChanged();
		
		// Update counter
		int currentPage = (mAdapter.getCount() > 0) ? mPager.getCurrentItem() + 1 : 0;
		mPageNumber.setText(currentPage + "/" + mAdapter.getCount());

		// Show empty list message if needed
		if (mCurrentCursor.getCount() == 0) {
			mEmptyHistoryMsg.setVisibility(View.VISIBLE);
		} else {
			mEmptyHistoryMsg.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (mAdapter != null) {
			Cursor c = mAdapter.swapCursor(null);
			if (c != null && !c.isClosed()) {
				c.close();
			}
			mAdapter.notifyDataSetChanged();
		}
	}
	
	ContentObserver observer = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			getLoaderManager().restartLoader(LOADER_ID, null, SinglePlayerFragment.this);
		}
	};
	
	OnTouchListener toggleButtonTextShadow = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			TextView button = (TextView) v;
			switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				button.setShadowLayer(3, -5, 0, getResources().getColor(R.color.button_shadow_color));
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				button.setShadowLayer(0,0,0,0);
				break;
			}
			return false;
		}
	};
	
}
