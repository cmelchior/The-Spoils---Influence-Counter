package dk.ilios.influencecounter.pages;
/**
 * Activity that controls Influence counters for both players
 * 
 * @author Christian Melchior <christian@ilios.dk>
 */
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import dk.ilios.influencecounter.GameTracker;
import dk.ilios.influencecounter.MainActivity;
import dk.ilios.influencecounter.PlayType;
import dk.ilios.influencecounter.R;
import dk.ilios.influencecounter.StyleTemplate;
import dk.ilios.influencecounter.views.OutlinedTextView;

public class TwoPlayerFragment extends HistoryFragment {

	private MainActivity mParent;
	
	private int mInfluenceTop = 0;
	private int mInfluenceBottom = 0;
	private OutlinedTextView mCounterTop;
	private OutlinedTextView mCounterBottom;

	private View mTopbarTop;
	private View mBottombarTop;

	private View mTopbarBottom;
	private View mBottombarBottom;
	
	private int currentStyleTop;
	private int currentStyleBottom;
	
	private ArrayList<StyleTemplate> styles = new ArrayList<StyleTemplate>();
	private ArrayList<StyleTemplate> stylesReversed = new ArrayList<StyleTemplate>();
	
	public TwoPlayerFragment(Activity activity) {
		super(activity);
	}
	
	@Override
	public void onCreate(Context context) {
		super.onCreate(context);
		
		mParent  = (MainActivity) getActivity();
		currentStyleTop = mParent.getTwoPlayerTopTheme() - 1;
		currentStyleBottom = mParent.getTwoPlayerBottomTheme() - 1;
		
		styles.add(new StyleTemplate(R.drawable.warlords_top, R.drawable.warlords_bottom));
		styles.add(new StyleTemplate(R.drawable.banker_top, R.drawable.banker_bottom));
		styles.add(new StyleTemplate(R.drawable.rogue_top, R.drawable.rogue_bottom));
		styles.add(new StyleTemplate(R.drawable.arcanist_top, R.drawable.arcanist_bottom));
		styles.add(new StyleTemplate(R.drawable.gearsmith_top, R.drawable.gearsmith_bottom));

		stylesReversed.add(new StyleTemplate(R.drawable.warlords_top_reversed, R.drawable.warlords_bottom_reversed));
		stylesReversed.add(new StyleTemplate(R.drawable.banker_top_reversed, R.drawable.banker_bottom_reversed));
		stylesReversed.add(new StyleTemplate(R.drawable.rogue_top_reversed, R.drawable.rogue_bottom_reversed));
		stylesReversed.add(new StyleTemplate(R.drawable.arcanist_top_reversed, R.drawable.arcanist_bottom_reversed));
		stylesReversed.add(new StyleTemplate(R.drawable.gearsmith_top_reversed, R.drawable.gearsmith_bottom_reversed));
		
		mInfluenceTop = mParent.getDefaultStartingInfluencePlayer1();
		mInfluenceBottom = mParent.getDefaultStartingInfluencePlayer2();
	}

	@Override
	public View onCreateView() {
		View v = mParent.getLayoutInflater().inflate(R.layout.two_player_view, null);
		initHistory(v);
		
		// Set reference to views
		mTopbarTop = v.findViewById(R.id.top_player_top_bar);
		mBottombarTop = v.findViewById(R.id.top_player_control_bar);
		mTopbarBottom = v.findViewById(R.id.bottom_player_top_bar);
		mBottombarBottom = v.findViewById(R.id.bottom_player_control_bar);
		
		mCounterTop = (OutlinedTextView) v.findViewById(R.id.top_player_counter);
		mCounterTop.setText(new Integer(mInfluenceTop).toString());

		mCounterBottom = (OutlinedTextView) v.findViewById(R.id.bottom_player_counter);
		mCounterBottom.setText(new Integer(mInfluenceBottom).toString());
		
		// Set event handlers
		v.findViewById(R.id.top_player_increase_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isHistoryVisible()) return;

				if (GameTracker.getCurrentGameId(PlayType.TWO_PLAYER) == GameTracker.NO_GAME_ID) {
					GameTracker.startGame(mInfluenceBottom, mInfluenceTop, getTimestampForAutomaticallyStartedGame());
				}

				mInfluenceTop++;
				GameTracker.setInfluence(PlayType.TWO_PLAYER, 1, mInfluenceTop);
				updateTopCounter();
			}
		});

		v.findViewById(R.id.bottom_player_increase_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isHistoryVisible()) return;

				if (GameTracker.getCurrentGameId(PlayType.TWO_PLAYER) == GameTracker.NO_GAME_ID) {
					GameTracker.startGame(mInfluenceBottom, mInfluenceTop, getTimestampForAutomaticallyStartedGame());
				}

				mInfluenceBottom++;
				GameTracker.setInfluence(PlayType.TWO_PLAYER, 0, mInfluenceBottom);
				updateBottomCounter();
			}
		});

		v.findViewById(R.id.top_player_decrease_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isHistoryVisible()) return;

				if (GameTracker.getCurrentGameId(PlayType.TWO_PLAYER) == GameTracker.NO_GAME_ID) {
					GameTracker.startGame(mInfluenceBottom, mInfluenceTop, getTimestampForAutomaticallyStartedGame());
				}

				mInfluenceTop--;
				GameTracker.setInfluence(PlayType.TWO_PLAYER, 1, mInfluenceTop);
				updateTopCounter();
			}
		});

		v.findViewById(R.id.bottom_player_decrease_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isHistoryVisible()) return;

				if (GameTracker.getCurrentGameId(PlayType.TWO_PLAYER) == GameTracker.NO_GAME_ID) {
					GameTracker.startGame(mInfluenceBottom, mInfluenceTop, getTimestampForAutomaticallyStartedGame());
				}

				mInfluenceBottom--;
				GameTracker.setInfluence(PlayType.TWO_PLAYER, 0, mInfluenceBottom);
				updateBottomCounter();
			}
		});

		
		v.findViewById(R.id.top_player_refresh_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				refresh();
			}
		});

		v.findViewById(R.id.bottom_player_refresh_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				refresh();
			}
		});

		v.findViewById(R.id.top_player_style_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggleTopStyle();
			}
		});


		v.findViewById(R.id.bottom_player_style_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggleBottomStyle();
			}
		});
		
		v.findViewById(R.id.history_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				toggleHistory();
			}
		});
		
		toggleBottomStyle();
		toggleTopStyle();

		return v;
	}

	private void refresh() {
		mInfluenceTop = ((MainActivity) getActivity()).getDefaultStartingInfluencePlayer2();
		mInfluenceBottom = ((MainActivity) getActivity()).getDefaultStartingInfluencePlayer1();
		
		GameTracker.startGame(mInfluenceBottom, mInfluenceTop, System.currentTimeMillis());
		updateTopCounter();
		updateBottomCounter();
	}
	
	
	@Override
	public void onResume() {
		setColors();
	}

	private void updateTopCounter() {
		mCounterTop.setText(new Integer(mInfluenceTop).toString());
	}
	
	private void updateBottomCounter() {
		mCounterBottom.setText(new Integer(mInfluenceBottom).toString());
	}
	
	/**
	 * Set configured colores
	 */
	public void setColors() {
		mCounterTop.setTextColor(mParent.getTextColor());
		mCounterTop.setGlowEnabled(mParent.isTextGlowEnabled());
		mCounterTop.setGlowColor(mParent.getGlowColor());
		mCounterTop.setBorderColor(mParent.getBorderColor());
		mCounterTop.setBorderEnabled(mParent.isTextBorderEnabled());

		mCounterBottom.setTextColor(mParent.getTextColor());
		mCounterBottom.setGlowEnabled(mParent.isTextGlowEnabled());
		mCounterBottom.setGlowColor(mParent.getGlowColor());
		mCounterBottom.setBorderColor(mParent.getBorderColor());
		mCounterBottom.setBorderEnabled(mParent.isTextBorderEnabled());
	}

	
	private void toggleTopStyle() {
		currentStyleTop = (currentStyleTop + 1) % stylesReversed.size();
		mParent.setTwoPlayerTopTheme(currentStyleTop);
		
		Drawable d = mParent.getResources().getDrawable(stylesReversed.get(currentStyleTop).top);
		d.setLevel(10000);
		mTopbarTop.setBackgroundDrawable(d);
		
		d = mParent.getResources().getDrawable(stylesReversed.get(currentStyleTop).bottom);
		d.setLevel(10000);
		mBottombarTop.setBackgroundDrawable(d);
	}
	
	private void toggleBottomStyle() {
		currentStyleBottom = (currentStyleBottom + 1) % styles.size();
		mParent.setTwoPlayerBottomTheme(currentStyleBottom);

		mTopbarBottom.setBackgroundDrawable(mParent.getResources().getDrawable(stylesReversed.get(currentStyleBottom).top));
		mBottombarBottom.setBackgroundDrawable(mParent.getResources().getDrawable(styles.get(currentStyleBottom).bottom));
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public PlayType getPlayType() {
		return PlayType.TWO_PLAYER;
	}

	@Override
	public int getLoaderId() {
		return 0x02;
	}
}
