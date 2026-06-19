package nc.forest.handler;

import com.sun.net.httpserver.HttpExchange;
import nc.forest.db.DatabaseManager;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class ForestReviewHandler extends BaseHandler {

    private static final int CONTINUOUS_HIGH_RISK_COUNT = 3;

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
                    saveReview(exchange);
                    break;
                case "query":
                    queryReview(exchange);
                    break;
                case "list":
                default:
                    listReviews(exchange);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, "服务器错误: " + e.getMessage());
        }
    }

    private void listReviews(HttpExchange exchange) throws Exception {
        Map<String, String> params = getQueryParams(exchange);
        String pkRecord = params.get("pk_trap_record");
        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            StringBuilder sql = new StringBuilder(
                    "SELECT v.*, r.record_date, r.insect_type, r.insect_count, " +
                            "r.is_suspect_quarantine, r.suspect_remark, r.pk_forest_trap, " +
                            "t.trap_code, t.trap_name, u.user_name as quarantine_name " +
                            "FROM forest_review v " +
                            "LEFT JOIN forest_trap_record r ON v.pk_trap_record = r.pk_trap_record " +
                            "LEFT JOIN forest_trap t ON r.pk_forest_trap = t.pk_forest_trap " +
                            "LEFT JOIN forest_user u ON v.pk_quarantine = u.pk_forest_user " +
                            "WHERE v.dr = 0");
            List<Object> args = new ArrayList<>();

            if (pkRecord != null && !pkRecord.isEmpty()) {
                sql.append(" AND v.pk_trap_record = ?");
                args.add(pkRecord);
            }
            sql.append(" ORDER BY v.review_date DESC, v.creationtime DESC");

            PreparedStatement ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> review = new HashMap<>();
                review.put("pk_forest_review", rs.getString("pk_forest_review"));
                review.put("pk_trap_record", rs.getString("pk_trap_record"));
                review.put("review_date", rs.getString("review_date"));
                review.put("risk_level", rs.getInt("risk_level"));
                review.put("risk_level_name", getRiskName(rs.getInt("risk_level")));
                review.put("is_quarantine", rs.getInt("is_quarantine"));
                review.put("review_remark", rs.getString("review_remark"));
                review.put("review_result", rs.getString("review_result"));
                review.put("is_allow_disposal", rs.getInt("is_allow_disposal"));
                review.put("pk_quarantine", rs.getString("pk_quarantine"));
                review.put("quarantine_name", rs.getString("quarantine_name"));
                review.put("pk_forest_trap", rs.getString("pk_forest_trap"));
                review.put("trap_code", rs.getString("trap_code"));
                review.put("trap_name", rs.getString("trap_name"));
                review.put("record_date", rs.getString("record_date"));
                review.put("insect_type", rs.getString("insect_type"));
                review.put("insect_count", rs.getInt("insect_count"));
                review.put("is_suspect_quarantine", rs.getInt("is_suspect_quarantine"));
                list.add(review);
            }
            rs.close();
            ps.close();
        }
        sendSuccess(exchange, list);
    }

    private String getRiskName(int risk) {
        switch (risk) {
            case 1: return "低风险";
            case 2: return "中风险";
            case 3: return "高风险";
            default: return "未评估";
        }
    }

    private void queryReview(HttpExchange exchange) throws Exception {
        Map<String, String> params = getQueryParams(exchange);
        String pk = params.get("pk");
        if (pk == null || pk.isEmpty()) {
            sendError(exchange, "缺少参数pk");
            return;
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT v.*, r.record_date, r.insect_type, r.insect_count, " +
                            "r.is_suspect_quarantine, r.suspect_remark, r.pk_forest_trap, " +
                            "t.trap_code, t.trap_name, u.user_name as quarantine_name " +
                            "FROM forest_review v " +
                            "LEFT JOIN forest_trap_record r ON v.pk_trap_record = r.pk_trap_record " +
                            "LEFT JOIN forest_trap t ON r.pk_forest_trap = t.pk_forest_trap " +
                            "LEFT JOIN forest_user u ON v.pk_quarantine = u.pk_forest_user " +
                            "WHERE v.pk_forest_review = ? AND v.dr = 0");
            ps.setString(1, pk);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Map<String, Object> review = new HashMap<>();
                review.put("pk_forest_review", rs.getString("pk_forest_review"));
                review.put("pk_trap_record", rs.getString("pk_trap_record"));
                review.put("review_date", rs.getString("review_date"));
                review.put("risk_level", rs.getInt("risk_level"));
                review.put("risk_level_name", getRiskName(rs.getInt("risk_level")));
                review.put("is_quarantine", rs.getInt("is_quarantine"));
                review.put("review_remark", rs.getString("review_remark"));
                review.put("review_result", rs.getString("review_result"));
                review.put("is_allow_disposal", rs.getInt("is_allow_disposal"));
                review.put("quarantine_name", rs.getString("quarantine_name"));
                sendSuccess(exchange, review);
            } else {
                sendError(exchange, "复核记录不存在");
            }
            rs.close();
            ps.close();
        }
    }

    private void saveReview(HttpExchange exchange) throws Exception {
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
            int riskLevel = ((Number) params.get("risk_level")).intValue();
            int isQuarantine = params.get("is_quarantine") != null ?
                    ((Number) params.get("is_quarantine")).intValue() : 0;
            int isAllowDisposal = params.get("is_allow_disposal") != null ?
                    ((Number) params.get("is_allow_disposal")).intValue() : 0;

            String pk = generatePk();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO forest_review (pk_forest_review,pk_trap_record,review_date,risk_level,is_quarantine,review_remark,review_result,is_allow_disposal,pk_quarantine,pk_org,pk_group,creator,creationtime,modifier,modifiedtime,ts) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            ps.setString(1, pk);
            ps.setString(2, pkTrapRecord);
            ps.setString(3, (String) params.getOrDefault("review_date", today()));
            ps.setInt(4, riskLevel);
            ps.setInt(5, isQuarantine);
            ps.setString(6, (String) params.get("review_remark"));
            ps.setString(7, (String) params.get("review_result"));
            ps.setInt(8, isAllowDisposal);
            ps.setString(9, (String) params.getOrDefault("pk_quarantine", userId));
            ps.setString(10, pkOrg != null ? pkOrg : "ORG001");
            ps.setString(11, pkGroup != null ? pkGroup : "GRP001");
            ps.setString(12, userId);
            ps.setString(13, now);
            ps.setString(14, userId);
            ps.setString(15, now);
            ps.setString(16, now);
            ps.executeUpdate();
            ps.close();

            PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE forest_trap_record SET record_status=1, risk_level=?, modifier=?, modifiedtime=? " +
                            "WHERE pk_trap_record=?");
            ps2.setInt(1, riskLevel);
            ps2.setString(2, userId);
            ps2.setString(3, now);
            ps2.setString(4, pkTrapRecord);
            ps2.executeUpdate();
            ps2.close();

            autoSetKeyPatrol(conn, pkTrapRecord, userId, now);

            Map<String, Object> result = new HashMap<>();
            result.put("pk_forest_review", pk);
            result.put("message", "复核完成");
            sendSuccess(exchange, result);
        }
    }

    private void autoSetKeyPatrol(Connection conn, String pkTrapRecord, String userId, String now) throws SQLException {
        PreparedStatement ps1 = conn.prepareStatement(
                "SELECT pk_forest_trap FROM forest_trap_record WHERE pk_trap_record = ?");
        ps1.setString(1, pkTrapRecord);
        ResultSet rs1 = ps1.executeQuery();
        if (!rs1.next()) {
            rs1.close();
            ps1.close();
            return;
        }
        String pkForestTrap = rs1.getString(1);
        rs1.close();
        ps1.close();

        PreparedStatement ps2 = conn.prepareStatement(
                "SELECT risk_level FROM forest_trap_record " +
                        "WHERE pk_forest_trap = ? AND dr = 0 AND risk_level IS NOT NULL " +
                        "ORDER BY record_date DESC, creationtime DESC LIMIT ?");
        ps2.setString(1, pkForestTrap);
        ps2.setInt(2, CONTINUOUS_HIGH_RISK_COUNT);
        ResultSet rs2 = ps2.executeQuery();

        int highRiskCount = 0;
        int totalCount = 0;
        while (rs2.next()) {
            totalCount++;
            if (rs2.getInt("risk_level") == 3) {
                highRiskCount++;
            }
        }
        rs2.close();
        ps2.close();

        if (totalCount >= CONTINUOUS_HIGH_RISK_COUNT && highRiskCount >= CONTINUOUS_HIGH_RISK_COUNT) {
            PreparedStatement ps3 = conn.prepareStatement(
                    "UPDATE forest_trap SET is_key_patrol=1, key_patrol_reason=?, modifier=?, modifiedtime=? " +
                            "WHERE pk_forest_trap=? AND is_key_patrol=0");
            ps3.setString(1, "连续" + CONTINUOUS_HIGH_RISK_COUNT + "次高风险，自动进入重点巡查");
            ps3.setString(2, userId);
            ps3.setString(3, now);
            ps3.setString(4, pkForestTrap);
            ps3.executeUpdate();
            ps3.close();
            System.out.println("[自动重点巡查] 诱捕器点位 " + pkForestTrap + " 已连续" + CONTINUOUS_HIGH_RISK_COUNT + "次高风险，自动设为重点巡查");
        }
    }
}
