
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;import dk.ilios.influencecounter.GameHistoryFragment;


public class GamesListAdapter extends FragmentStatePagerAdapter {
	public GamesListAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public int getCount() {
		// TODO
		return 2; // 2 pages - Single and two-player view
	}

	@Override
	public Fragment getItem(int position) {
		return new GameHistoryFragment();
	}
}
