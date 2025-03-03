package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.List;

public class MemoryAuthDAO implements AuthDAO {
    List<AuthData> authDatabase;

    public AuthData getAuth(String authToken) {
        for (AuthData a : authDatabase) {
            if (a.authToken().equals(authToken)) {
                return a;
            }
        }
        return null;
    }

    /*
    public AuthData authenticateUser(String username) {
        return null;
    }
    */

    public void createAuth(AuthData authData) {
        authDatabase.add(authData);
    }

    public void deleteAuth(String authToken) {
        authDatabase.remove(getAuth(authToken));
    }

    public void clearAllAuthData() {
        authDatabase.clear();
    }
}
