package nc.forest.handler;

import com.sun.net.httpserver.HttpExchange;
import nc.forest.db.DatabaseManager;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class ForestTrapHandler extends BaseHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        try {
            String action = getAction(exchange);
            switch (action) {
                case "save":
                case "add":
                    saveTrap(exchange);
                    break;
                case "update":
                    updateTrap(exchange);
                    break;
                case "delete":
                    deleteTrap(exchange);
                    break;
                case "query":
                    queryTrap(exchange);
                    break;
                case "list":
                default:
                    listTraps(exchange);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, "服务器错误: " + e.getMessage());
        }
    }

    private void listTraps(HttpExchange exchange) throws Exception {
        Map<String, String> params = getQueryParams(exchange);
        String isKeyPatrol = params.get("is_key_patrol");
        String pkRanger = params.get("pk_ranger");
        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT t.*, u.user_name as ranger_name " +
                    "FROM forest_trap t LEFT JOIN forest_user u ON t.pk_ranger = u.pk_forest_user " +
                    "WHERE t.dr = 0");
            List<Object> args = new ArrayList<>();

            if (isKeyPatrol != null && !isKeyPatrol.isEmpty()) {
                sql.append(" AND t.is_key_patrol = ?");
                args.add(Integer.parseInt(isKeyPatrol));
            }
            if (pkRanger != null && !pkRanger.isEmpty()) {
                sql.append(" AND t.pk_ranger = ?");
                args.add(pkRanger);
            }
            sql.append(" ORDER BY t.trap_code");

            PreparedStatement ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> trap = new HashMap<>();
                trap.put("pk_forest_trap", rs.getString("pk_forest_trap"));
                trap.put("trap_code", rs.getString("trap_code"));
                trap.put("trap_name", rs.getString("trap_name"));
                trap.put("longitude", rs.getDouble("longitude"));
                trap.put("latitude", rs.getDouble("latitude"));
                trap.put("location_desc", rs.getString("location_desc"));
                trap.put("forest_type", rs.getString("forest_type"));
                trap.put("trap_type", rs.getString("trap_type"));
                trap.put("install_date", rs.getString("install_date"));
                trap.put("is_key_patrol", rs.getInt("is_key_patrol"));
                trap.put("key_patrol_reason", rs.getString("key_patrol_reason"));
                trap.put("pk_ranger", rs.getString("pk_ranger"));
                trap.put("ranger_name", rs.getString("ranger_name"));
                list.add(trap);
            }
            rs.close();
            ps.close();
        }
        sendSuccess(exchange, list);
    }

    private void queryTrap(HttpExchange exchange) throws Exception {
        Map<String, String> params = getQueryParams(exchange);
        String pk = params.get("pk");
        if (pk == null || pk.isEmpty()) {
            sendError(exchange, "缺少参数pk");
            return;
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT t.*, u.user_name as ranger_name FROM forest_trap t " +
                            "LEFT JOIN forest_user u ON t.pk_ranger = u.pk_forest_user " +
                            "WHERE t.pk_forest_trap = ? AND t.dr = 0");
            ps.setString(1, pk);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Map<String, Object> trap = new HashMap<>();
                trap.put("pk_forest_trap", rs.getString("pk_forest_trap"));
                trap.put("trap_code", rs.getString("trap_code"));
                trap.put("trap_name", rs.getString("trap_name"));
                trap.put("longitude", rs.getDouble("longitude"));
                trap.put("latitude", rs.getDouble("latitude"));
                trap.put("location_desc", rs.getString("location_desc"));
                trap.put("forest_type", rs.getString("forest_type"));
                trap.put("trap_type", rs.getString("trap_type"));
                trap.put("install_date", rs.getString("install_date"));
                trap.put("is_key_patrol", rs.getInt("is_key_patrol"));
                trap.put("key_patrol_reason", rs.getString("key_patrol_reason"));
                trap.put("pk_ranger", rs.getString("pk_ranger"));
                trap.put("ranger_name", rs.getString("ranger_name"));
                sendSuccess(exchange, trap);
            } else {
                sendError(exchange, "诱捕器点位不存在");
            }
            rs.close();
            ps.close();
        }
    }

    private void saveTrap(HttpExchange exchange) throws Exception {
        Map<String, Object> params = parseJsonBody(exchange);
        String pk = generatePk();
        String now = now();
        String userId = getHeader(exchange, "userId");
        String pkOrg = getHeader(exchange, "pkOrg");
        String pkGroup = getHeader(exchange, "pkGroup");

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO forest_trap (pk_forest_trap,trap_code,trap_name,longitude,latitude,location_desc,forest_type,trap_type,install_date,is_key_patrol,key_patrol_reason,pk_ranger,pk_org,pk_group,creator,creationtime,modifier,modifiedtime,dr,ts) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0,?)");
            ps.setString(1, pk);
            ps.setString(2, (String) params.get("trap_code"));
            ps.setString(3, (String) params.get("trap_name"));
            ps.setDouble(4, ((Number) params.get("longitude")).doubleValue());
            ps.setDouble(5, ((Number) params.get("latitude")).doubleValue());
            ps.setString(6, (String) params.get("location_desc"));
            ps.setString(7, (String) params.get("forest_type"));
            ps.setString(8, (String) params.get("trap_type"));
            ps.setString(9, (String) params.getOrDefault("install_date", today()));
            ps.setInt(10, 0);
            ps.setString(11, null);
            ps.setString(12, (String) params.getOrDefault("pk_ranger", userId));
            ps.setString(13, pkOrg != null ? pkOrg : "ORG001");
            ps.setString(14, pkGroup != null ? pkGroup : "GRP001");
            ps.setString(15, userId);
            ps.setString(16, now);
            ps.setString(17, userId);
            ps.setString(18, now);
            ps.setString(19, now);
            ps.executeUpdate();
            ps.close();

            Map<String, Object> result = new HashMap<>();
            result.put("pk_forest_trap", pk);
            result.put("trap_code", params.get("trap_code"));
            sendSuccess(exchange, result);
        }
    }

    private void updateTrap(HttpExchange exchange) throws Exception {
        Map<String, Object> params = parseJsonBody(exchange);
        String pk = (String) params.get("pk_forest_trap");
        if (pk == null) {
            sendError(exchange, "缺少参数pk_forest_trap");
            return;
        }
        String now = now();
        String userId = getHeader(exchange, "userId");

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            StringBuilder sql = new StringBuilder("UPDATE forest_trap SET modifier=?, modifiedtime=?");
            List<Object> args = new ArrayList<>();
            args.add(userId);
            args.add(now);

            if (params.containsKey("trap_name")) {
                sql.append(", trap_name=?");
                args.add(params.get("trap_name"));
            }
            if (params.containsKey("longitude")) {
                sql.append(", longitude=?");
                args.add(((Number) params.get("longitude")).doubleValue());
            }
            if (params.containsKey("latitude")) {
                sql.append(", latitude=?");
                args.add(((Number) params.get("latitude")).doubleValue());
            }
            if (params.containsKey("location_desc")) {
                sql.append(", location_desc=?");
                args.add(params.get("location_desc"));
            }
            if (params.containsKey("is_key_patrol")) {
                sql.append(", is_key_patrol=?");
                args.add(params.get("is_key_patrol"));
            }
            if (params.containsKey("key_patrol_reason")) {
                sql.append(", key_patrol_reason=?");
                args.add(params.get("key_patrol_reason"));
            }
            if (params.containsKey("pk_ranger")) {
                sql.append(", pk_ranger=?");
                args.add(params.get("pk_ranger"));
            }

            sql.append(" WHERE pk_forest_trap=?");
            args.add(pk);

            PreparedStatement ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            ps.executeUpdate();
            ps.close();
            sendSuccess(exchange, "更新成功");
        }
    }

    private void deleteTrap(HttpExchange exchange) throws Exception {
        Map<String, Object> params = parseJsonBody(exchange);
        String pk = (String) params.get("pk");
        if (pk == null) {
            Map<String, String> qp = getQueryParams(exchange);
            pk = qp.get("pk");
        }
        if (pk == null) {
            sendError(exchange, "缺少参数pk");
            return;
        }
        String now = now();
        String userId = getHeader(exchange, "userId");

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE forest_trap SET dr=1, modifier=?, modifiedtime=? WHERE pk_forest_trap=?");
            ps.setString(1, userId);
            ps.setString(2, now);
            ps.setString(3, pk);
            ps.executeUpdate();
            ps.close();
            sendSuccess(exchange, "删除成功");
        }
    }
}
