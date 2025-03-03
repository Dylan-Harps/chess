package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MemoryGameDAO implements GameDAO {
    List<GameData> gameDatabase = new ArrayList<>();

    public GameData getGame(int gameID) {
        for (GameData g : gameDatabase) {
            if (g.gameID() == gameID) {
                return g;
            }
        }
        return null;
    }

    public Collection<GameData> listGames() {
        return gameDatabase;
    }

    public void createGame(GameData gameData) {
        gameDatabase.add(gameData);
    }

    public void deleteGame(int gameID) {
        gameDatabase.remove(getGame(gameID));
    }

    public GameData updateGame(GameData gameData) {
        GameData old = getGame(gameData.gameID());
        if (old != null) {
            gameDatabase.remove(old);
        }
        createGame(gameData);
        return gameData;
    }

    public void clearAllGameData() {
        gameDatabase.clear();
    }
}
