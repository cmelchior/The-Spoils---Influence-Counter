package dk.ilios.influencecounter;
/**
 * Class for keeping track of changes to influence totals.
 * Is also responsible for saving logs to the database.
 * 
 * @author Christian Melchior <christian@ilios.dk>
 */
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import dk.ilios.influencecounter.history.HistoryContentProvider;
import dk.ilios.influencecounter.utils.Logger;

public class GameTracker {

	private static InfluenceChange[] playerInfluence = new InfluenceChange[Constants.MAX_PLAYERS];
	private static Thread[] timerThreads = new Thread[Constants.MAX_PLAYERS];
		
	private static Context mContext;
	
	private static boolean isInitialized = false;
	private static long mTimeoutInMilliSeconds = 2*1000; // Default is 2 seconds
	private static long  mCurrentGameId = -1;				// -1 = no current game
	
	public static void initialize(Context context, long timeoutInMilliSeconds) {
		isInitialized = true;
		mTimeoutInMilliSeconds = timeoutInMilliSeconds;
		mContext = context;
		
		// Make sure that timers are stopped
		for (int i = 0; i < timerThreads.length; i++) {
			Thread t = timerThreads[i];
			if (t != null) {
				t.interrupt();
			}
		}
	}

	/**
	 * Starts logging a new game
	 */
	public static synchronized void startGame(int player1StartingInfluence, int player2StartingInfluence) {
		new NewGame().execute(new NewGameConfiguration(player1StartingInfluence, player2StartingInfluence));
	}
	
	
	/**
	 * Either starts a timer or resets if already running
	 */
	private static synchronized void resetTimer(final int playerId) {
		// Kill any old timers
		if (timerThreads[playerId] != null) {
			timerThreads[playerId].interrupt();
		}
		
		// Start a new timer
		timerThreads[playerId] = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(mTimeoutInMilliSeconds);
						saveInfluenceState(playerId);
					} catch (InterruptedException e) {
						/* Ignore, just end thread */
					}
				}
			});
		timerThreads[playerId].start();

	}
	
	private synchronized static void saveInfluenceState(int playerId) {

		if (playerInfluence[playerId] != null) {
			InfluenceChange changeToSave = playerInfluence[playerId];
			playerInfluence[playerId] = null;
			new SaveInfluenceChange().execute(changeToSave);
		}
	}
	
	/**
	 * Set new influence for a player
	 * @param playerId		zero-index based (ie. player 1 is 0, player 2 is 1)
	 * @param influence
	 */
	public static void setInfluence(int playerId, int influence) {
		
		if (mTimeoutInMilliSeconds > 0) {
			resetTimer(playerId);
		}
		
		playerInfluence[playerId] = new GameTracker.InfluenceChange(playerId, influence, System.currentTimeMillis(), mCurrentGameId);
		
		if (mTimeoutInMilliSeconds == 0) {
			saveInfluenceState(playerId);
		}
	}
	
	public static long getCurrentGameId() {
		return mCurrentGameId;
	}

	public static void deleteGame(int gameId) {
		new DeleteGame().execute(gameId);
		
	}
	
	public static void updateGameName(int gameId, String gameName) {
		new UpdateGameName().execute(Integer.toString(gameId), gameName);
	}
	
	public static void clearHistory() {
		new ClearHistory().execute();
	}
	
	/**************************************************************************
	 * Async task for new games                                               *
	 **************************************************************************/
	private static class NewGame extends AsyncTask<NewGameConfiguration, Void, Void> {

		@Override
		protected Void doInBackground(NewGameConfiguration... params) {
			Logger.i("InfluenceCounter", "ASync - NewGame");
			if (!isInitialized) return null;

			ContentResolver cr = mContext.getContentResolver();
		    NewGameConfiguration config = params[0];
			
		    // Determine number of players
		    ContentValues values = new ContentValues();
		    values.put("player1", config.player1StartingInfluence);
		    if (config.player2StartingInfluence > 0) {
		    	values.put("player2", config.player2StartingInfluence);
		    }

		    Uri result = cr.insert(HistoryContentProvider.GAMES_NEWGAME_URI, values);
			mCurrentGameId = ContentUris.parseId(result);

		    return null;
		}
	}

	/***************************************************************************
	 * Async task for updating game state
	 **************************************************************************/
	private static class SaveInfluenceChange extends AsyncTask<InfluenceChange, Void, Void> {

		@Override
		protected Void doInBackground(InfluenceChange... params) {
			Logger.i("InfluenceCounter", "ASync - SaveInfluenceChange");
			if (!isInitialized) return null;

			ContentResolver cr = mContext.getContentResolver();
			
			for (int i = 0; i < params.length; i++) {
				InfluenceChange change = params[i];
				ContentValues values = new ContentValues();
				values.put(Database.COLUMN_PLAYER_ID, change.playerId);
				values.put(Database.COLUMN_INFLUENCE, change.influenceChange);
				values.put(Database.COLUMN_TIMESTAMP, change.timestamp);
				values.put(Database.COLUMN_GAME_ID, change.gameId);
				cr.insert(HistoryContentProvider.GAMES_STATE_URI, values);
			}

			return null;
		}
	}
	
	/***************************************************************************
	 * Async task for deleting a game                                          *
	 **************************************************************************/
	private static class DeleteGame extends AsyncTask<Integer, Void, Void> {
		@Override
		protected Void doInBackground(Integer... params) {
			Logger.i("InfluenceCounter", "ASync - Delete Game: " + params[0]);
			if (!isInitialized) return null;

			int gameId = params[0];
			ContentResolver cr = mContext.getContentResolver();
			cr.delete(HistoryContentProvider.GAMES_DELETE_URI, null, new String[] { Integer.toString(gameId) });
			return null;
		}
	}

	/***************************************************************************
	 * Async task for clearing whole history                                          *
	 **************************************************************************/
	private static class ClearHistory extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			Logger.i("InfluenceCounter", "ASync - Clear history");
			if (!isInitialized) return null;

			ContentResolver cr = mContext.getContentResolver();
			int rows = cr.delete(HistoryContentProvider.GAMES_URI, "1=1", null);
			int rows2 = cr.delete(HistoryContentProvider.GAMES_STATE_URI, "1=1", null);
			Logger.i("Database", "Deleted: " + rows + " , "  + rows2);
			return null;
		}
	}

	/***************************************************************************
	 * Async task for updating a game name						               *
	 **************************************************************************/
	private static class UpdateGameName extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			Logger.i("InfluenceCounter", "ASync - Update name");
			if (!isInitialized) return null;

			ContentResolver cr = mContext.getContentResolver();
			ContentValues values = new ContentValues();
			values.put(Database.COLUMN_GAME_NAME, params[1]);
			
			cr.update(HistoryContentProvider.GAMES_URI, values, Database.COLUMN__ID+"=?", new String[] { params[0] });
			return null;
		}
	}

	
	
	/***************************************************************************
	 * Async task for removing old games with only starting values             *
	 **************************************************************************/
	private static class CleanupDatabase extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Logger.i("InfluenceCounter", "ASync - CleanupDatabase");
			if (!isInitialized) return null;

			SQLiteDatabase db = new Database(mContext).getWritableDatabase();

			Cursor c = db.query(Database.TABLE_GAME_STATE, 
					new String[] {Database.COLUMN_GAME_ID , "COUNT(*)"}, 
					null, 
					null, 
					Database.COLUMN_GAME_ID, 
					"COUNT(*) <= 2", 
					Database.COLUMN_GAME_ID);
			
			if (c.getCount() > 0) {
				while(c.moveToNext()) {
					db.delete(Database.TABLE_GAMES, "game_id=" + c.getInt(c.getColumnIndexOrThrow("COUNT(*)")), null);
				}
			}
			
			c.close();
			db.close();

			return null;
		}
	}
	
	
	/**************************************************************************
	 * Wrapper class for new games	                                          *
	 **************************************************************************/
	public static class NewGameConfiguration {
		public int player1StartingInfluence;
		public int player2StartingInfluence;
		
		public NewGameConfiguration(int player1StartingInfluence, int player2StartingInfluence) {
			this.player1StartingInfluence = player1StartingInfluence;
			this.player2StartingInfluence = player2StartingInfluence;
		}
	}
	
	/**************************************************************************
	 * Wrapper class for influence changes                                    *
	 **************************************************************************/
	public static class InfluenceChange {
		public int playerId;
		public int influenceChange;
		public long timestamp;
		public long gameId;
		
		public InfluenceChange(int playerId, int influenceChange, long timestamp, long gameId) {
			this.playerId = playerId;
			this.influenceChange = influenceChange;
			this.timestamp = timestamp;
			this.gameId = gameId;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof InfluenceChange)) return false;
			InfluenceChange compareObj = (InfluenceChange) o;
			return (compareObj.influenceChange == this.influenceChange && compareObj.playerId == this.playerId);
		}
		
	}
}
