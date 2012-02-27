package dk.ilios.influencecounter;
/**
 * Activity that controls a single player Influence Counter
 * 
 * @author Christian Melchior
 */
import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import dk.ilios.influencecounter.views.OutlinedTextView;

public class SinglePlayerFragment extends Fragment {

	private int mInfluence = 25;
	private OutlinedTextView mCounterView;
	private View mTopbar;
	private View mBottombar;
	
	private int currentStyle = -1;
	private ArrayList<StyleTemplate> styles = new ArrayList<StyleTemplate>();
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		styles.add(new StyleTemplate(R.drawable.warlords_top, R.drawable.warlords_bottom));
		styles.add(new StyleTemplate(R.drawable.banker_top, R.drawable.banker_bottom));
		styles.add(new StyleTemplate(R.drawable.rogue_top, R.drawable.rogue_bottom));
		styles.add(new StyleTemplate(R.drawable.arcanist_top, R.drawable.arcanist_bottom));
		styles.add(new StyleTemplate(R.drawable.gearsmith_top, R.drawable.gearsmith_bottom));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.single_player_view, container, false);

		mInfluence = ((MainActivity) getActivity()).getDefaultStartingInfluence();
		
		// Set reference to views
		mCounterView = (OutlinedTextView) v.findViewById(R.id.counter);
		mCounterView.setText(new Integer(mInfluence).toString());

		mTopbar = v.findViewById(R.id.top_bar);
		mBottombar = v.findViewById(R.id.control_bar);
		
		// Set event handlers
		v.findViewById(R.id.increase_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mInfluence++;
				updateCounter();
			}
		});
		
		v.findViewById(R.id.decrease_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mInfluence--;
				updateCounter();
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


	private void updateCounter() {
		mCounterView.setText(new Integer(mInfluence).toString());
	}
	
	/**
	 * Switch to the next style
	 */
	private void toggleStyle() {
		currentStyle = (currentStyle + 1) % styles.size();
		mTopbar.setBackgroundDrawable(getResources().getDrawable(styles.get(currentStyle).top));
		mBottombar.setBackgroundDrawable(getResources().getDrawable(styles.get(currentStyle).bottom));
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
