package dataaccess;

import model.AuthData;

public interface AuthDAO {
    public AuthData getAuth(String authToken) throws DataAccessException;

    public void createAuth(AuthData authData);

    public void deleteAuth(String authToken) throws DataAccessException;

    public void clearAllAuthData();
}
