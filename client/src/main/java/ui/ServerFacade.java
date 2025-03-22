package ui;

import chess.ChessGame;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;

public class ServerFacade {
    HttpURLConnection http;

    public ServerFacade() throws Exception {
        // Specify the desired endpoint
        URI uri = new URI("http://localhost:3306/chess");
        http = (HttpURLConnection) uri.toURL().openConnection();
    }

    public void example() throws Exception {
        // Make the request
        http.setRequestMethod("GET");
        http.connect();

        // Output the response body
        try (InputStream respBody = http.getInputStream()) {
            InputStreamReader inputStreamReader = new InputStreamReader(respBody);
            System.out.println(new Gson().fromJson(inputStreamReader, Map.class));
        }

        //example of outputting to error stream or output stream
        // Handle bad HTTP status
        var status = http.getResponseCode();
        if ( status >= 200 && status < 300) {
            try (InputStream in = http.getInputStream()) {
                System.out.println(new Gson().fromJson(new InputStreamReader(in), Map.class));
            }
        } else {
            try (InputStream in = http.getErrorStream()) {
                System.out.println(new Gson().fromJson(new InputStreamReader(in), Map.class));
            }
        }
    }

    private void makeRequest() throws Exception {
        // Specify that we are going to write out data
        http.setDoOutput(true);

        // Write out a header
        http.addRequestProperty("HeaderName", "HeaderContents");

        // Write out the body
        var body = Map.of("name", "joe", "type", "cat");
        try (var outputStream = http.getOutputStream()) {
            var jsonBody = new Gson().toJson(body);
            outputStream.write(jsonBody.getBytes());
        }
    }
}
