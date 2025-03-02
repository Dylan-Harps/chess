package dataaccess;

import model.GameData;

public interface GameDAO {
    public GameData getGame(String gameID);

    public void createGame(GameData gameData);

    public void deleteGame(String gameID);

    public GameData updateGame(GameData gameData);

    public void clearAllGameData();
}
