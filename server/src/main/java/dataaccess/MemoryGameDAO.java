package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MemoryGameDAO implements GameDAO {
    List<GameData> gameDatabase = new ArrayList<>();

    public GameData getGame(int gameID) throws DataAccessException {
        for (GameData g : gameDatabase) {
            if (g.gameID() == gameID) {
                return g;
            }
        }
        throw new DataAccessException("game doesn't exist");
    }

    public Collection<GameData> listGames() {
        return gameDatabase;
    }

    public void createGame(GameData gameData) {
        gameDatabase.add(gameData);
    }

    public void deleteGame(int gameID)  throws DataAccessException {
        gameDatabase.remove(getGame(gameID));
    }

    public void clear() {
        gameDatabase.clear();
    }

    public void updateGame(int gameID, ChessGame updatedGame)  throws DataAccessException {
        GameData gameData = gameDatabase.get(gameID);
        GameData update = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), updatedGame);
        gameDatabase.set(gameID, update);
    }
}
