package dk.ilios.influencecounter;
/**
 * Activity that controls a single player Influence Counter
 * 
 * @author Christian Melchior <christian@ilios.dk>
 */
import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import dk.ilios.influencecounter.views.OutlinedTextView;

public class SinglePlayerFragment extends Fragment {

	private MainActivity mParent;
	
	private int mInfluence = 25;
	private OutlinedTextView mCounterView;
	private View mTopbar;
	private View mBottombar;
	private View mHistoryContainer;
	
	private View mUpArrow;
	private View mDownArrow;

	private boolean mIsAnimationInProgress;
	private boolean mIsHistoryVisible;
	private int currentStyle;
	private ArrayList<StyleTemplate> styles = new ArrayList<StyleTemplate>();
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mParent  = (MainActivity) getActivity();
		currentStyle = mParent.getSinglePlayerTheme() - 1;

		styles.add(new StyleTemplate(R.drawable.warlords_top, R.drawable.warlords_bottom));
		styles.add(new StyleTemplate(R.drawable.banker_top, R.drawable.banker_bottom));
		styles.add(new StyleTemplate(R.drawable.rogue_top, R.drawable.rogue_bottom));
		styles.add(new StyleTemplate(R.drawable.arcanist_top, R.drawable.arcanist_bottom));
		styles.add(new StyleTemplate(R.drawable.gearsmith_top, R.drawable.gearsmith_bottom));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.single_player_view, container, false);
		mInfluence = mParent.getDefaultStartingInfluence();
		
		// Set reference to views
		mHistoryContainer = v.findViewById(R.id.history);

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
				mInfluence = ((MainActivity) getActivity()).getDefaultStartingInfluence();
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

	
	@Override
	public void onResume() {
		super.onResume();
		setColors();
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
				mIsHistoryVisible = true;
				mHistoryContainer.setVisibility(View.VISIBLE);
			}
		});
		mHistoryContainer.setAnimation(anim);

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
}
