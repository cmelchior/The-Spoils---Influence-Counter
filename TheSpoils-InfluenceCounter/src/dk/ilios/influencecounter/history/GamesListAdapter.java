package dk.ilios.influencecounter.history;
/**
 * Page adapter for showing games. The Database is monitored and new
 * games are automatically inserted.
 * 
 * @author Christian Melchior <christian@ilios.dk>
 */
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import dk.ilios.influencecounter.Database;
import dk.ilios.influencecounter.GameHistoryFragment;
import dk.ilios.influencecounter.MainActivity;

public class GamesListAdapter extends FragmentStatePagerAdapter implements LoaderCallbacks<Cursor> {

	private SQLiteDatabase mDb;
	private int mGameCount = 0;
	private Cursor mCursor;
	private MainActivity mContext;
	
	public GamesListAdapter(FragmentManager fm, MainActivity activity) {
		super(fm);
		mContext = activity;
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
	
	public void closeDatabase() {
		if (mDb != null) {
			mDb.close();
		}
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new GamesListCursorLoader(mContext);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mGameCount = data.getCount();
		mCursor = data;
		notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mGameCount = 0;
		mCursor = null;
		notifyDataSetChanged();
	}
}
