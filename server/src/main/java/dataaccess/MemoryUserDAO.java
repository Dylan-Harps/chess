package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.List;

public class MemoryUserDAO implements UserDAO {
    List<UserData> userDatabase;

    public UserData getUser(String username) {
        for (UserData u : userDatabase) {
            if (u.username().equals(username)) {
                return u;
            }
        }
        return null;
    }

    public void createUser(UserData userData) {
        userDatabase.add(userData);
    }

    public void clearAllUserData() {
        userDatabase.clear();
    }
}
