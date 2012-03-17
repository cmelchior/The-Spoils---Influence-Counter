package dk.ilios.influencecounter.history;
/**
 * Page adapter for showing games.
 * Note: FragmentPageAdapter seems to remove the inner lists for some reason,
 * FragmentStatePagerAdpater doesn't do that, but it seems to make the 
 * animations more choppy.
 *  
 * @author Christian Melchior <christian@ilios.dk>
 */
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import dk.ilios.influencecounter.Database;
import dk.ilios.influencecounter.GameHistoryFragment;

public class GamesListAdapter extends FragmentStatePagerAdapter {

	private int mGameCount = 0;
	private Cursor mCursor;
	private int[] oldIds = null;	// List of Id's from old cursor
	private int[] currentIds = null; // List of Id's from new cursor;
	
	public GamesListAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public void swapCursor(Cursor cursor) {
		mCursor = cursor;
		mGameCount = (cursor != null) ? cursor.getCount() : 0;
		
		// Save a list of game Ids
		oldIds = currentIds;
		currentIds = (cursor != null) ? new int[cursor.getCount()] : null;
	
		if (cursor != null) {
			int startPotion = mCursor.getPosition();
			int i = 0;
			while (cursor.moveToNext()) {
				currentIds[i] = cursor.getInt(cursor.getColumnIndex(Database.COLUMN__ID));
				i++;
			}
			cursor.moveToPosition(startPotion);
		}

		notifyDataSetChanged();
	}
	
	
	@Override
	public int getCount() {
		return mGameCount;
	}

	@Override
	public Fragment getItem(int position) {
		if (mCursor != null) {
			mCursor.moveToPosition(position);
			int gameId = mCursor.getInt(mCursor.getColumnIndex(Database.COLUMN__ID));
			String gameName = mCursor.getString(mCursor.getColumnIndex(Database.COLUMN_GAME_NAME));
			int players = mCursor.getInt(mCursor.getColumnIndex(Database.COLUMN_PLAYERS));
			return GameHistoryFragment.newInstance(gameId, gameName, players);
		}
		
  		return null;
	}
	
	@Override
	public int getItemPosition(Object object) {
		GameHistoryFragment fragment = (GameHistoryFragment) object;
		int gameId = fragment.getGameId();
		
		// Trivial case (first cursor)
		if (oldIds == null) {
			return PagerAdapter.POSITION_UNCHANGED;
		}
		
		// Find position in new and old cursor
		int oldPosition = -1;
		int newPosition = -1;
		
		for (int i = 0; i < oldIds.length; i++) {
			if (oldIds[i] == gameId) {
				oldPosition = i;
			}
		}
		
		for (int i = 0; i < currentIds.length; i++) {
			if (currentIds[i] == gameId) {
				newPosition = i;
			}
		}
		
		// Determine if fragment has been deleted or moved position
		if (newPosition == -1) {
			return PagerAdapter.POSITION_NONE;
		} else if (oldPosition == newPosition) {
			return PagerAdapter.POSITION_UNCHANGED;
		} else {
			return newPosition;
		}
	}
}
