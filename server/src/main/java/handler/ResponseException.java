package handler;

import com.google.gson.Gson;

import java.util.Map;

public class ResponseException extends RuntimeException {
    private final int status;

    public ResponseException(int status, String message) {
        super(message);
        this.status = status;
    }

    public String toJson() {
        return new Gson().toJson(Map.of("message", getMessage(), "status", status));
    }

    public int status() {
        return status;
    }
}
