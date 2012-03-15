package dk.ilios.influencecounter.history;
/**
 * Page adapter for showing games. The Database is monitored and new
 * games are automatically inserted.
 * 
 * @author Christian Melchior <christian@ilios.dk>
 */
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import dk.ilios.influencecounter.Database;
import dk.ilios.influencecounter.GameHistoryFragment;
import dk.ilios.influencecounter.MainActivity;

public class GamesListAdapter extends FragmentStatePagerAdapter {

	private int mGameCount = 0;
	private Cursor mCursor;
	
	public GamesListAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public void setCursor(Cursor cursor) {
		mCursor = cursor;
		mGameCount = (cursor != null) ? cursor.getCount() : 0;
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
}
