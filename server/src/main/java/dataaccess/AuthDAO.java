package dataaccess;

import model.AuthData;

public interface AuthDAO {
    public AuthData getAuth(String authToken);

    //public AuthData authenticateUser(String username);

    public void createAuth(AuthData authData);

    public void deleteAuth(String authToken);

    public void clearAllAuthData();
}
