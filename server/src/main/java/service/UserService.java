package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class UserService {
    MemoryAuthDAO authDataBase = new MemoryAuthDAO();
    MemoryUserDAO userDataBase = new MemoryUserDAO();

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public RegisterResult register(RegisterRequest registerRequest) {
        //if registerRequest is missing data, throw error 400 "bad request"
        if (registerRequest.username() == null
                || registerRequest.username().isEmpty()
                || registerRequest.password() == null
                || registerRequest.password().isEmpty()
                || registerRequest.email() == null
                || registerRequest.email().isEmpty()) {
            throw new RuntimeException("bad request");
        }
        try {
            userDataBase.getUser(registerRequest.username());
        } catch (DataAccessException e) {
            //If getUser() throws an error, then the username is not in the system and can be added
            UserData userData = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
            userDataBase.createUser(userData);

            String authToken = generateToken();
            AuthData authData = new AuthData(authToken, registerRequest.username());
            authDataBase.createAuth(authData);

            return new RegisterResult(registerRequest.username(), authToken);
        }
        //if getUser() returns without an error, then the username is already taken
        throw new RuntimeException("already taken");
    }

    public LoginResult login(LoginRequest loginRequest) {
        //TODO
        return null;
    }

    public void logout(LogoutRequest logoutRequest) {
        //TODO
    }
}
