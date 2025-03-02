package dataaccess;

import model.AuthData;
import model.UserData;

public interface UserDAO {
    public UserData getUser(String username);

    public void createUser(UserData userData);

    public AuthData authenticateUser(String username);

    public void clearAllUserData();
}
