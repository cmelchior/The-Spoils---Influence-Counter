package dk.ilios.influencecounter;
/**
 * Activity that controls Influence counters for both players
 * 
 * @author Christian Melchior
 */
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class TwoPlayerFragment extends Fragment {

	private int mValueTop = 25;
	private int mValueBottom = 25;
	private TextView mCounterTop;
	private TextView mCounterBottom;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.two_player_view, container, false);

		// Set reference to views
		mCounterTop = (TextView) v.findViewById(R.id.top_player_counter);
		mCounterTop.setText(new Integer(mValueTop).toString());

		mCounterBottom = (TextView) v.findViewById(R.id.bottom_player_counter);
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

		
		return v;
	}

	
	private void updateTopCounter() {
		mCounterTop.setText(new Integer(mValueTop).toString());
	}
	
	private void updateBottomCounter() {
		mCounterBottom.setText(new Integer(mValueBottom).toString());
	}
	
	private void toggleTopStyle() {
		// TODO
	}
	
	private void toggleBottomStyle() {
		// TODO
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
