package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.ArrayList;
import java.util.List;

public class MemoryUserDAO implements UserDAO {
    List<UserData> userDatabase = new ArrayList<>();

    @Override
    public UserData getUser(String username)  throws DataAccessException {
        for (UserData u : userDatabase) {
            if (u.username().equals(username)) {
                return u;
            }
        }
        throw new DataAccessException("Error: user doesn't exist");
    }

    @Override
    public void createUser(UserData userData) {
        userDatabase.add(userData);
    }

    @Override
    public void clear() {
        userDatabase.clear();
    }
}
