package rideshare;

import rideshare.DBHelper;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;


public class SimpleHttpServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Serve static HTML pages
        server.createContext("/login", new StaticFileHandler("login.html"));
        server.createContext("/register", new StaticFileHandler("register.html"));
        server.createContext("/requestride", new StaticFileHandler("requestride.html")); // GET: serve page
        server.createContext("/findride", new StaticFileHandler("findride.html"));
        server.createContext("/submitDestination", new SubmitHandler());       // POST: handle data
        server.createContext("/checkUser", new CheckUserHandler());
        server.createContext("/registerUser", new RegisterUserHandler());

        // root redirects to login
        server.createContext("/", new RedirectHandler("/login"));

        server.setExecutor(null);
        server.start();
        System.out.println("Server started at http://localhost:8080");
    }

    static class SubmitHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            JSONObject json = new JSONObject(body);
            String username = json.getString("username");
            String destination = json.getString("destination");

            boolean success = DBHelper.insertDestination(username, destination);

            String response = "";

            if (success) {
                response = "Destination saved!";
            } else {
                response = "Failed to save.";
            }

            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
        }
    }

    static class StaticFileHandler implements HttpHandler {
    private final String filename;

    public StaticFileHandler(String filename) {
        this.filename = filename;
    }

    public void handle(HttpExchange exchange) throws IOException {
        File file = new File("demo/webcontent", filename);
        byte[] bytes = Files.readAllBytes(file.toPath());

        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}

static class RedirectHandler implements HttpHandler {
    private final String target;

    public RedirectHandler(String target) {
        this.target = target;
    }

    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Location", target);
        exchange.sendResponseHeaders(302, -1); // HTTP 302 Found
    }
}

static class CheckUserHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(body);
            String username = json.getString("username");

            boolean exists = false;

            exists = DBHelper.riderExists(username);

            String response = "";

            if (exists) {
                response = "OKRider";
            } else {
                exists = DBHelper.driverExists(username);
                if (exists) {
                    response = "OKDriver";
                }
                else {
                    response = "NOT_FOUND";
                }
            }

            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }
}

static class RegisterUserHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(body);

            boolean success = false;

            System.out.println(json);

            if (json.getString("role").equals("rider")) {
                success = DBHelper.insertRider(
                json.getString("username"),
                json.getString("name"),
                json.getString("email"),
                json.getString("phone"),
                json.getString("payment")
            );
            }
            
            if (json.getString("role").equals("driver")) {
                success = DBHelper.insertDriver(
                json.getString("username"),
                json.getString("name"),
                json.getString("email"),
                json.getString("phone"),
                json.getString("bank"),
                json.getString("vehicle")
            );
            }
            

            String response = success ? "Registration successful! Sending you back to login..." : "Registration failed.";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }
}

}