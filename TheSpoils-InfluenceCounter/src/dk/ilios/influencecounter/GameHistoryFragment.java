package dk.ilios.influencecounter;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.ResourceCursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import dk.ilios.influencecounter.history.HistoryContentProvider;
import dk.ilios.influencecounter.utils.Formatter;
import dk.ilios.influencecounter.utils.Logger;

public class GameHistoryFragment extends Fragment implements LoaderCallbacks<Cursor> {

	private static final int LOADER_ID = 0x02;
	
	private ListView mHistoryList;
	private int mGameId;
	private String mGameName;
	private int mPlayers;
	private Cursor mCurrentCursor;
	private CursorAdapter mAdapter;
	
	public static GameHistoryFragment newInstance(int gameId, String gameName, int players) {
        GameHistoryFragment f = new GameHistoryFragment();
        Bundle args = new Bundle();
        args.putInt("gameId", gameId);
        args.putString("gameName", gameName);
        args.putInt("players", players);
        f.setArguments(args);

        return f;
    }	
	
	public GameHistoryFragment() {
		// Empty constructor required by the framework
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGameId = getArguments().getInt("gameId", -1);
		mGameName = getArguments().getString("gameName", "");
		mPlayers = getArguments().getInt("players", 0);
		if (mGameId == GameTracker.getCurrentGameId()) {
			GameTracker.setGameHistoryFragment(this);
		}
		
		if (mPlayers == 2) {
			mAdapter = new TwoPlayerHistoryAdapter(getActivity(), R.layout.history_row, null, 0);
		} else {
			mAdapter = new SinglePlayerHistoryAdapter(getActivity(), R.layout.history_row, null, 0);
		}
		
		getLoaderManager().initLoader(LOADER_ID, null, this);
		Logger.i("InfluenceCounter", "HistoryFragment created: " + mGameId);
	}

	@Override
	public void onDestroy() {
		Logger.i("InfluenceCounter", "HistoryFragment destroyed: " + mGameId);
		super.onDestroy();
		getLoaderManager().destroyLoader(LOADER_ID);
		if (mCurrentCursor != null && !mCurrentCursor.isClosed()) {
			mCurrentCursor.close();
			mCurrentCursor.unregisterContentObserver(observer);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		int layout = (mPlayers == 2) ? R.layout.history_two_player : R.layout.history_single_player;
		View v = inflater.inflate(layout, null);
		mHistoryList = (ListView) v.findViewById(R.id.history_list);
		mHistoryList.setAdapter(mAdapter);

		((TextView) v.findViewById(R.id.game_name)).setText(mGameName);
		return v;
	}
	
	public int getGameId() {
		return getArguments().getInt("gameId", -1);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new android.support.v4.content.CursorLoader(getActivity(), 
				HistoryContentProvider.GAMES_STATE_URI, 
				Database.HISTORY_GAME_STATE_COLUMNS, 
				Database.COLUMN_GAME_ID+"="+mGameId, 
				null,
				Database.COLUMN_TIMESTAMP + " ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (isRemoving()) return;

		if (mCurrentCursor != null) {
			mCurrentCursor.unregisterContentObserver(observer);
			mCurrentCursor.close();
		}
		
		mCurrentCursor = data;
		mCurrentCursor.registerContentObserver(observer);
		Cursor c = mAdapter.swapCursor(mCurrentCursor);
		if (c != null && !c.isClosed()) {
			c.close();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (isRemoving()) return;

		if (mAdapter != null) {
			Cursor c = mAdapter.swapCursor(null);
			if (c != null) {
				c.close();
			}
		}
	}
	
	ContentObserver observer = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			if (isAdded() && !isRemoving()) {
				getLoaderManager().restartLoader(LOADER_ID, null, GameHistoryFragment.this);
			}
		}
	};

	

/*******************************************************************************
 * SINGLE PLAYER ROW ADAPTER                                                   *
 ******************************************************************************/
	private static class SinglePlayerHistoryAdapter extends ResourceCursorAdapter {

		private DateFormat mTimeFormatter;
		
		public SinglePlayerHistoryAdapter(Context context, int layout, Cursor c, int flags) {
			super(context, layout, c, flags);
			mTimeFormatter = Formatter.getTimeFormatter(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			if (cursor.getPosition() % 2 == 0) {
				view.setBackgroundColor(mContext.getResources().getColor(R.color.history_row_background_1));
			} else {
				view.setBackgroundColor(mContext.getResources().getColor(R.color.history_row_background_2));
			}
			
			int previousInfluence = Integer.MIN_VALUE;
			if (!cursor.isFirst() && cursor.moveToPrevious()) {
				previousInfluence = cursor.getInt(cursor.getColumnIndex(Database.COLUMN_INFLUENCE));
				cursor.moveToNext();
			} 

			int currentInfluence = cursor.getInt(cursor.getColumnIndex(Database.COLUMN_INFLUENCE)); 
			int change = currentInfluence - previousInfluence;
			
			TextView time = (TextView) view.findViewById(R.id.column_1);
			time.setText(mTimeFormatter.format(new Date(cursor.getLong(cursor.getColumnIndex(Database.COLUMN_TIMESTAMP)))));
			
			TextView influenceChange = (TextView) view.findViewById(R.id.column_2);
			influenceChange.setText(Integer.toString(currentInfluence));
			
			TextView tv = (TextView) view.findViewById(R.id.column_3);
			if (previousInfluence == Integer.MIN_VALUE) {
				tv.setText(mContext.getString(R.string.start_history_marker));
			} else {
				tv.setText(Formatter.colorize(change, mContext));
			}
		}
	}
	
/*******************************************************************************
 * SINGLE PLAYER ROW ADAPTER                                                   *
 ******************************************************************************/
	private static class TwoPlayerHistoryAdapter extends ResourceCursorAdapter {

		private DateFormat mTimeFormatter;

		public TwoPlayerHistoryAdapter(Context context, int layout, Cursor c, int flags) {
			super(context, layout, c, flags);
			mTimeFormatter = Formatter.getTimeFormatter(context);
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			if (cursor.getPosition() % 2 == 0) {
				view.setBackgroundColor(mContext.getResources().getColor(R.color.history_row_background_1));
			} else {
				view.setBackgroundColor(mContext.getResources().getColor(R.color.history_row_background_2));
			}

			int player1ActualInfluence = 0;
			int player1Change = 0;
			int player2ActualInfluence = 0;
			int player2Change = 0;
			
			// Hide starting row
			if (cursor.isFirst()) {
				view.setVisibility(View.GONE);
				return;
			} else {
				view.setVisibility(View.VISIBLE);
			}
			
			// Assume 2nd row is the "starting row"
			// Invariant: 2nd player is row 2, 1st player is row 1
			
			boolean startRow = false;
			int player = cursor.getInt(cursor.getColumnIndexOrThrow(Database.COLUMN_PLAYER_ID));

			if (cursor.getPosition() == 1) {
				startRow = true;
				player2ActualInfluence = cursor.getInt(cursor.getColumnIndexOrThrow(Database.COLUMN_INFLUENCE));
				cursor.moveToPrevious();
				player1ActualInfluence = cursor.getInt(cursor.getColumnIndexOrThrow(Database.COLUMN_INFLUENCE));
				cursor.moveToNext();

			} else {
				if (player == 1) {
					player1ActualInfluence = cursor.getInt(cursor.getColumnIndexOrThrow(Database.COLUMN_INFLUENCE));
					player1Change = player1ActualInfluence - getPlayerInfluence(cursor, 1);
					player2ActualInfluence = getPlayerInfluence(cursor, 2);
							
				} else {
					player2ActualInfluence = cursor.getInt(cursor.getColumnIndexOrThrow(Database.COLUMN_INFLUENCE));
					player2Change = player2ActualInfluence - getPlayerInfluence(cursor, 2);
					player1ActualInfluence = getPlayerInfluence(cursor, 1);
				}
			}
			
			
			TextView time = (TextView) view.findViewById(R.id.column_1);
			time.setText(mTimeFormatter.format(new Date(cursor.getLong(cursor.getColumnIndex(Database.COLUMN_TIMESTAMP)))));

			TextView influenceChange = (TextView) view.findViewById(R.id.column_2);
			if (startRow || player1Change == 0) {
				influenceChange.setText(Integer.toString(player1ActualInfluence));
			} else {
				influenceChange.setText(TextUtils.concat(Integer.toString(player1ActualInfluence), Formatter.colorize(" (%1$s)", player1Change, mContext)));
			}

			TextView tv = (TextView) view.findViewById(R.id.column_3);
			if (startRow || player2Change == 0) {
				tv.setText(Integer.toString(player2ActualInfluence));
			} else {
				tv.setText(TextUtils.concat(Integer.toString(player2ActualInfluence), Formatter.colorize(" (%1$s)", player2Change, mContext)));
			}
			
		}
		
		/**
		 * Finds the last occurence of a players influence.
		 * Invariant: It is assumed the cursor is positioned at the last element,
		 * so searching is only backwards.
		 * 
		 * The last element is also ignored
		 * 
		 * @param c			Cursor to search (list of game_state_changes)
		 * @param player	Player id
		 * @return	Players influence
		 */
		private int getPlayerInfluence(Cursor c, int player) {
			
			int playerInfluence = 0;
			int startPosition = c.getPosition();

			c.moveToPrevious(); // Ignore last element
			while(!c.isBeforeFirst()) {
				int rowPlayerId = c.getInt(c.getColumnIndexOrThrow(Database.COLUMN_PLAYER_ID));
				if (rowPlayerId == player) {
					playerInfluence = c.getInt(c.getColumnIndexOrThrow(Database.COLUMN_INFLUENCE));
					break;
				}
				
				c.moveToPrevious();
			}
			
			c.moveToPosition(startPosition);
			return playerInfluence;
		}
		
	}
}
