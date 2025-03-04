package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    public GameData getGame(int gameID) throws DataAccessException;

    public Collection<GameData> listGames();

    public void createGame(GameData gameData);

    public void deleteGame(int gameID)  throws DataAccessException;

    public GameData updateGame(GameData gameData)  throws DataAccessException;

    public void clearAllGameData();
}
