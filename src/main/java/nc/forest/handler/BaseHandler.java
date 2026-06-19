package nc.forest.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import nc.forest.db.DatabaseManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class BaseHandler implements HttpHandler {

    @Override
    public abstract void handle(HttpExchange exchange) throws IOException;

    protected static final ObjectMapper mapper = new ObjectMapper();
    protected static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected static final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

    protected void sendResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        Map<String, Object> response = new HashMap<>();
        response.put("success", statusCode == 200);
        response.put("data", data);
        response.put("code", statusCode);
        if (statusCode != 200) {
            response.put("message", data);
        }

        String json = mapper.writeValueAsString(response);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, userId, pkOrg, pkGroup");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendSuccess(HttpExchange exchange, Object data) throws IOException {
        sendResponse(exchange, 200, data);
    }

    protected void sendError(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, 500, message);
    }

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    protected Map<String, Object> parseJsonBody(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        if (body == null || body.isEmpty()) {
            return new HashMap<>();
        }
        return mapper.readValue(body, Map.class);
    }

    protected Map<String, String> getQueryParams(HttpExchange exchange) {
        Map<String, String> params = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    params.put(pair[0], pair[1]);
                } else {
                    params.put(pair[0], "");
                }
            }
        }
        return params;
    }

    protected String getHeader(HttpExchange exchange, String name) {
        List<String> values = exchange.getRequestHeaders().get(name);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

    protected String getAction(HttpExchange exchange) {
        Map<String, String> params = getQueryParams(exchange);
        String action = params.get("action");
        if (action == null) {
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length > 0) {
                action = parts[parts.length - 1];
            }
        }
        return action;
    }

    protected String now() {
        return sdf.format(new Timestamp(System.currentTimeMillis()));
    }

    protected String today() {
        return sdfDate.format(new Date());
    }

    protected String generatePk() {
        return DatabaseManager.generatePk();
    }

    protected void handleOptions(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, userId, pkOrg, pkGroup");
        exchange.sendResponseHeaders(204, -1);
    }
}
