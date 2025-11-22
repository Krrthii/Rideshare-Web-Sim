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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;


public class SimpleHttpServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Serve static HTML pages
        server.createContext("/login", new StaticFileHandler("login.html"));
        server.createContext("/register", new StaticFileHandler("register.html"));
        server.createContext("/requestride", new StaticFileHandler("requestride.html")); // GET: serve page
        server.createContext("/findrider", new StaticFileHandler("findrider.html"));
        server.createContext("/createBooking", new createBookingHandler());       // POST: handle data
        server.createContext("/checkBookingStatus", new CheckBookingStatusHandler());
        server.createContext("/cancelBooking", new CancelBookingHandler());
        server.createContext("/checkUser", new CheckUserHandler());
        server.createContext("/registerUser", new RegisterUserHandler());

        server.createContext("/searchRider", new searchRiderHandler());
        server.createContext("/acceptBooking", new acceptBookingHandler());
        server.createContext("/updateDriverStatus", new updateDriverStatusHandler());

        // root redirects to login
        server.createContext("/", new RedirectHandler("/login"));

        server.setExecutor(null);
        server.start();
        System.out.println("Server started at http://localhost:8080");
    }

    static class createBookingHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            JSONObject json = new JSONObject(body);

            int success = DBHelper.insertBooking(
                json.getString("username"),
                json.getString("pickup"),
                json.getString("destination"));

            String response = String.valueOf(success);

            if (success == -1) {
                exchange.sendResponseHeaders(500, response.getBytes().length); // triggers .catch()
            } else {
                exchange.sendResponseHeaders(200, response.getBytes().length); // triggers .then()
            }

            //exchange.sendResponseHeaders(200, response.getBytes().length);
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

static class CheckBookingStatusHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery(); // e.g., "booking_id=123"
            Map<String, String> params = new HashMap<>();
            for (String pair : query.split("&")) {
                String[] parts = pair.split("=");
                if (parts.length == 2) {
                    params.put(parts[0], parts[1]);
                }
            }

            String response;
            try {
                int bookingId = Integer.parseInt(params.get("booking_id"));
                JSONObject result = DBHelper.getBookingStatus(bookingId);
                response = result.toString();
                //System.out.println(response);
                exchange.sendResponseHeaders(200, response.getBytes().length);
            } catch (Exception e) {
                e.printStackTrace();
                response = "{\"error\":\"Unable to retrieve booking status.\"}";
                exchange.sendResponseHeaders(500, response.getBytes().length);
            }

            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }
}

static class CancelBookingHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery(); // e.g., "booking_id=123"
            Map<String, String> params = new HashMap<>();
            for (String pair : query.split("&")) {
                String[] parts = pair.split("=");
                if (parts.length == 2) {
                    params.put(parts[0], parts[1]);
                }
            }

            String response;
            int bookingId = Integer.parseInt(params.get("booking_id"));
            boolean result = DBHelper.cancelBooking(bookingId);
            if (result) {
                response = "OK";
            } else {
                response = "FAIL";
            }

            exchange.sendResponseHeaders(200, response.getBytes().length);

            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }
}

static class searchRiderHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {

            String response;
            try {
                List<RiderBooking> result_bookings = DBHelper.searchRider();

                JSONObject result = new JSONObject();
                if (result_bookings.isEmpty()) {
                    result.put("status", "not_found");
                    result.put("bookings", new JSONArray()); // empty array
                } else {
                    result.put("status", "found");
                    JSONArray bookings = new JSONArray();
                    for (RiderBooking b : result_bookings) {
                        JSONObject obj = new JSONObject();
                        obj.put("bookingId", b.getBookingId());
                        obj.put("riderId", b.getRiderId());
                        obj.put("pickup", b.getPickup());
                        obj.put("destination", b.getDestination());
                        bookings.put(obj);
                    }
                    result.put("bookings", bookings);
                }

                response = result.toString();
                System.out.println(response);
                exchange.sendResponseHeaders(200, response.getBytes().length);

            } catch (Exception e) {
                e.printStackTrace();
                response = "{\"error\":\"Unable to retrieve booking status.\"}";
                exchange.sendResponseHeaders(500, response.getBytes().length);
            }

            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }
}


static class acceptBookingHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(body);
            String bookingId = json.getString("bookingId");

            boolean success = false;

            success = DBHelper.acceptBooking(Integer.parseInt(bookingId));
            
            String response = success ? "Booking successfully accepted" : "Booking acceptance failed.";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }
}

static class updateDriverStatusHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(body);
            String username = json.getString("username");
            String search_status = json.getString("search_status");

            boolean success = false;

            success = DBHelper.updateDriverStatus(username, search_status);
            
            String response = success ? "Driver status successfully changed" : "Driver status change unsuccessful.";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }
}

}