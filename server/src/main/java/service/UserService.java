package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import handler.ResponseException;
import model.AuthData;
import model.UserData;

import javax.xml.crypto.Data;
import java.util.UUID;

public class UserService {
    MemoryAuthDAO authDataBase = new MemoryAuthDAO();
    MemoryUserDAO userDataBase = new MemoryUserDAO();

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public RegisterResult register(RegisterRequest registerRequest) throws ResponseException {
        //if registerRequest is missing data, throw error 400 "bad request"
        if (registerRequest.username() == null
                || registerRequest.username().isEmpty()
                || registerRequest.password() == null
                || registerRequest.password().isEmpty()
                || registerRequest.email() == null
                || registerRequest.email().isEmpty()) {
            throw new ResponseException(400, "bad request");
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
        throw new ResponseException(403, "already taken");
    }

    public LoginResult login(LoginRequest loginRequest) throws ResponseException {
        //if the request is missing data, throw an exception
        if (loginRequest.username() == null
                || loginRequest.username().isEmpty()
                || loginRequest.password() == null
                || loginRequest.password().isEmpty()) {
            throw new ResponseException(401, "unauthorized");
        }

        //check if username and password are correct
        UserData userData;
        try {
            userData = userDataBase.getUser(loginRequest.username());
        } catch (DataAccessException e) {
            throw new ResponseException(500, e.getMessage());
        }
        if (!userData.password().equals(loginRequest.password())) {
            throw new ResponseException(401, "unauthorized");
        }

        //check if user is already logged in
        AuthData authData;
        try {
            authData = authDataBase.authenticateUser(loginRequest.username());
        } catch(DataAccessException e) {
            //if a DataAccessException is thrown, then the user is not logged in and can be logged in now
            String authToken = generateToken();
            authDataBase.createAuth(new AuthData(authToken, loginRequest.username()));
            return new LoginResult(loginRequest.username(), authToken);
        }
        throw new ResponseException(500, "already logged in");
    }

    public LogoutResult logout(LogoutRequest logoutRequest) {
        //if the request is missing data, throw an exception
        if (logoutRequest.authToken() == null
                || logoutRequest.authToken().isEmpty()) {
            throw new ResponseException(401, "unauthorized");
        }

        //log out the user
        try {
            authDataBase.deleteAuth(logoutRequest.authToken());
        } catch (DataAccessException e) {
            throw new ResponseException(401, "unauthorized");
        }

        return new LogoutResult();
    }
}
