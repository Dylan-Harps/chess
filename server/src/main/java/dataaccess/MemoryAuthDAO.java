package dataaccess;

import model.AuthData;

import java.util.ArrayList;
import java.util.List;

public class MemoryAuthDAO implements AuthDAO {
    List<AuthData> authDatabase = new ArrayList<>();

    public AuthData getAuth(String authToken) throws DataAccessException {
        for (AuthData a : authDatabase) {
            if (a.authToken().equals(authToken)) {
                return a;
            }
        }
        throw new DataAccessException("Error: unauthorized");
    }

    public void createAuth(AuthData authData) {
        authDatabase.add(authData);
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        authDatabase.remove(getAuth(authToken));
    }

    public void clear() {
        authDatabase.clear();
    }
}
