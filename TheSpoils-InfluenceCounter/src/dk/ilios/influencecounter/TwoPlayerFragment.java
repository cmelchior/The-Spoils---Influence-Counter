package dk.ilios.influencecounter;
/**
 * Activity that controls Influence counters for both players
 * 
 * @author Christian Melchior <christian@ilios.dk>
 */
import java.util.ArrayList;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import dk.ilios.influencecounter.views.OutlinedTextView;

public class TwoPlayerFragment extends HistoryFragment {

	protected int LOADER_ID = 0x02;
	
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
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		GameTracker.startGame(mInfluenceBottom, mInfluenceTop);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.two_player_view, container, false);
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
				mInfluenceTop++;
				GameTracker.setInfluence(PlayType.TWO_PLAYER, 1, mInfluenceTop);
				updateTopCounter();
			}
		});

		v.findViewById(R.id.bottom_player_increase_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mInfluenceBottom++;
				GameTracker.setInfluence(PlayType.TWO_PLAYER, 0, mInfluenceBottom);
				updateBottomCounter();
			}
		});

		v.findViewById(R.id.top_player_decrease_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mInfluenceTop--;
				GameTracker.setInfluence(PlayType.TWO_PLAYER, 1, mInfluenceTop);
				updateTopCounter();
			}
		});

		v.findViewById(R.id.bottom_player_decrease_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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
		
		GameTracker.startGame(mInfluenceBottom, mInfluenceTop);
		updateTopCounter();
		updateBottomCounter();
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
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
		
		Drawable d = getResources().getDrawable(stylesReversed.get(currentStyleTop).top);
		d.setLevel(10000);
		mTopbarTop.setBackgroundDrawable(d);
		
		d = getResources().getDrawable(stylesReversed.get(currentStyleTop).bottom);
		d.setLevel(10000);
		mBottombarTop.setBackgroundDrawable(d);
	}
	
	private void toggleBottomStyle() {
		currentStyleBottom = (currentStyleBottom + 1) % styles.size();
		mParent.setTwoPlayerBottomTheme(currentStyleBottom);

		mTopbarBottom.setBackgroundDrawable(getResources().getDrawable(stylesReversed.get(currentStyleBottom).top));
		mBottombarBottom.setBackgroundDrawable(getResources().getDrawable(styles.get(currentStyleBottom).bottom));
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
	public PlayType getPlayType() {
		return PlayType.TWO_PLAYER;
	}
}
