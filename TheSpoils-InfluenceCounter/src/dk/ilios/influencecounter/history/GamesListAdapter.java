package dk.ilios.influencecounter.history;
/**
 * Page adapter for showing games.
 * Note: FragmentPageAdapter seems to remove the inner lists for some reason,
 * FragmentStatePagerAdpater doesn't do that, but it seems to make the 
 * animations more choppy.
 * 
 * TODO There seems to be some trouble with the FragmentStatePagerAdapter
 * when adding/removing fragments at a rapid pace. Some of the problems
 * seems to be related to multiple contentprovider actions, but moving
 * these transactions to the provider itself probably don't fix the root
 * cause.
 * 
 * Testing so far seems to indicate that the problem has been removed by 
 * moving all "transactions" to the ContentProvider itself.
 *  
 * @author Christian Melchior <christian@ilios.dk>
 */
import java.util.Arrays;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import dk.ilios.influencecounter.Database;
import dk.ilios.influencecounter.GameHistoryFragment;
import dk.ilios.influencecounter.utils.Logger;

public class GamesListAdapter extends FragmentStatePagerAdapter {

	private int mGameCount = 0;
	private Cursor mCursor;
	private int[] oldIds = null;	// List of Id's from old cursor
	private int[] currentIds = null; // List of Id's from new cursor;
	
	public GamesListAdapter(FragmentManager fm) {
		super(fm);
	}
	
	/**
	 * Returns old cursor (not closed) or null if none. Also returns null if
	 * cursor is the same.
	 */
	public Cursor swapCursor(Cursor cursor) {
		if (cursor != null && cursor.equals(mCursor)) {
			return null;
		}
		
		Cursor returnCursor = mCursor;
		mCursor = cursor;
		mGameCount = (cursor != null) ? cursor.getCount() : 0;
		
		// Save a list of game Ids
		oldIds = currentIds;
		currentIds = (cursor != null) ? new int[cursor.getCount()] : null;
	
		if (cursor != null && cursor.getCount() > 0) {
			int startPotion = mCursor.getPosition();
			int i = 0;
			while (cursor.moveToNext()) {
				currentIds[i] = cursor.getInt(cursor.getColumnIndex(Database.COLUMN__ID));
				i++;
			}
			cursor.moveToPosition(startPotion);
		}

		Logger.i("InfluenceCounter", "Old: " + Arrays.toString(oldIds) + " , " + "New: " + Arrays.toString(currentIds));
		
		return returnCursor;
	}
	
	
	@Override
	public int getCount() {
		return mGameCount;
	}

	@Override
	public Fragment getItem(int position) {
		Logger.i("InfluenceCounter", "GetItem: " + position);
		if (mCursor != null && mCursor.moveToPosition(position)) {
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

		// Trivial case (no elements)
		if (currentIds == null) {
			return PagerAdapter.POSITION_NONE;
		}
		
		// Find position in new and old cursor
		int oldPosition = -1;
		int newPosition = -1;
		
		for (int i = 0; i < oldIds.length; i++) {
			if (oldIds[i] == -gameId) {
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
