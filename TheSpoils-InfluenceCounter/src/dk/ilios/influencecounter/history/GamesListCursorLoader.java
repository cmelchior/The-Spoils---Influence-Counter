package dk.ilios.influencecounter.history;
/**
 * Loads the list of games.
 * 
 * @author Christian Melchior <christian@ilios.dk>
 */
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import dk.ilios.influencecounter.Database;

public class GamesListCursorLoader extends CursorLoader {

	private Context mContext;
	
	public GamesListCursorLoader(Context ctx) {
		super(ctx);
		mContext = ctx;
	}
	
	@Override
	public Cursor loadInBackground() {
		ContentResolver cr = mContext.getContentResolver();
		return cr.query(HistoryContentProvider.GAMES_URI, Database.HISTORY_GAME_COLUMNS, null, null, Database.COLUMN__ID + " ASC");
	}
	
    @Override
    protected void onStopLoading() {
    	super.onStopLoading();
    }

    @Override
    public void onCanceled(Cursor cursor) {
    	super.onCanceled(cursor);
    }
}
