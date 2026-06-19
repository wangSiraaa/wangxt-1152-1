package nc.forest.handler;

import com.sun.net.httpserver.HttpExchange;
import nc.forest.db.DatabaseManager;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class ForestDisposalHandler extends BaseHandler {

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
                case "create":
                    saveDisposal(exchange);
                    break;
                case "update":
                    updateDisposal(exchange);
                    break;
                case "close":
                    closeDisposal(exchange);
                    break;
                case "start":
                    startDisposal(exchange);
                    break;
                case "uploadPhoto":
                case "savePhoto":
                    uploadPhoto(exchange);
                    break;
                case "query":
                    queryDisposal(exchange);
                    break;
                case "list":
                default:
                    listDisposals(exchange);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, "服务器错误: " + e.getMessage());
        }
    }

    private void listDisposals(HttpExchange exchange) throws Exception {
        Map<String, String> params = getQueryParams(exchange);
        String status = params.get("status");
        String pkRecord = params.get("pk_trap_record");
        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            StringBuilder sql = new StringBuilder(
                    "SELECT d.*, r.record_date, r.insect_type, r.insect_count, " +
                            "r.is_suspect_quarantine, r.pk_forest_trap, " +
                            "t.trap_code, t.trap_name, t.longitude, t.latitude, t.location_desc, " +
                            "u.user_name as disposal_team_name " +
                            "FROM forest_disposal d " +
                            "LEFT JOIN forest_trap_record r ON d.pk_trap_record = r.pk_trap_record " +
                            "LEFT JOIN forest_trap t ON r.pk_forest_trap = t.pk_forest_trap " +
                            "LEFT JOIN forest_user u ON d.pk_disposal_team = u.pk_forest_user " +
                            "WHERE d.dr = 0");
            List<Object> args = new ArrayList<>();

            if (status != null && !status.isEmpty()) {
                sql.append(" AND d.disposal_status = ?");
                args.add(Integer.parseInt(status));
            }
            if (pkRecord != null && !pkRecord.isEmpty()) {
                sql.append(" AND d.pk_trap_record = ?");
                args.add(pkRecord);
            }
            sql.append(" ORDER BY d.disposal_date DESC, d.creationtime DESC");

            PreparedStatement ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> disposal = buildDisposalMap(rs);
                list.add(disposal);
            }
            rs.close();
            ps.close();
        }
        sendSuccess(exchange, list);
    }

    private Map<String, Object> buildDisposalMap(ResultSet rs) throws SQLException {
        Map<String, Object> disposal = new HashMap<>();
        disposal.put("pk_forest_disposal", rs.getString("pk_forest_disposal"));
        disposal.put("pk_trap_record", rs.getString("pk_trap_record"));
        disposal.put("disposal_date", rs.getString("disposal_date"));
        disposal.put("disposal_type", rs.getString("disposal_type"));
        disposal.put("disposal_method", rs.getString("disposal_method"));
        disposal.put("disposal_area", rs.getObject("disposal_area") != null ? rs.getDouble("disposal_area") : null);
        disposal.put("tree_count", rs.getObject("tree_count") != null ? rs.getInt("tree_count") : null);
        disposal.put("disposal_remark", rs.getString("disposal_remark"));
        disposal.put("has_photo", rs.getInt("has_photo"));
        disposal.put("disposal_status", rs.getInt("disposal_status"));
        disposal.put("disposal_status_name", getDisposalStatusName(rs.getInt("disposal_status")));
        disposal.put("pk_disposal_team", rs.getString("pk_disposal_team"));
        disposal.put("disposal_team_name", rs.getString("disposal_team_name"));
        disposal.put("pk_forest_trap", rs.getString("pk_forest_trap"));
        disposal.put("trap_code", rs.getString("trap_code"));
        disposal.put("trap_name", rs.getString("trap_name"));
        disposal.put("longitude", rs.getObject("longitude") != null ? rs.getDouble("longitude") : null);
        disposal.put("latitude", rs.getObject("latitude") != null ? rs.getDouble("latitude") : null);
        disposal.put("location_desc", rs.getString("location_desc"));
        disposal.put("record_date", rs.getString("record_date"));
        disposal.put("insect_type", rs.getString("insect_type"));
        disposal.put("insect_count", rs.getObject("insect_count") != null ? rs.getInt("insect_count") : null);
        disposal.put("is_suspect_quarantine", rs.getInt("is_suspect_quarantine"));
        return disposal;
    }

    private String getDisposalStatusName(int status) {
        switch (status) {
            case 0: return "待处置";
            case 1: return "处置中";
            case 2: return "已完成";
            default: return "未知";
        }
    }

    private void queryDisposal(HttpExchange exchange) throws Exception {
        Map<String, String> params = getQueryParams(exchange);
        String pk = params.get("pk");
        if (pk == null || pk.isEmpty()) {
            sendError(exchange, "缺少参数pk");
            return;
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT d.*, r.record_date, r.insect_type, r.insect_count, " +
                            "r.is_suspect_quarantine, r.pk_forest_trap, " +
                            "t.trap_code, t.trap_name, t.longitude, t.latitude, t.location_desc, " +
                            "u.user_name as disposal_team_name " +
                            "FROM forest_disposal d " +
                            "LEFT JOIN forest_trap_record r ON d.pk_trap_record = r.pk_trap_record " +
                            "LEFT JOIN forest_trap t ON r.pk_forest_trap = t.pk_forest_trap " +
                            "LEFT JOIN forest_user u ON d.pk_disposal_team = u.pk_forest_user " +
                            "WHERE d.pk_forest_disposal = ? AND d.dr = 0");
            ps.setString(1, pk);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Map<String, Object> disposal = buildDisposalMap(rs);

                PreparedStatement ps2 = conn.prepareStatement(
                        "SELECT * FROM forest_disposal_photo WHERE pk_forest_disposal = ? AND dr = 0");
                ps2.setString(1, pk);
                ResultSet rs2 = ps2.executeQuery();
                List<Map<String, Object>> photos = new ArrayList<>();
                while (rs2.next()) {
                    Map<String, Object> photo = new HashMap<>();
                    photo.put("pk_disposal_photo", rs2.getString("pk_disposal_photo"));
                    photo.put("photo_url", rs2.getString("photo_url"));
                    photo.put("photo_type", rs2.getString("photo_type"));
                    photo.put("photo_remark", rs2.getString("photo_remark"));
                    photo.put("upload_date", rs2.getString("upload_date"));
                    photos.add(photo);
                }
                rs2.close();
                ps2.close();
                disposal.put("photos", photos);

                sendSuccess(exchange, disposal);
            } else {
                sendError(exchange, "处置记录不存在");
            }
            rs.close();
            ps.close();
        }
    }

    private void saveDisposal(HttpExchange exchange) throws Exception {
        Map<String, Object> params = parseJsonBody(exchange);
        String pkTrapRecord = (String) params.get("pk_trap_record");
        if (pkTrapRecord == null || pkTrapRecord.isEmpty()) {
            sendError(exchange, "缺少参数pk_trap_record");
            return;
        }

        String userId = getHeader(exchange, "userId");
        String pkOrg = getHeader(exchange, "pkOrg");
        String pkGroup = getHeader(exchange, "pkGroup");
        String now = now();

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            validateAllowDisposal(conn, pkTrapRecord);

            String pk = generatePk();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO forest_disposal (pk_forest_disposal,pk_trap_record,disposal_date,disposal_type,disposal_method,disposal_area,tree_count,disposal_remark,has_photo,disposal_status,pk_disposal_team,pk_org,pk_group,creator,creationtime,modifier,modifiedtime,ts) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            ps.setString(1, pk);
            ps.setString(2, pkTrapRecord);
            ps.setString(3, (String) params.getOrDefault("disposal_date", today()));
            ps.setString(4, (String) params.get("disposal_type"));
            ps.setString(5, (String) params.get("disposal_method"));
            ps.setObject(6, params.get("disposal_area") != null ?
                    ((Number) params.get("disposal_area")).doubleValue() : null);
            ps.setObject(7, params.get("tree_count") != null ?
                    ((Number) params.get("tree_count")).intValue() : null);
            ps.setString(8, (String) params.get("disposal_remark"));
            ps.setInt(9, 0);
            ps.setInt(10, 0);
            ps.setString(11, (String) params.getOrDefault("pk_disposal_team", userId));
            ps.setString(12, pkOrg != null ? pkOrg : "ORG001");
            ps.setString(13, pkGroup != null ? pkGroup : "GRP001");
            ps.setString(14, userId);
            ps.setString(15, now);
            ps.setString(16, userId);
            ps.setString(17, now);
            ps.setString(18, now);
            ps.executeUpdate();
            ps.close();

            Map<String, Object> result = new HashMap<>();
            result.put("pk_forest_disposal", pk);
            result.put("message", "处置单创建成功");
            sendSuccess(exchange, result);
        } catch (IllegalArgumentException e) {
            sendError(exchange, e.getMessage());
        }
    }

    private void validateAllowDisposal(Connection conn, String pkTrapRecord) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT r.is_suspect_quarantine, r.record_status, " +
                        "v.is_allow_disposal, v.pk_forest_review " +
                        "FROM forest_trap_record r " +
                        "LEFT JOIN forest_review v ON r.pk_trap_record = v.pk_trap_record AND v.dr = 0 " +
                        "WHERE r.pk_trap_record = ? AND r.dr = 0 " +
                        "ORDER BY v.creationtime DESC LIMIT 1");
        ps.setString(1, pkTrapRecord);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            int isSuspect = rs.getInt("is_suspect_quarantine");
            int recordStatus = rs.getInt("record_status");
            int isAllowDisposal = rs.getObject("is_allow_disposal") != null ?
                    rs.getInt("is_allow_disposal") : -1;

            if (isSuspect == 1) {
                if (recordStatus == 0 || rs.getString("pk_forest_review") == null) {
                    rs.close();
                    ps.close();
                    throw new IllegalArgumentException("疑似检疫对象未经过复核，不能进行清理处置");
                }
                if (isAllowDisposal == 0) {
                    rs.close();
                    ps.close();
                    throw new IllegalArgumentException("复核未允许清理处置，不能创建处置单");
                }
            }
        }
        rs.close();
        ps.close();
    }

    private void startDisposal(HttpExchange exchange) throws Exception {
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
                    "UPDATE forest_disposal SET disposal_status=1, modifier=?, modifiedtime=? " +
                            "WHERE pk_forest_disposal=? AND disposal_status=0");
            ps.setString(1, userId);
            ps.setString(2, now);
            ps.setString(3, pk);
            int count = ps.executeUpdate();
            ps.close();

            if (count == 0) {
                sendError(exchange, "处置单不存在或状态不正确");
                return;
            }
            sendSuccess(exchange, "开始处置成功");
        }
    }

    private void closeDisposal(HttpExchange exchange) throws Exception {
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
                    "SELECT has_photo, disposal_status, pk_trap_record FROM forest_disposal " +
                            "WHERE pk_forest_disposal=? AND dr=0");
            ps.setString(1, pk);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                rs.close();
                ps.close();
                sendError(exchange, "处置单不存在");
                return;
            }

            int hasPhoto = rs.getInt("has_photo");
            int status = rs.getInt("disposal_status");
            String pkTrapRecord = rs.getString("pk_trap_record");
            rs.close();
            ps.close();

            if (hasPhoto == 0) {
                sendError(exchange, "疫木清理未拍照，不能关闭处置单");
                return;
            }
            if (status != 1) {
                sendError(exchange, "处置单状态不正确，当前状态：" + getDisposalStatusName(status));
                return;
            }

            PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE forest_disposal SET disposal_status=2, modifier=?, modifiedtime=? " +
                            "WHERE pk_forest_disposal=?");
            ps2.setString(1, userId);
            ps2.setString(2, now);
            ps2.setString(3, pk);
            ps2.executeUpdate();
            ps2.close();

            PreparedStatement ps3 = conn.prepareStatement(
                    "UPDATE forest_trap_record SET record_status=2, modifier=?, modifiedtime=? " +
                            "WHERE pk_trap_record=?");
            ps3.setString(1, userId);
            ps3.setString(2, now);
            ps3.setString(3, pkTrapRecord);
            ps3.executeUpdate();
            ps3.close();

            sendSuccess(exchange, "处置单关闭成功");
        }
    }

    private void updateDisposal(HttpExchange exchange) throws Exception {
        Map<String, Object> params = parseJsonBody(exchange);
        String pk = (String) params.get("pk_forest_disposal");
        if (pk == null) {
            sendError(exchange, "缺少参数pk_forest_disposal");
            return;
        }
        String now = now();
        String userId = getHeader(exchange, "userId");

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            StringBuilder sql = new StringBuilder("UPDATE forest_disposal SET modifier=?, modifiedtime=?");
            List<Object> args = new ArrayList<>();
            args.add(userId);
            args.add(now);

            if (params.containsKey("disposal_type")) {
                sql.append(", disposal_type=?");
                args.add(params.get("disposal_type"));
            }
            if (params.containsKey("disposal_method")) {
                sql.append(", disposal_method=?");
                args.add(params.get("disposal_method"));
            }
            if (params.containsKey("disposal_area")) {
                sql.append(", disposal_area=?");
                args.add(params.get("disposal_area") != null ?
                        ((Number) params.get("disposal_area")).doubleValue() : null);
            }
            if (params.containsKey("tree_count")) {
                sql.append(", tree_count=?");
                args.add(params.get("tree_count") != null ?
                        ((Number) params.get("tree_count")).intValue() : null);
            }
            if (params.containsKey("disposal_remark")) {
                sql.append(", disposal_remark=?");
                args.add(params.get("disposal_remark"));
            }

            sql.append(" WHERE pk_forest_disposal=?");
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

    private void uploadPhoto(HttpExchange exchange) throws Exception {
        Map<String, Object> params = parseJsonBody(exchange);
        String pkDisposal = (String) params.get("pk_forest_disposal");
        if (pkDisposal == null || pkDisposal.isEmpty()) {
            sendError(exchange, "缺少参数pk_forest_disposal");
            return;
        }

        String userId = getHeader(exchange, "userId");
        String pkOrg = getHeader(exchange, "pkOrg");
        String pkGroup = getHeader(exchange, "pkGroup");
        String now = now();

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String pk = generatePk();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO forest_disposal_photo (pk_disposal_photo,pk_forest_disposal,photo_url,photo_type,photo_remark,upload_date,pk_org,pk_group,creator,creationtime,ts) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
            ps.setString(1, pk);
            ps.setString(2, pkDisposal);
            ps.setString(3, (String) params.get("photo_url"));
            ps.setString(4, (String) params.getOrDefault("photo_type", "清理现场照片"));
            ps.setString(5, (String) params.get("photo_remark"));
            ps.setString(6, now);
            ps.setString(7, pkOrg != null ? pkOrg : "ORG001");
            ps.setString(8, pkGroup != null ? pkGroup : "GRP001");
            ps.setString(9, userId);
            ps.setString(10, now);
            ps.setString(11, now);
            ps.executeUpdate();
            ps.close();

            PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE forest_disposal SET has_photo=1, modifier=?, modifiedtime=? " +
                            "WHERE pk_forest_disposal=?");
            ps2.setString(1, userId);
            ps2.setString(2, now);
            ps2.setString(3, pkDisposal);
            ps2.executeUpdate();
            ps2.close();

            Map<String, Object> result = new HashMap<>();
            result.put("pk_disposal_photo", pk);
            result.put("message", "照片上传成功");
            sendSuccess(exchange, result);
        }
    }
}
