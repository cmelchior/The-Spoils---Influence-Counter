package dk.ilios.influencecounter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
	private BaseAdapter mAdapter;

	private Button mGameNameView;
	
	private TextView totalGainedColumn3;
	private TextView totalLostColumn2;
	private TextView totalGainedColumn2;
	private TextView totalLostColumn3;
	
	private int player1TotalGained = 0;
	private int player1TotalLost = 0;
	private int player2TotalGained = 0;
	private int player2TotalLost = 0;
	
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
		
		if (mPlayers == 2) {
			mAdapter = new TwoPlayerHistoryAdapter(getActivity(), 0);
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

		mGameNameView = (Button) v.findViewById(R.id.game_name);
		mGameNameView.setText(mGameName);
		mGameNameView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				TextView button = (TextView) v;
				switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					button.setShadowLayer(3, -5, 0, getResources().getColor(R.color.button_shadow_color));
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					button.setShadowLayer(0,0,0,0);
					break;
				}
				return false;
			}
		});
		
		mGameNameView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				View dialogView = getActivity().getLayoutInflater().inflate(R.layout.edit_name_dialog, null);
				final EditText input = (EditText) dialogView.findViewById(R.id.edit_name_input);
				input.setText(mGameName);
				
				new AlertDialog.Builder(getActivity())
			    .setTitle(R.string.game_name_dialog_title)
			    .setView(dialogView)
			    .setPositiveButton(getText(R.string.dialog_ok), new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			        	mGameName = input.getText().toString(); 
			        	mGameNameView.setText(mGameName);
			        	GameTracker.updateGameName(mGameId, mGameName);
			        }
			    }).setNegativeButton(getText(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			            // Do nothing.
			        }
			    }).show();				
			}
		});
		
		// Summary
		View totalGainedRow = v.findViewById(R.id.total_gained);
		View totalLostRow = v.findViewById(R.id.total_lost);
		
		((TextView)totalGainedRow.findViewById(R.id.column_1)).setText(R.string.total_gained);
		totalGainedColumn2 = (TextView)totalGainedRow.findViewById(R.id.column_2);
		totalGainedColumn3 = (TextView) totalGainedRow.findViewById(R.id.column_3);
		
		((TextView)totalLostRow.findViewById(R.id.column_1)).setText(R.string.total_lost);
		totalLostColumn2 = (TextView)totalLostRow.findViewById(R.id.column_2);
		totalLostColumn3 = (TextView)totalLostRow.findViewById(R.id.column_3);

		if (mPlayers == 1) {
			totalGainedColumn2.setText("");
			totalGainedColumn3.setText(Formatter.colorize(0, getActivity()));
			totalLostColumn2.setText("");
			totalLostColumn3.setText(Formatter.colorizeNegative(null, "-0", getActivity()));
		} else {
			totalGainedColumn2.setText(Formatter.colorize(0, getActivity()));
			totalGainedColumn3.setText(Formatter.colorize(0, getActivity()));
			totalLostColumn2.setText(Formatter.colorizeNegative(null, "-0", getActivity()));
			totalLostColumn3.setText(Formatter.colorizeNegative(null, "-0", getActivity()));
		}
		
		totalGainedColumn3.setText(Formatter.colorize(0, getActivity()));
			
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
		if (mCurrentCursor != null) {
			mCurrentCursor.unregisterContentObserver(observer);
			mCurrentCursor.close();
		}
		
		mCurrentCursor = data;
		mCurrentCursor.registerContentObserver(observer);

		// Calculate summary values
		player1TotalGained = 0;
		player1TotalLost = 0;
		player2TotalGained = 0;
		player2TotalLost = 0;

		int startPosition = mCurrentCursor.getPosition();
		int lastPlayer1Influence = Integer.MAX_VALUE;
		int lastPlayer2Influence = Integer.MAX_VALUE;
		
		while (mCurrentCursor.moveToNext()) {
			int player = mCurrentCursor.getInt(mCurrentCursor.getColumnIndexOrThrow(Database.COLUMN_PLAYER_ID));
			int influence = mCurrentCursor.getInt(mCurrentCursor.getColumnIndexOrThrow(Database.COLUMN_INFLUENCE));
			
			if (player == 0) {
				if (lastPlayer1Influence != Integer.MAX_VALUE) {
					int diff = influence - lastPlayer1Influence;
					if (diff < 0) {
						player1TotalLost += diff;
					} else {
						player1TotalGained += diff;
					}
				}
				
				lastPlayer1Influence = influence;
				
			} else if (player == 1) {
				if (lastPlayer2Influence != Integer.MAX_VALUE) {
					int diff = influence - lastPlayer2Influence;
					if (diff < 0) {
						player2TotalLost += diff;
					} else {
						player2TotalGained += diff;
					}
				}
				
				lastPlayer2Influence = influence;
			}
		}
		
		mCurrentCursor.moveToPosition(startPosition);
		
		if (mPlayers == 1) {
			totalGainedColumn3.setText(Formatter.colorizePositive(null, "+" + Integer.toString(player1TotalGained), getActivity()));
			totalLostColumn3.setText(Formatter.colorizeNegative(null, (player1TotalLost == 0) ? "-" + player1TotalLost : Integer.toString(player1TotalLost), getActivity()));
		 	
		} else if (mPlayers == 2) {
			totalGainedColumn2.setText(Formatter.colorizePositive(null, "+" + Integer.toString(player1TotalGained), getActivity()));
			totalLostColumn2.setText(Formatter.colorizeNegative(null, (player1TotalLost == 0) ? "-" + player1TotalLost : Integer.toString(player1TotalLost), getActivity()));
			totalGainedColumn3.setText(Formatter.colorizePositive(null, "+" + Integer.toString(player2TotalGained), getActivity()));
			totalLostColumn3.setText(Formatter.colorizeNegative(null, (player2TotalLost == 0) ? "-" + player2TotalLost : Integer.toString(player2TotalLost), getActivity()));
			
		}
		
		// Insert new cursor
		if (mPlayers == 1 && mAdapter instanceof CursorAdapter) {
			Cursor c = ((CursorAdapter) mAdapter).swapCursor(mCurrentCursor);
			if (c != null && !c.isClosed()) {	
				c.close();
			}
		} else {
			((ArrayAdapter<TwoPlayerRow>) mAdapter).clear();
			((ArrayAdapter<TwoPlayerRow>) mAdapter).addAll(createTwoPlayerRows());
			
		}
	}
	
	private ArrayList<TwoPlayerRow> createTwoPlayerRows() {
		Cursor c = mCurrentCursor;
		ArrayList<TwoPlayerRow> rows = new ArrayList<GameHistoryFragment.TwoPlayerRow>();

		int columnTimestamp = c.getColumnIndex(Database.COLUMN_TIMESTAMP);
		int columnPlayer = c.getColumnIndex(Database.COLUMN_PLAYER_ID);
		int columnInfluence = c.getColumnIndex(Database.COLUMN_INFLUENCE);
		
		int lastPlayer1Influence = 0;
		int lastPlayer2Influence = 0;
		
		// Invariant 1st row is player 1 start value, 2nd row is player 2
		c.moveToFirst();
		long timestamp = c.getInt(columnTimestamp);
		int player1StartInfluence = lastPlayer1Influence = c.getInt(columnInfluence);
		c.moveToNext();
		int player2StartInfluence = lastPlayer2Influence = c.getInt(columnInfluence);
		
		rows.add(new TwoPlayerRow(timestamp, player1StartInfluence, 0, player2StartInfluence, 0));
		
		while(c.moveToNext()) {
			int player = c.getInt(columnPlayer);
			int influence = c.getInt(columnInfluence);
			timestamp = c.getInt(columnTimestamp);
			
			if (player == 0) {
				int diff = influence - lastPlayer1Influence;
				if (diff < 0) {
					player1TotalLost += diff;
				} else {
					player1TotalGained += diff;
				}
				
				lastPlayer1Influence = influence;
				if (mPlayers == 1) {
					rows.add(new TwoPlayerRow(timestamp, lastPlayer1Influence, diff, 0, 0));
				} else { 
					rows.add(new TwoPlayerRow(timestamp, lastPlayer1Influence, diff, lastPlayer2Influence, 0));
				}
				
			} else if (player == 1) {
				int diff = influence - lastPlayer2Influence;
				if (diff < 0) {
					player2TotalLost += diff;
				} else {
					player2TotalGained += diff;
				}
				
				lastPlayer2Influence = influence;
				rows.add(new TwoPlayerRow(timestamp, lastPlayer1Influence, 0, lastPlayer2Influence, diff));
			}
		}
		
		return rows;
	}
	

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (mAdapter != null && mAdapter instanceof CursorAdapter) {
			Cursor c = ((CursorAdapter) mAdapter).swapCursor(null);
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
 * TWO PLAYER ROW ADAPTER                                                   *
 ******************************************************************************/
	public static class TwoPlayerRow {
		public long timestamp;
		public int player1Influence;
		public int player1Change;
		public int player2Influence;
		public int player2Change;
		
		public TwoPlayerRow(long timestamp, int player1Influence, int player1Change, int player2Influence, int player2Change) {
			this.timestamp = timestamp;
			this.player1Influence = player1Influence;
			this.player1Change = player1Change;
			this.player2Influence = player2Influence;
			this.player2Change = player2Change;
		}
	}
	
	private static class TwoPlayerHistoryAdapter extends ArrayAdapter<TwoPlayerRow> {

		private LayoutInflater mInflater;
		private Context mContext;
		private DateFormat mTimeFormatter;

		public TwoPlayerHistoryAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			mInflater = LayoutInflater.from(context);
			mContext = context;
			mTimeFormatter = Formatter.getTimeFormatter(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = (convertView != null) ? convertView : mInflater.inflate(R.layout.history_row, null);

			if (position % 2 == 0) {
				v.setBackgroundColor(mContext.getResources().getColor(R.color.history_row_background_1));
			} else {
				v.setBackgroundColor(mContext.getResources().getColor(R.color.history_row_background_2));
			}

			TwoPlayerRow item = getItem(position);

			TextView c1 = (TextView) v.findViewById(R.id.column_1);
			TextView c2 = (TextView) v.findViewById(R.id.column_2);
			TextView c3 = (TextView) v.findViewById(R.id.column_3);
			
			c1.setText(mTimeFormatter.format(new Date(item.timestamp)));
			
			if (item.player1Change != 0) {
				c2.setText(TextUtils.concat(Integer.toString(item.player1Influence), Formatter.colorize(" (%1$s)", item.player1Change, mContext)));
			} else {
				c2.setText(Integer.toString(item.player1Influence));
			}
			
			if (item.player2Change != 0) {
				c3.setText(TextUtils.concat(Integer.toString(item.player2Influence), Formatter.colorize(" (%1$s)", item.player2Change, mContext)));
			} else {
				c3.setText(Integer.toString(item.player2Influence));
			}
			
			return v;
		}
		
		
		
//		@Override
//		public void bindView(View view, Context context, Cursor cursor) {
//			if (cursor.getPosition() % 2 == 0) {
//				view.setBackgroundColor(mContext.getResources().getColor(R.color.history_row_background_1));
//			} else {
//				view.setBackgroundColor(mContext.getResources().getColor(R.color.history_row_background_2));
//			}
//
//			int player1ActualInfluence = 0;
//			int player1Change = 0;
//			int player2ActualInfluence = 0;
//			int player2Change = 0;
//			
//			// Hide starting row
//			if (cursor.isFirst()) {
//				view.setVisibility(View.GONE);
//				return;
//			} else {
//				view.setVisibility(View.VISIBLE);
//			}
//			
//			// Assume 2nd row is the "starting row"
//			// Invariant: 2nd player is row 2, 1st player is row 1
//			
//			boolean startRow = false;
//			int player = cursor.getInt(cursor.getColumnIndexOrThrow(Database.COLUMN_PLAYER_ID));
//
//			if (cursor.getPosition() == 1) {
//				startRow = true;
//				player2ActualInfluence = cursor.getInt(cursor.getColumnIndexOrThrow(Database.COLUMN_INFLUENCE));
//				cursor.moveToPrevious();
//				player1ActualInfluence = cursor.getInt(cursor.getColumnIndexOrThrow(Database.COLUMN_INFLUENCE));
//				cursor.moveToNext();
//
//			} else {
//				if (player == 1) {
//					player1ActualInfluence = cursor.getInt(cursor.getColumnIndexOrThrow(Database.COLUMN_INFLUENCE));
//					player1Change = player1ActualInfluence - getPlayerInfluence(cursor, 1);
//					player2ActualInfluence = getPlayerInfluence(cursor, 2);
//							
//				} else {
//					player2ActualInfluence = cursor.getInt(cursor.getColumnIndexOrThrow(Database.COLUMN_INFLUENCE));
//					player2Change = player2ActualInfluence - getPlayerInfluence(cursor, 2);
//					player1ActualInfluence = getPlayerInfluence(cursor, 1);
//				}
//			}
//			
//			
//			TextView time = (TextView) view.findViewById(R.id.column_1);
//			time.setText();
//
//			TextView influenceChange = (TextView) view.findViewById(R.id.column_2);
//			if (startRow || player1Change == 0) {
//				influenceChange.setText(Integer.toString(player1ActualInfluence));
//			} else {
//				influenceChange.setText(TextUtils.concat(Integer.toString(player1ActualInfluence), Formatter.colorize(" (%1$s)", player1Change, mContext)));
//			}
//
//			TextView tv = (TextView) view.findViewById(R.id.column_3);
//			if (startRow || player2Change == 0) {
//				tv.setText(Integer.toString(player2ActualInfluence));
//			} else {
//				tv.setText(TextUtils.concat(Integer.toString(player2ActualInfluence), Formatter.colorize(" (%1$s)", player2Change, mContext)));
//			}
//			
//		}
//		
//		/**
//		 * Finds the last occurence of a players influence.
//		 * Invariant: It is assumed the cursor is positioned at the last element,
//		 * so searching is only backwards.
//		 * 
//		 * The last element is also ignored
//		 * 
//		 * @param c			Cursor to search (list of game_state_changes)
//		 * @param player	Player id
//		 * @return	Players influence
//		 */
//		private int getPlayerInfluence(Cursor c, int player) {
//			
//			int playerInfluence = 0;
//			int startPosition = c.getPosition();
//
//			c.moveToPrevious(); // Ignore last element
//			while(!c.isBeforeFirst()) {
//				int rowPlayerId = c.getInt(c.getColumnIndexOrThrow(Database.COLUMN_PLAYER_ID));
//				if (rowPlayerId == player) {
//					playerInfluence = c.getInt(c.getColumnIndexOrThrow(Database.COLUMN_INFLUENCE));
//					break;
//				}
//				
//				c.moveToPrevious();
//			}
//			
//			c.moveToPosition(startPosition);
//			return playerInfluence;
//		}
		
	}
}
