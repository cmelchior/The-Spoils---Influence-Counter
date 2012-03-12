
/* Game 1  with 1 player */
INSERT INTO games (name, players) VALUES ('Game 1', 1);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (1, 1, 1000, 25);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (1, 1, 1000, 24);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (1, 1, 31000, 20);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (1, 1, 100000, 9);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (1, 1, 150000, 8);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (1, 1, 160000, 7);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (1, 1, 175000, 6);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (1, 1, 200000, 10);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (1, 1, 400000, 9);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (1, 1, 530000, 0);

/* Game 2 with 2 players */
INSERT INTO games (name, players) VALUES ('Game 2', 2);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (2, 1, 1000, 25);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (2, 2, 1000, 25);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (2, 1, 5000, 24);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (2, 1, 10000, 23);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (2, 2, 15000, 24);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (2, 2, 35000, 20);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (2, 1, 60000, 19);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (2, 2, 90000, 16);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (2, 1, 100000, 10);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (2, 2, 200000, 13);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (2, 1, 230000, 5);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (2, 1, 270000, 2);
INSERT INTO game_state_changes(game_id, player, timestamp, influence) VALUES (2, 1, 300000, 0);
