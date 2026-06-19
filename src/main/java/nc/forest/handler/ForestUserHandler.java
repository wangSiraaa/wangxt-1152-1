package nc.forest.handler;

import com.sun.net.httpserver.HttpExchange;
import nc.forest.db.DatabaseManager;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class ForestUserHandler extends BaseHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        try {
            String action = getAction(exchange);
            if ("login".equals(action)) {
                login(exchange);
            } else if ("list".equals(action)) {
                listUsers(exchange);
            } else if ("query".equals(action)) {
                queryUser(exchange);
            } else {
                sendError(exchange, "未知的操作: " + action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, "服务器错误: " + e.getMessage());
        }
    }

    private void login(HttpExchange exchange) throws Exception {
        Map<String, Object> params = parseJsonBody(exchange);
        String userCode = (String) params.get("userCode");
        String password = (String) params.get("password");

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM forest_user WHERE user_code = ? AND dr = 0");
            ps.setString(1, userCode);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String dbPwd = rs.getString("password");
                if (password.equals(dbPwd)) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("pk_forest_user", rs.getString("pk_forest_user"));
                    user.put("user_code", rs.getString("user_code"));
                    user.put("user_name", rs.getString("user_name"));
                    user.put("user_role", rs.getInt("user_role"));
                    user.put("role_name", getRoleName(rs.getInt("user_role")));
                    user.put("phone", rs.getString("phone"));
                    user.put("pk_org", rs.getString("pk_org"));
                    user.put("pk_group", rs.getString("pk_group"));
                    sendSuccess(exchange, user);
                } else {
                    sendError(exchange, "密码错误");
                }
            } else {
                sendError(exchange, "用户不存在: " + userCode);
            }
            rs.close();
            ps.close();
        }
    }

    private String getRoleName(int role) {
        switch (role) {
            case 1: return "护林员";
            case 2: return "检疫员";
            case 3: return "处置队";
            default: return "未知";
        }
    }

    private void listUsers(HttpExchange exchange) throws Exception {
        Map<String, String> params = getQueryParams(exchange);
        String role = params.get("role");
        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = "SELECT * FROM forest_user WHERE dr = 0";
            if (role != null && !role.isEmpty()) {
                sql += " AND user_role = " + role;
            }
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("pk_forest_user", rs.getString("pk_forest_user"));
                user.put("user_code", rs.getString("user_code"));
                user.put("user_name", rs.getString("user_name"));
                user.put("user_role", rs.getInt("user_role"));
                user.put("role_name", getRoleName(rs.getInt("user_role")));
                user.put("phone", rs.getString("phone"));
                list.add(user);
            }
            rs.close();
            stmt.close();
        }
        sendSuccess(exchange, list);
    }

    private void queryUser(HttpExchange exchange) throws Exception {
        Map<String, String> params = getQueryParams(exchange);
        String pk = params.get("pk");
        if (pk == null || pk.isEmpty()) {
            sendError(exchange, "缺少参数pk");
            return;
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM forest_user WHERE pk_forest_user = ? AND dr = 0");
            ps.setString(1, pk);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("pk_forest_user", rs.getString("pk_forest_user"));
                user.put("user_code", rs.getString("user_code"));
                user.put("user_name", rs.getString("user_name"));
                user.put("user_role", rs.getInt("user_role"));
                user.put("role_name", getRoleName(rs.getInt("user_role")));
                user.put("phone", rs.getString("phone"));
                sendSuccess(exchange, user);
            } else {
                sendError(exchange, "用户不存在");
            }
            rs.close();
            ps.close();
        }
    }
}
