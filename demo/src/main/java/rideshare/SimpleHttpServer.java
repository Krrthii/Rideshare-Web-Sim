package rideshare;

import rideshare.DBHelper;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;


public class SimpleHttpServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new HelloHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started at http://localhost:8080");
    }

    static class HelloHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String response;
        int statusCode;

        try {
            String html = Files.readString(
                Paths.get(System.getProperty("user.dir"), "demo", "webcontent", "home.html"),
                StandardCharsets.UTF_8
            );
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            response = html;
            statusCode = 200;

            // DBHelper
            boolean success = DBHelper.insertDestination("Test Destination");

        } catch (IOException e) {
            e.printStackTrace(); // log the error
            response = "<h1>500 Internal Server Error</h1><p>Could not load HTML file.</p>";
            statusCode = 500;
        }

        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
        }
    }

}