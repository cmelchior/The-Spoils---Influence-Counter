package dk.ilios.influencecounter;
/**
 * Database controller
 * 
 * A few important invariants to maintain:
 * - player_id in game_state is the player number from 1 - x.
 * - Timestamps, when doing the first change, the original number is saved first
 *   with the same timestamp, eg. 25 -> 24 is saved as to states with the same time.
 * 
 * @author Christian Melchior <christian@ilios.dk>
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database extends SQLiteOpenHelper {

	private static final String LOG_TAG = "DB";

	// Database
	public static final String DATABASE_NAME = "history.db";
	public static final int DATABASE_VERSION = 1;

	// Tables
	public static final String TABLE_GAMES = "games";
	public static final String TABLE_GAME_STATE = "game_state_changes";

	// Columns
	public static final String COLUMN__ID = "_id";

	// Games
	public static final String COLUMN_GAME_NAME = "name";
	public static final String COLUMN_PLAYERS = "players"; // Number of tracked players
	
	// Game state changes
	public static final String COLUMN_GAME_ID = "game_id";
	public static final String COLUMN_PLAYER_ID = "player"; // Player number [1-x]
	public static final String COLUMN_TIMESTAMP = "timestamp";
	public static final String COLUMN_INFLUENCE = "influence";

	// Easy reference all table columns when loading data
	public static final String[] HISTORY_GAME_COLUMNS = { Database.COLUMN__ID, Database.COLUMN_GAME_NAME, Database.COLUMN_PLAYERS };
	public static final String[] HISTORY_GAME_STATE_COLUMNS = { Database.COLUMN__ID, Database.COLUMN_PLAYER_ID, Database.COLUMN_TIMESTAMP, Database.COLUMN_INFLUENCE }; 
	
	private AssetManager am;

	public Database(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		am = context.getAssets();
	}

	/**
	 * Create database from external schema if needed
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {

		// Read schema from file
		importData("history.db.sql", db);

		// Read demo data in debug mode
		if (Constants.DEBUG) {
			importData("demo-history.sql", db);
		}

		// Create database
		Log.d(LOG_TAG, "Database created");
	}

	private void importData(String fileName, SQLiteDatabase db) {

		InputStream is = null;
		BufferedReader reader = null;
		try {

			is = am.open(fileName);
			reader = new BufferedReader(new InputStreamReader(is, "utf8"));
			StringBuilder sb = new StringBuilder();
			String s = "";

			while ((s = reader.readLine()) != null) {
				sb.append(s);
				if (s.endsWith(";")) {
					db.execSQL(sb.toString());
					sb.setLength(0);
				}
			}

			// Create database
			Log.d(LOG_TAG, fileName + " imported.");

		} catch (IOException e) {
			Log.e(LOG_TAG, "Failed to access DB schema file: " + e.getMessage());
		} catch (SQLException e) {
			Log.e(LOG_TAG,
					"Could not import file (" + fileName + "): "
							+ e.getMessage());
		} finally {
			try {
				reader.close();
				is.close();
			} catch (Exception e) {
				// Ignore
			}
		}
	}

	/**
	 * Upgrade database if needed.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		try {

			// Do upgrades here
			// None yet...

		} catch (SQLException e) {
			Log.e(LOG_TAG, "Could not upgrade database: " + e.getMessage());
		}
	}
}
