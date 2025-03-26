package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import endpoints.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import static java.sql.Types.NULL;

public class SQLDataAccess implements UserDAO, GameDAO, AuthDAO {
    public SQLDataAccess() throws ResponseException {
        configureDatabase();
    }

    private void executeUpdate(String statement, Object... params) throws ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            var ps = conn.prepareStatement(statement);
            for (var i = 0; i < params.length; i++) {
                var param = params[i];
                switch (param) {
                    case String p -> ps.setString(i + 1, p);
                    case Integer p -> ps.setInt(i + 1, p);
                    case ChessGame p -> {
                        var json = new Gson().toJson(p);
                        ps.setString(i + 1, json);
                    }
                    case null -> ps.setNull(i + 1, NULL);
                    default -> {
                    }
                }
            }
            ps.executeUpdate();
            ps.close();
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM auth WHERE authToken= ?;";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(authToken, rs.getString("username"));
                    }
                }
            }
            throw new DataAccessException("Error: unauthorized"); //authToken wasn't found
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        } catch (Exception e) {
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
    }

    @Override
    public void createAuth(AuthData authData) {
        var statement = "INSERT INTO auth VALUES (?, ?)";
        executeUpdate(statement, authData.authToken(), authData.username());
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        try {
            getAuth(authToken);
            var statement = "DELETE FROM auth WHERE authToken=?";
            executeUpdate(statement, authToken);
        } catch(Exception e) {
            throw new DataAccessException("Error: unauthorized");
        }
    }

    private GameData readGameFromJson(ResultSet rs) {
        try {
            int gameID = rs.getInt("gameID");
            String whiteUsername = rs.getString("whiteUsername");
            String blackUsername = rs.getString("blackUserName");
            String gameName = rs.getString("gameName");
            //deserialize json into ChessGame
            var builder = new GsonBuilder();
            var chessGame = builder.create().fromJson(rs.getString("game"), ChessGame.class);

            return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
        } catch (Exception e) {
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games WHERE gameID= ?;";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGameFromJson(rs);
                    } else {
                        throw new DataAccessException("Error: game doesn't exist");
                    }
                }
            }
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        } catch (Exception e) {
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
    }

    @Override
    public Collection<GameData> listGames() {
        var result = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM games;";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readGameFromJson(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return result;
    }

    @Override
    public void createGame(GameData gameData) {
        var statement = "INSERT INTO games VALUES (?, ?, ?, ?, ?)";
        executeUpdate(statement, gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game());
    }

    @Override
    public void deleteGame(int gameID) throws DataAccessException {
        try {
            getGame(gameID);
            var statement = "DELETE FROM games WHERE gameID=?";
            executeUpdate(statement, gameID);
        } catch(Exception e) {
            throw new DataAccessException("Error: game doesn't exist");
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM users WHERE username= ?;";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(username, rs.getString("password"), rs.getString("email"));
                    }
                }
            }
            throw new DataAccessException("Error: user doesn't exist"); //user is not in the database
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        } catch (Exception e) {
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
    }

    @Override
    public void createUser(UserData userData) {
        var statement = "INSERT INTO users VALUES (?, ?, ?)";
        executeUpdate(statement, userData.username(), userData.password(), userData.email());
    }

    @Override
    public void clear() {
        var statement = "TRUNCATE users";
        executeUpdate(statement);
        statement = "TRUNCATE auth";
        executeUpdate(statement);
        statement = "TRUNCATE games";
        executeUpdate(statement);
    }

    private void configureDatabase() throws ResponseException {
        try {
            DatabaseManager.createDatabase();
            try (var conn = DatabaseManager.getConnection()) {
                for (var statement : createStatements) {
                    try (var preparedStatement = conn.prepareStatement(statement)) {
                        preparedStatement.executeUpdate();
                    }
                }
            } catch (SQLException ex) {
                throw new ResponseException(500, String.format("Unable to configure database: %s", ex.getMessage()));
            }
        } catch (DataAccessException e) {
            throw new ResponseException(500, String.format("Unable to configure database: %s", e.getMessage()));
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS users (
              username VARCHAR(255) NOT NULL,
              password VARCHAR(255) NOT NULL,
              email VARCHAR(255) NOT NULL,
              PRIMARY KEY (username)
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS auth (
              authToken VARCHAR(255) NOT NULL,
              username VARCHAR(255) NOT NULL,
              PRIMARY KEY (authToken)
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS games (
              gameID int NOT NULL,
              whiteUsername VARCHAR(255) DEFAULT NULL,
              blackUsername VARCHAR(255) DEFAULT NULL,
              gameName VARCHAR(255) NOT NULL,
              game TEXT NOT NULL,
              PRIMARY KEY (gameID)
            );
            """
    };
}
