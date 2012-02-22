package dk.ilios.influencecounter;
/**
 * Activity that controls a single player Influence Counter
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

public class SinglePlayerFragment extends Fragment {

	private int value = 25;
	private TextView counter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.single_player_view, container, false);

		// Set reference to views
		counter = (TextView) v.findViewById(R.id.counter);
		counter.setText(new Integer(value).toString());
		
		// Set event handlers
		v.findViewById(R.id.increase_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				value++;
				updateCounter();
			}
		});
		
		v.findViewById(R.id.decrease_influence_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				value--;
				updateCounter();
			}
		});

		v.findViewById(R.id.refresh_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				value = 25;
				updateCounter();
			}
		});
		
		v.findViewById(R.id.style_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggleStyle();
			}
		});
		
		return v;
	}


	private void updateCounter() {
		counter.setText(new Integer(value).toString());
	}
	
	/**
	 * Switch to the next style
	 */
	private void toggleStyle() {
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
