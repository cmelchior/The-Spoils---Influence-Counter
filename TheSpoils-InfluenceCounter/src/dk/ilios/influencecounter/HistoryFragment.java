package dk.ilios.influencecounter;
/**
 * Encapsulation of the history view for single and two player counter views.
 * 
 * @author Christian Melchior <christian@ilios.dk>
 */
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import dk.ilios.influencecounter.history.GamesListAdapter;
import dk.ilios.influencecounter.history.GamesListCursorLoader;

public abstract class HistoryFragment extends Fragment implements LoaderCallbacks<Cursor> {

	protected int LOADER_ID = 0x00;

	private MainActivity mParent;
	private View mHistoryContainer;
	private View mHistoryContainerContentWrapper;
	private boolean mIsAnimationInProgress;

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
		mParent = (MainActivity) getActivity();
		mAdapter = new GamesListAdapter(getFragmentManager());
		getLoaderManager().initLoader(LOADER_ID, null, this);
	}
	
	public abstract PlayType getPlayType();
	
	protected void initHistory(View v) {
		mHistoryContainer = (getPlayType() == PlayType.SINGLE_PLAYER) ? v.findViewById(R.id.history) : v.findViewById(R.id.history_twoplayer);
		mHistoryContainerContentWrapper = mHistoryContainer.findViewById(R.id.history_wrapper);
		
		mPageNumber = (TextView) mHistoryContainer.findViewById(R.id.page_number);
		mPageNumber.setText("");
		
		mEmptyHistoryMsg = (TextView) mHistoryContainer.findViewById(R.id.empty_history_message);
		mEmptyHistoryMsg.setVisibility(View.GONE);

		// Apparently having two view pagers with the same ID even if they are
		// in two different fragments result in no views being shown in the 
		// second view pager.
		mPager = (getPlayType() == PlayType.SINGLE_PLAYER) ? (ViewPager) v.findViewById(R.id.history_pager) : (ViewPager) v.findViewById(R.id.history_pager_twoplayer);
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
				}
			}
		});
		
		mDeleteAllButton = (Button) mHistoryContainer.findViewById(R.id.delete_history_button);
		mDeleteAllButton.setOnTouchListener(toggleButtonTextShadow); // Shadow state changes not support in XML
		mDeleteAllButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int players = 1;
				if (getPlayType() == PlayType.TWO_PLAYER) {
					players = 2;
				}
				
				GameTracker.clearHistory(players);
			}
		});
	}
	
	/**
	 * 
	 */
	protected void toggleHistory() {
		if (mIsAnimationInProgress) return;
		
		if (mIsHistoryVisible) {
			hideHistory();
		} else {
			showHistory();
		}
	}
	
	private void showHistory() {
		Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.show_history);
		anim.setAnimationListener(new AnimationListener() {
			
			@Override public void onAnimationStart(Animation animation) {}
			@Override public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mIsAnimationInProgress = false;
				mIsHistoryVisible = true;
				mHistoryContainer.setVisibility(View.VISIBLE);
			
				Animation anim1 = AnimationUtils.loadAnimation(getActivity(), R.anim.show_history_content);
				anim1.setAnimationListener(new AnimationListener() {
					@Override public void onAnimationStart(Animation animation) {}
					@Override public void onAnimationRepeat(Animation animation) {}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						mHistoryContainerContentWrapper.setVisibility(View.VISIBLE);
					}
				});
				mHistoryContainerContentWrapper.setAnimation(anim1);
				anim1.start();			
			
			}
		});

		mHistoryContainer.setAnimation(anim);
		mParent.setVisibleHistoryContainer(mHistoryContainer);
		mIsAnimationInProgress = true;
		anim.start();
	}
	
	private void hideHistory() {
		
		Animation anim1 = AnimationUtils.loadAnimation(getActivity(), R.anim.hide_history_content);
		anim1.setAnimationListener(new AnimationListener() {
			@Override public void onAnimationStart(Animation animation) {}
			@Override public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mHistoryContainerContentWrapper.setVisibility(View.INVISIBLE);
				Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.hide_history);
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
				anim.start();
			}
		});
		
		mHistoryContainerContentWrapper.setAnimation(anim1);
		mIsAnimationInProgress = true;
		anim1.start();			
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
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new GamesListCursorLoader(getActivity(), getPlayType());
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
				c.unregisterContentObserver(observer);
			}
			mAdapter.notifyDataSetChanged();
		}
	}
	
	ContentObserver observer = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			getLoaderManager().restartLoader(LOADER_ID, null, HistoryFragment.this);
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
