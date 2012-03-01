package dk.ilios.influencecounter;
/**
 * Activity that controls Influence counters for both players
 * 
 * @author Christian Melchior <christian@ilios.dk>
 */
import java.util.ArrayList;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import dk.ilios.influencecounter.views.OutlinedTextView;

public class TwoPlayerFragment extends Fragment {

	private int mValueTop = 25;
	private int mValueBottom = 25;
	private OutlinedTextView mCounterTop;
	private OutlinedTextView mCounterBottom;
	
	private View mTopbarTop;
	private View mBottombarTop;

	private View mTopbarBottom;
	private View mBottombarBottom;
	
	private int currentStyleTop = 1;	// Starting style minus 1;
	private int currentStyleBottom = -1;	// Starting style minus 1;
	
	private ArrayList<StyleTemplate> styles = new ArrayList<StyleTemplate>();
	private ArrayList<StyleTemplate> stylesReversed = new ArrayList<StyleTemplate>();
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.two_player_view, container, false);

		// Set reference to views
		mTopbarTop = v.findViewById(R.id.top_player_top_bar);
		mBottombarTop = v.findViewById(R.id.top_player_control_bar);
		mTopbarBottom = v.findViewById(R.id.bottom_player_top_bar);
		mBottombarBottom = v.findViewById(R.id.bottom_player_control_bar);
		
		
		
		mCounterTop = (OutlinedTextView) v.findViewById(R.id.top_player_counter);
		mCounterTop.setText(new Integer(mValueTop).toString());

		mCounterBottom = (OutlinedTextView) v.findViewById(R.id.bottom_player_counter);
		mCounterBottom.setText(new Integer(mValueBottom).toString());
		
		// Set event handlers
		v.findViewById(R.id.top_player_increase_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mValueTop++;
				updateTopCounter();
			}
		});

		v.findViewById(R.id.bottom_player_increase_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mValueBottom++;
				updateBottomCounter();
			}
		});

		v.findViewById(R.id.top_player_decrease_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mValueTop--;
				updateTopCounter();
			}
		});

		v.findViewById(R.id.bottom_player_decrease_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mValueBottom--;
				updateBottomCounter();
			}
		});

		
		v.findViewById(R.id.top_player_refresh_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mValueTop = ((MainActivity) getActivity()).getDefaultStartingInfluence();
				updateTopCounter();
			}
		});

		v.findViewById(R.id.bottom_player_refresh_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mValueBottom = ((MainActivity) getActivity()).getDefaultStartingInfluence();
				updateBottomCounter();
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
		
		toggleBottomStyle();
		toggleTopStyle();

		return v;
	}

	
	private void updateTopCounter() {
		mCounterTop.setText(new Integer(mValueTop).toString());
	}
	
	private void updateBottomCounter() {
		mCounterBottom.setText(new Integer(mValueBottom).toString());
	}
	
	private void toggleTopStyle() {
		currentStyleTop = (currentStyleTop + 1) % stylesReversed.size();
		
		Drawable d = getResources().getDrawable(stylesReversed.get(currentStyleTop).top);
		d.setLevel(10000);
		mTopbarTop.setBackgroundDrawable(d);
		
		d = getResources().getDrawable(stylesReversed.get(currentStyleTop).bottom);
		d.setLevel(10000);
		mBottombarTop.setBackgroundDrawable(d);
	}
	
	private void toggleBottomStyle() {
		currentStyleBottom = (currentStyleBottom + 1) % styles.size();
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
}
