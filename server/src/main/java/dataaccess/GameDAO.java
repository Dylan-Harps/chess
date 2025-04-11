package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;

public interface GameDAO {
    public GameData getGame(int gameID) throws DataAccessException;

    public Collection<GameData> listGames();

    public void createGame(GameData gameData);

    public void deleteGame(int gameID)  throws DataAccessException;

    public void updateGame(int gameID, ChessGame updatedGame)  throws DataAccessException;

    public void clear();
}
