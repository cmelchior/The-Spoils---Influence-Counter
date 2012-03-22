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
import dk.ilios.influencecounter.PlayType;

public class GamesListCursorLoader extends CursorLoader {

	private PlayType mGameType;
	private Context mContext;
	
	public GamesListCursorLoader(Context ctx, PlayType gameType) {
		super(ctx);
		mContext = ctx;
		mGameType = gameType;
	}
	
	@Override
	public Cursor loadInBackground() {
		ContentResolver cr = mContext.getContentResolver();
		int players = 1;
		if (mGameType == PlayType.TWO_PLAYER) {
			players = 2;
		}
		return cr.query(HistoryContentProvider.GAMES_URI, Database.HISTORY_GAME_COLUMNS, Database.COLUMN_PLAYERS+"="+players, null, Database.COLUMN__ID + " ASC");
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
