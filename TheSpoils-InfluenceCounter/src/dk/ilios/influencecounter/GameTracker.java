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
import android.net.Uri;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;
import dk.ilios.influencecounter.history.HistoryContentProvider;
import dk.ilios.influencecounter.utils.Logger;

public class GameTracker {

	private static int NO_GAME_ID = -1;
	
	private static InfluenceChange[] playerInfluence = new InfluenceChange[Constants.MAX_PLAYERS];
	private static Thread[] timerThreads = new Thread[Constants.MAX_PLAYERS];
		
	private static Context mContext;
	
	private static boolean isInitialized = false;
	private static long mTimeoutInMilliSeconds = 2*1000; // Default is 2 seconds
	private static long  mCurrentSinglePlayerGameId = NO_GAME_ID;
	private static long mCurrentTwoPlayerGameId = NO_GAME_ID;
	private static boolean mNoLogWarningShowedSinglePlayer = false;
	private static boolean mNoLogWarningShowedTwoPlayer = false;
	
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
	
	/*
	
	
	
	
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
	public static void setInfluence(PlayType gameType, int playerId, int influence) {
		
		if (mTimeoutInMilliSeconds > 0) {
			resetTimer(playerId);
		}
		
		long gameId = NO_GAME_ID;
		switch(gameType) {
		case SINGLE_PLAYER: gameId = mCurrentSinglePlayerGameId; break;
		case TWO_PLAYER: gameId = mCurrentTwoPlayerGameId; break;
		}
		
		if (gameId != NO_GAME_ID) {
			playerInfluence[playerId] = new GameTracker.InfluenceChange(playerId, influence, System.currentTimeMillis(), gameId);
			
			if (mTimeoutInMilliSeconds == 0) {
				saveInfluenceState(playerId);
			}

		} else {
			
			switch (gameType) {
			case SINGLE_PLAYER: 
				if (!mNoLogWarningShowedSinglePlayer) {
					showToast();
					mNoLogWarningShowedSinglePlayer = true;
				}
				break;

			case TWO_PLAYER:
				if (!mNoLogWarningShowedTwoPlayer) {
					showToast();
					mNoLogWarningShowedTwoPlayer = true;
				}
				break;
			}
		}
		
	}

	private static void showToast() {
		Toast t = Toast.makeText(mContext, mContext.getString(R.string.no_log), Toast.LENGTH_LONG);
		t.setGravity(Gravity.TOP, 0, (int) mContext.getResources().getDimension(R.dimen.toast_offset));
		t.show();
	}
	
	public static long getCurrentGameId(PlayType gameType) {
		switch(gameType) {
		case SINGLE_PLAYER: return mCurrentSinglePlayerGameId;
		case TWO_PLAYER: return mCurrentTwoPlayerGameId;
		default: return NO_GAME_ID;
		}
	}

	public static void deleteGame(int gameId) {
		if (mCurrentSinglePlayerGameId == gameId) {
			mCurrentSinglePlayerGameId = NO_GAME_ID;
		} else if (mCurrentTwoPlayerGameId == gameId) {
			mCurrentTwoPlayerGameId = NO_GAME_ID;
		}
		
		new DeleteGame().execute(gameId);
	}
	
	public static void updateGameName(int gameId, String gameName) {
		new UpdateGameName().execute(Integer.toString(gameId), gameName);
	}
	
	public static void clearHistory(int players) {
		if (players == 1) {
			mCurrentSinglePlayerGameId = NO_GAME_ID;
		} else if (players == 2) {
			mCurrentTwoPlayerGameId = NO_GAME_ID;
		}
		
		new ClearHistory().execute(players);
	}

	public static void cleanupDatabase() {
		new CleanupDatabase().execute();
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
			
		    if (config.player2StartingInfluence > 0 ) {
		    	mCurrentTwoPlayerGameId = ContentUris.parseId(result);
		    	mNoLogWarningShowedSinglePlayer = false;
		    } else {
		    	mCurrentSinglePlayerGameId = ContentUris.parseId(result);
		    	mNoLogWarningShowedTwoPlayer = false;
		    }
		    
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
	private static class ClearHistory extends AsyncTask<Integer, Void, Void> {
		@Override
		protected Void doInBackground(Integer... params) {
			Logger.i("InfluenceCounter", "ASync - Clear history: " + params[0]);
			if (!isInitialized) return null;

			ContentResolver cr = mContext.getContentResolver();
			int rows = cr.delete(HistoryContentProvider.GAMES_DELETE_TYPE_URI, null, new String[] { Integer.toString(params[0]) });
			Logger.i("Database", "Games deleted: " + rows);
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

			ContentResolver cr = mContext.getContentResolver();
			cr.delete(HistoryContentProvider.GAMES_CLEANUP_URI, null, null);
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
