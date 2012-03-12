package dk.ilios.influencecounter.history;
/**
 * Loads the list of games.
 * 
 * @author Christian Melchior <christian@ilios.dk>
 */
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import dk.ilios.influencecounter.Database;

public class GamesListCursorLoader extends SimpleCursorLoader {

	private SQLiteDatabase mDb;
	private Context mContext;
	
	public GamesListCursorLoader(Context ctx) {
		super(ctx);
		mContext = ctx;
	}
	
	@Override
	public Cursor loadInBackground() {
		mDb = new Database(mContext).getReadableDatabase(); 
		Cursor c = mDb.query(Database.TABLE_GAMES, Database.HISTORY_GAME_COLUMNS, null, null, null, null, "_id ASC");
		return c;	
	}
	
	/**
	 * Close the loader gracefully so no database connections are left open
	 */
	public void closeDatabase() {
		if (mDb != null) {
			mDb.close();
		}
	}
}
