package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    public GameData getGame(int gameID);
    public Collection<GameData> listGames();

    public void createGame(GameData gameData);

    public void deleteGame(int gameID);

    public GameData updateGame(GameData gameData);

    public void clearAllGameData();
}
