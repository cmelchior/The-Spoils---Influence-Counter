package dk.ilios.influencecounter.history;
/**
 * Content provider for history information.
 * Switching to using a ContentProvider after trying with async DB helpers, which
 * caused a lot of trouble handling the db connection across fragments including
 * database lock errors.
 * 
 * Accessing the ContentProvider should ideally be done using AsyncQueryHandler
 * to avoid locking the UI-thread.
 * 
 * Christian Melchior <christian@ilios.dk>
 */
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import dk.ilios.influencecounter.Database;

public class HistoryContentProvider extends ContentProvider {

	// Setup URI's
	private static final String AUTHORITY = "dk.ilios.influencecounter"; 
	private static final int GAMES = 1;
	private static final int GAME_ID = 2;
	private static final int GAMES_CHANGES = 3;
	private static final int GAMES_CHANGE_ID = 4;
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sUriMatcher.addURI(AUTHORITY, Database.TABLE_GAMES, GAMES);				 // Games table
		sUriMatcher.addURI(AUTHORITY, Database.TABLE_GAMES+"/#", GAME_ID);			 // Games row
		sUriMatcher.addURI(AUTHORITY, Database.TABLE_GAME_STATE, GAMES_CHANGES);	 // Game history table
		sUriMatcher.addURI(AUTHORITY, Database.TABLE_GAME_STATE+"/#", GAMES_CHANGE_ID); // Game history table row
	}

	// Content URIs
	public static Uri GAMES_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/" + Database.TABLE_GAMES);
	public static Uri GAMES_STATE_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/" + Database.TABLE_GAME_STATE);
	
	// Private members
	private Database mDbHelper;
	
	@Override
	public boolean onCreate() {
		mDbHelper = new Database(getContext());
		return true;
	}

	/***************************************************************************
	 * Public utility functions for the content provider                       *
	 **************************************************************************/
	
	/**
	 * Create new game
	 */
	public void createGame(int player1StartingInfluence, int player2StartingInfluence) {
		// TODO
	}
	
	/**
	 * Delete all information about a given game
	 */
	public int deleteGame(int gameId) {
		int deletedRows = 0;
		ContentResolver cr = getContext().getContentResolver();
		deletedRows += cr.delete(ContentUris.withAppendedId(GAMES_URI, gameId), null, null);
		deletedRows += cr.delete(GAMES_STATE_URI, Database.COLUMN_GAME_ID+"="+gameId, null);
		return deletedRows;
	}

	/**
	 * Returns alle state information about a game
	 */
	public Cursor getGameHistory(int gameId) {
		// TODO
		return null;
	}

	/**
	 * Add a new state change for a game
	 */
	public void addInfluenceChange(int gameId, int playerId, int influence, int timestamp) {
		// TODO
	}

	/***************************************************************************
	 * Standard content provider access                                        *
	 **************************************************************************/
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		String finalSortOrder = "";

		switch(sUriMatcher.match(uri)) {
		case GAMES:
			sqlBuilder.setTables(Database.TABLE_GAMES);
			finalSortOrder = setSortOrder(sortOrder, Database.COLUMN__ID);
			break;

		case GAME_ID:
			sqlBuilder.setTables(Database.TABLE_GAMES);
			finalSortOrder = null;
			sqlBuilder.appendWhere(Database.COLUMN__ID + "=" + ContentUris.parseId(uri));
			break;

		case GAMES_CHANGES:
			sqlBuilder.setTables(Database.TABLE_GAME_STATE);
			finalSortOrder = setSortOrder(sortOrder, Database.COLUMN_TIMESTAMP);
			break;

		case GAMES_CHANGE_ID:
			sqlBuilder.setTables(Database.TABLE_GAME_STATE);
			finalSortOrder = null;
			sqlBuilder.appendWhere(Database.COLUMN__ID + "=" + ContentUris.parseId(uri));
			break;

		default:
			throw new IllegalArgumentException("Unsupported query URI: " + uri);        
		}   


		Cursor c = sqlBuilder.query(
				db, 
				projection, 
				selection, 
				selectionArgs, 
				null, 
				null, 
				finalSortOrder);

		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}	
	
	private String setSortOrder(String userSortOrder, String defaultSortOrder) {
		if (userSortOrder == null || userSortOrder.equals("")) {
			return defaultSortOrder;
		} else {
			return userSortOrder;
		}
	}
	
	
	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)){
		case GAMES:
			return "vnd.android.cursor.dir/" + AUTHORITY + "." + Database.TABLE_GAMES;
		case GAME_ID:
			return "vnd.android.cursor.item/" + AUTHORITY + "." + Database.TABLE_GAMES;
		case GAMES_CHANGES:
			return "vnd.android.cursor.dir/" + AUTHORITY + "." + Database.TABLE_GAME_STATE;
		case GAMES_CHANGE_ID:
			return "vnd.android.cursor.item/" + AUTHORITY + "." + Database.TABLE_GAME_STATE;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);        
		}   
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		long rowId = -1;
		Uri contentUri = null;
		
		switch(sUriMatcher.match(uri)) {
		case GAMES:
			rowId = db.insert(Database.TABLE_GAMES, null, values);
			contentUri = Uri.parse(ContentResolver.SCHEME_CONTENT +"://"+ AUTHORITY + "/" + Database.TABLE_GAMES + "/");
			break;
			
		case GAMES_CHANGES:
			rowId = db.insert(Database.TABLE_GAME_STATE, null, values);
			contentUri = Uri.parse(ContentResolver.SCHEME_CONTENT + "://"  + AUTHORITY + "/" + Database.TABLE_GAME_STATE + "/");
			break;

		default:
			throw new IllegalArgumentException("Unsupported insert URI: " + uri);        
		}

		// Notify that a new row has been inserted
		if (rowId > 0) {
	         Uri resultUri = ContentUris.withAppendedId(contentUri, rowId);
	         getContext().getContentResolver().notifyChange(uri, null);
	         getContext().getContentResolver().notifyChange(resultUri, null);    
	         return resultUri;                
		} 
		
        throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int rowsDeleted = 0;
		
		switch(sUriMatcher.match(uri)) {
		case GAMES:
			rowsDeleted = db.delete(Database.TABLE_GAMES, selection, selectionArgs);
			break;
		case GAME_ID:
			rowsDeleted = db.delete(Database.TABLE_GAMES, Database.COLUMN__ID+"="+ContentUris.parseId(uri), null);
			break;
			
		case GAMES_CHANGES:
			rowsDeleted = db.delete(Database.TABLE_GAME_STATE, selection, selectionArgs);
			break;

		case GAMES_CHANGE_ID:
			rowsDeleted = db.delete(Database.TABLE_GAME_STATE, Database.COLUMN__ID+"="+ContentUris.parseId(uri), null);
			break;
			
			
		default:
			throw new IllegalArgumentException("Unsupported delete URI: " + uri);        
		
		
		}
		
		// Notify that a new row has been inserted
		if (rowsDeleted > 0) {
	         getContext().getContentResolver().notifyChange(uri, null);    
		} 

        return rowsDeleted;                
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new RuntimeException("Not implemented");
	}
}
