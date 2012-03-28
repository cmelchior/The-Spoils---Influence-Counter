package dk.ilios.influencecounter.pages;
/**
 * Activity that controls a single player Influence Counter
 * 
 * @author Christian Melchior <christian@ilios.dk>
 */
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import dk.ilios.influencecounter.GameTracker;
import dk.ilios.influencecounter.MainActivity;
import dk.ilios.influencecounter.PlayType;
import dk.ilios.influencecounter.R;
import dk.ilios.influencecounter.StyleTemplate;
import dk.ilios.influencecounter.views.OutlinedTextView;

public class SinglePlayerFragment extends HistoryFragment {

	private MainActivity mParent;
	
	private int mInfluence = 25;
	private OutlinedTextView mCounterView;
	private View mTopbar;
	private View mBottombar;
	
	private View mUpArrow;
	private View mDownArrow;

	private int currentStyle;
	private ArrayList<StyleTemplate> styles = new ArrayList<StyleTemplate>();

	public SinglePlayerFragment(Activity activity) {
		super(activity);
	}

	@Override
	public void onCreate(Context context) {
		super.onCreate(context);
		
		mParent  = (MainActivity) getActivity();
		
		currentStyle = mParent.getSinglePlayerTheme() - 1;

		styles.add(new StyleTemplate(R.drawable.warlords_top, R.drawable.warlords_bottom));
		styles.add(new StyleTemplate(R.drawable.banker_top, R.drawable.banker_bottom));
		styles.add(new StyleTemplate(R.drawable.rogue_top, R.drawable.rogue_bottom));
		styles.add(new StyleTemplate(R.drawable.arcanist_top, R.drawable.arcanist_bottom));
		styles.add(new StyleTemplate(R.drawable.gearsmith_top, R.drawable.gearsmith_bottom));

		mInfluence = mParent.getDefaultStartingInfluencePlayer1();
	}
	
	@Override
	public View onCreateView() {
		final View v = mParent.getLayoutInflater().inflate(R.layout.single_player_view, null);
		
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
				GameTracker.setInfluence(PlayType.SINGLE_PLAYER, 0, mInfluence);
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
				GameTracker.setInfluence(PlayType.SINGLE_PLAYER, 0, mInfluence);
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
		mTopbar.setBackgroundDrawable(mParent.getResources().getDrawable(styles.get(currentStyle).top));
		mBottombar.setBackgroundDrawable(mParent.getResources().getDrawable(styles.get(currentStyle).bottom));
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

	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public PlayType getPlayType() {
		return PlayType.SINGLE_PLAYER;
	}

	@Override
	public int getLoaderId() {
		return 0x01;
	}
}
