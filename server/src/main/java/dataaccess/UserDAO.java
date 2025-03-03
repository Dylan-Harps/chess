package dataaccess;

import model.AuthData;
import model.UserData;

public interface UserDAO {
    public UserData getUser(String username) throws DataAccessException;

    public void createUser(UserData userData);

    public void clearAllUserData();
}
