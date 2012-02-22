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

	private int valueTop = 25;
	private int valueBottom = 25;
	private TextView counterTop;
	private TextView counterBottom;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.two_player_view, container, false);

		// Set reference to views
		counterTop = (TextView) v.findViewById(R.id.top_player_counter);
		counterTop.setText(new Integer(valueTop).toString());

		counterBottom = (TextView) v.findViewById(R.id.bottom_player_counter);
		counterBottom.setText(new Integer(valueBottom).toString());

		
		// Set event handlers
		v.findViewById(R.id.top_player_increase_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				valueTop++;
				updateTopCounter();
			}
		});

		v.findViewById(R.id.bottom_player_increase_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				valueBottom++;
				updateBottomCounter();
			}
		});

		v.findViewById(R.id.top_player_decrease_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				valueTop--;
				updateTopCounter();
			}
		});

		v.findViewById(R.id.bottom_player_decrease_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				valueBottom--;
				updateBottomCounter();
			}
		});

		
		v.findViewById(R.id.top_player_refresh_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				valueTop = 25;
				updateTopCounter();
			}
		});

		v.findViewById(R.id.bottom_player_refresh_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				valueBottom = 25;
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
		counterTop.setText(new Integer(valueTop).toString());
	}
	
	private void updateBottomCounter() {
		counterBottom.setText(new Integer(valueBottom).toString());
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
