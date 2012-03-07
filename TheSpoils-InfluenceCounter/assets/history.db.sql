/*
 * Database schema for the History database
 * v1		: Initial version
 *
 * @author Christian Melchior <christian@ilios.dk>
 */
 
CREATE TABLE games ( 
	_ID INTEGER PRIMARY KEY, 
	name TEXT NOT NULL, 
	players INTEGER NOT NULL,
);

CREATE TABLE game_state_changes (
	_ID INTEGER PRIMARY KEY,
	game_id INTEGER NOT NULL,
	player INTEGER NOT NULL,
	timestamp INTEGER NOT NULL,
	influence INTEGER NOT NULL	
);