package nc.forest.handler;

import com.sun.net.httpserver.HttpExchange;
import nc.forest.db.DatabaseManager;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class TrapRecordHandler extends BaseHandler {

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
                    saveRecord(exchange);
                    break;
                case "update":
                    updateRecord(exchange);
                    break;
                case "delete":
                    deleteRecord(exchange);
                    break;
                case "query":
                    queryRecord(exchange);
                    break;
                case "list":
                default:
                    listRecords(exchange);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, "服务器错误: " + e.getMessage());
        }
    }

    private void listRecords(HttpExchange exchange) throws Exception {
        Map<String, String> params = getQueryParams(exchange);
        String status = params.get("status");
        String pkTrap = params.get("pk_forest_trap");
        String isSuspect = params.get("is_suspect");
        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            StringBuilder sql = new StringBuilder(
                    "SELECT r.*, t.trap_code, t.trap_name, t.longitude, t.latitude, " +
                            "t.location_desc, u.user_name as ranger_name " +
                            "FROM forest_trap_record r " +
                            "LEFT JOIN forest_trap t ON r.pk_forest_trap = t.pk_forest_trap " +
                            "LEFT JOIN forest_user u ON r.pk_ranger = u.pk_forest_user " +
                            "WHERE r.dr = 0");
            List<Object> args = new ArrayList<>();

            if (status != null && !status.isEmpty()) {
                sql.append(" AND r.record_status = ?");
                args.add(Integer.parseInt(status));
            }
            if (pkTrap != null && !pkTrap.isEmpty()) {
                sql.append(" AND r.pk_forest_trap = ?");
                args.add(pkTrap);
            }
            if (isSuspect != null && !isSuspect.isEmpty()) {
                sql.append(" AND r.is_suspect_quarantine = ?");
                args.add(Integer.parseInt(isSuspect));
            }
            sql.append(" ORDER BY r.record_date DESC, r.creationtime DESC");

            PreparedStatement ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> record = buildRecordMap(rs);
                list.add(record);
            }
            rs.close();
            ps.close();
        }
        sendSuccess(exchange, list);
    }

    private Map<String, Object> buildRecordMap(ResultSet rs) throws SQLException {
        Map<String, Object> record = new HashMap<>();
        record.put("pk_trap_record", rs.getString("pk_trap_record"));
        record.put("pk_forest_trap", rs.getString("pk_forest_trap"));
        record.put("trap_code", rs.getString("trap_code"));
        record.put("trap_name", rs.getString("trap_name"));
        record.put("longitude", rs.getObject("longitude") != null ? rs.getDouble("longitude") : null);
        record.put("latitude", rs.getObject("latitude") != null ? rs.getDouble("latitude") : null);
        record.put("location_desc", rs.getString("location_desc"));
        record.put("record_date", rs.getString("record_date"));
        record.put("insect_type", rs.getString("insect_type"));
        record.put("insect_count", rs.getInt("insect_count"));
        record.put("is_suspect_quarantine", rs.getInt("is_suspect_quarantine"));
        record.put("suspect_remark", rs.getString("suspect_remark"));
        record.put("record_status", rs.getInt("record_status"));
        record.put("record_status_name", getStatusName(rs.getInt("record_status")));
        record.put("risk_level", rs.getObject("risk_level") != null ? rs.getInt("risk_level") : null);
        record.put("risk_level_name", getRiskName(rs.getObject("risk_level") != null ? rs.getInt("risk_level") : 0));
        record.put("pk_ranger", rs.getString("pk_ranger"));
        record.put("ranger_name", rs.getString("ranger_name"));
        return record;
    }

    private String getStatusName(int status) {
        switch (status) {
            case 0: return "待复核";
            case 1: return "已复核";
            case 2: return "已处置";
            default: return "未知";
        }
    }

    private String getRiskName(int risk) {
        switch (risk) {
            case 1: return "低风险";
            case 2: return "中风险";
            case 3: return "高风险";
            default: return "未评估";
        }
    }

    private void queryRecord(HttpExchange exchange) throws Exception {
        Map<String, String> params = getQueryParams(exchange);
        String pk = params.get("pk");
        if (pk == null || pk.isEmpty()) {
            sendError(exchange, "缺少参数pk");
            return;
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT r.*, t.trap_code, t.trap_name, t.longitude, t.latitude, " +
                            "t.location_desc, u.user_name as ranger_name " +
                            "FROM forest_trap_record r " +
                            "LEFT JOIN forest_trap t ON r.pk_forest_trap = t.pk_forest_trap " +
                            "LEFT JOIN forest_user u ON r.pk_ranger = u.pk_forest_user " +
                            "WHERE r.pk_trap_record = ? AND r.dr = 0");
            ps.setString(1, pk);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Map<String, Object> record = buildRecordMap(rs);

                PreparedStatement ps2 = conn.prepareStatement(
                        "SELECT v.*, u.user_name as quarantine_name FROM forest_review v " +
                                "LEFT JOIN forest_user u ON v.pk_quarantine = u.pk_forest_user " +
                                "WHERE v.pk_trap_record = ? AND v.dr = 0 ORDER BY v.creationtime DESC");
                ps2.setString(1, pk);
                ResultSet rs2 = ps2.executeQuery();
                List<Map<String, Object>> reviews = new ArrayList<>();
                while (rs2.next()) {
                    Map<String, Object> review = new HashMap<>();
                    review.put("pk_forest_review", rs2.getString("pk_forest_review"));
                    review.put("review_date", rs2.getString("review_date"));
                    review.put("risk_level", rs2.getInt("risk_level"));
                    review.put("risk_level_name", getRiskName(rs2.getInt("risk_level")));
                    review.put("is_quarantine", rs2.getInt("is_quarantine"));
                    review.put("review_remark", rs2.getString("review_remark"));
                    review.put("is_allow_disposal", rs2.getInt("is_allow_disposal"));
                    review.put("quarantine_name", rs2.getString("quarantine_name"));
                    reviews.add(review);
                }
                rs2.close();
                ps2.close();
                record.put("reviews", reviews);

                PreparedStatement ps3 = conn.prepareStatement(
                        "SELECT d.*, u.user_name as disposal_team_name FROM forest_disposal d " +
                                "LEFT JOIN forest_user u ON d.pk_disposal_team = u.pk_forest_user " +
                                "WHERE d.pk_trap_record = ? AND d.dr = 0 ORDER BY d.creationtime DESC");
                ps3.setString(1, pk);
                ResultSet rs3 = ps3.executeQuery();
                List<Map<String, Object>> disposals = new ArrayList<>();
                while (rs3.next()) {
                    Map<String, Object> disposal = new HashMap<>();
                    disposal.put("pk_forest_disposal", rs3.getString("pk_forest_disposal"));
                    disposal.put("disposal_date", rs3.getString("disposal_date"));
                    disposal.put("disposal_type", rs3.getString("disposal_type"));
                    disposal.put("disposal_method", rs3.getString("disposal_method"));
                    disposal.put("tree_count", rs3.getObject("tree_count") != null ? rs3.getInt("tree_count") : null);
                    disposal.put("disposal_remark", rs3.getString("disposal_remark"));
                    disposal.put("has_photo", rs3.getInt("has_photo"));
                    disposal.put("disposal_status", rs3.getInt("disposal_status"));
                    disposal.put("disposal_status_name", getDisposalStatusName(rs3.getInt("disposal_status")));
                    disposal.put("disposal_team_name", rs3.getString("disposal_team_name"));

                    PreparedStatement ps4 = conn.prepareStatement(
                            "SELECT * FROM forest_disposal_photo WHERE pk_forest_disposal = ? AND dr = 0");
                    ps4.setString(1, rs3.getString("pk_forest_disposal"));
                    ResultSet rs4 = ps4.executeQuery();
                    List<Map<String, Object>> photos = new ArrayList<>();
                    while (rs4.next()) {
                        Map<String, Object> photo = new HashMap<>();
                        photo.put("pk_disposal_photo", rs4.getString("pk_disposal_photo"));
                        photo.put("photo_url", rs4.getString("photo_url"));
                        photo.put("photo_type", rs4.getString("photo_type"));
                        photo.put("photo_remark", rs4.getString("photo_remark"));
                        photo.put("upload_date", rs4.getString("upload_date"));
                        photos.add(photo);
                    }
                    rs4.close();
                    ps4.close();
                    disposal.put("photos", photos);

                    disposals.add(disposal);
                }
                rs3.close();
                ps3.close();
                record.put("disposals", disposals);

                sendSuccess(exchange, record);
            } else {
                sendError(exchange, "诱捕记录不存在");
            }
            rs.close();
            ps.close();
        }
    }

    private String getDisposalStatusName(int status) {
        switch (status) {
            case 0: return "待处置";
            case 1: return "处置中";
            case 2: return "已完成";
            default: return "未知";
        }
    }

    private void saveRecord(HttpExchange exchange) throws Exception {
        Map<String, Object> params = parseJsonBody(exchange);
        String pk = generatePk();
        String now = now();
        String userId = getHeader(exchange, "userId");
        String pkOrg = getHeader(exchange, "pkOrg");
        String pkGroup = getHeader(exchange, "pkGroup");

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO forest_trap_record (pk_trap_record,pk_forest_trap,record_date,insect_type,insect_count,is_suspect_quarantine,suspect_remark,record_status,risk_level,pk_ranger,pk_org,pk_group,creator,creationtime,modifier,modifiedtime,dr,ts) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0,?)");
            ps.setString(1, pk);
            ps.setString(2, (String) params.get("pk_forest_trap"));
            ps.setString(3, (String) params.getOrDefault("record_date", today()));
            ps.setString(4, (String) params.get("insect_type"));
            ps.setInt(5, ((Number) params.get("insect_count")).intValue());
            ps.setInt(6, params.get("is_suspect_quarantine") != null ?
                    ((Number) params.get("is_suspect_quarantine")).intValue() : 0);
            ps.setString(7, (String) params.get("suspect_remark"));
            ps.setInt(8, 0);
            ps.setObject(9, null);
            ps.setString(10, (String) params.getOrDefault("pk_ranger", userId));
            ps.setString(11, pkOrg != null ? pkOrg : "ORG001");
            ps.setString(12, pkGroup != null ? pkGroup : "GRP001");
            ps.setString(13, userId);
            ps.setString(14, now);
            ps.setString(15, userId);
            ps.setString(16, now);
            ps.setString(17, now);
            ps.executeUpdate();
            ps.close();

            Map<String, Object> result = new HashMap<>();
            result.put("pk_trap_record", pk);
            sendSuccess(exchange, result);
        }
    }

    private void updateRecord(HttpExchange exchange) throws Exception {
        Map<String, Object> params = parseJsonBody(exchange);
        String pk = (String) params.get("pk_trap_record");
        if (pk == null) {
            sendError(exchange, "缺少参数pk_trap_record");
            return;
        }
        String now = now();
        String userId = getHeader(exchange, "userId");

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            StringBuilder sql = new StringBuilder("UPDATE forest_trap_record SET modifier=?, modifiedtime=?");
            List<Object> args = new ArrayList<>();
            args.add(userId);
            args.add(now);

            if (params.containsKey("insect_type")) {
                sql.append(", insect_type=?");
                args.add(params.get("insect_type"));
            }
            if (params.containsKey("insect_count")) {
                sql.append(", insect_count=?");
                args.add(((Number) params.get("insect_count")).intValue());
            }
            if (params.containsKey("is_suspect_quarantine")) {
                sql.append(", is_suspect_quarantine=?");
                args.add(((Number) params.get("is_suspect_quarantine")).intValue());
            }
            if (params.containsKey("suspect_remark")) {
                sql.append(", suspect_remark=?");
                args.add(params.get("suspect_remark"));
            }
            if (params.containsKey("record_status")) {
                sql.append(", record_status=?");
                args.add(((Number) params.get("record_status")).intValue());
            }
            if (params.containsKey("risk_level")) {
                sql.append(", risk_level=?");
                args.add(params.get("risk_level") != null ?
                        ((Number) params.get("risk_level")).intValue() : null);
            }

            sql.append(" WHERE pk_trap_record=?");
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

    private void deleteRecord(HttpExchange exchange) throws Exception {
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
                    "UPDATE forest_trap_record SET dr=1, modifier=?, modifiedtime=? WHERE pk_trap_record=?");
            ps.setString(1, userId);
            ps.setString(2, now);
            ps.setString(3, pk);
            ps.executeUpdate();
            ps.close();
            sendSuccess(exchange, "删除成功");
        }
    }
}
