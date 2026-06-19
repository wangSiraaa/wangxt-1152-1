package nc.forest.db;

import java.sql.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseManager {

    private static DatabaseManager instance;
    private String dbUrl;
    private static final AtomicInteger pkGenerator = new AtomicInteger(1000000);

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void init() throws Exception {
        Class.forName("org.sqlite.JDBC");
        dbUrl = "jdbc:sqlite:forest_monitor.db";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(true);
        }
    }

    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl);
        conn.setAutoCommit(true);
        return conn;
    }

    public static String generatePk() {
        return "PK" + pkGenerator.incrementAndGet();
    }

    public void initTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();

        stmt.execute("CREATE TABLE IF NOT EXISTS forest_user (" +
                "pk_forest_user TEXT PRIMARY KEY, " +
                "user_code TEXT, user_name TEXT, user_role INTEGER, " +
                "phone TEXT, id_card TEXT, password TEXT, " +
                "pk_org TEXT, pk_group TEXT, " +
                "creator TEXT, creationtime TEXT, modifier TEXT, modifiedtime TEXT, " +
                "dr INTEGER DEFAULT 0, ts TEXT)");

        stmt.execute("CREATE TABLE IF NOT EXISTS forest_trap (" +
                "pk_forest_trap TEXT PRIMARY KEY, " +
                "trap_code TEXT, trap_name TEXT, " +
                "longitude REAL, latitude REAL, " +
                "location_desc TEXT, forest_type TEXT, trap_type TEXT, " +
                "install_date TEXT, " +
                "is_key_patrol INTEGER DEFAULT 0, key_patrol_reason TEXT, " +
                "pk_ranger TEXT, " +
                "pk_org TEXT, pk_group TEXT, " +
                "creator TEXT, creationtime TEXT, modifier TEXT, modifiedtime TEXT, " +
                "dr INTEGER DEFAULT 0, ts TEXT)");

        stmt.execute("CREATE TABLE IF NOT EXISTS forest_trap_record (" +
                "pk_trap_record TEXT PRIMARY KEY, " +
                "pk_forest_trap TEXT, record_date TEXT, " +
                "insect_type TEXT, insect_count INTEGER, " +
                "is_suspect_quarantine INTEGER DEFAULT 0, suspect_remark TEXT, " +
                "record_status INTEGER DEFAULT 0, risk_level INTEGER, " +
                "pk_ranger TEXT, " +
                "pk_org TEXT, pk_group TEXT, " +
                "creator TEXT, creationtime TEXT, modifier TEXT, modifiedtime TEXT, " +
                "dr INTEGER DEFAULT 0, ts TEXT)");

        stmt.execute("CREATE TABLE IF NOT EXISTS forest_review (" +
                "pk_forest_review TEXT PRIMARY KEY, " +
                "pk_trap_record TEXT, review_date TEXT, " +
                "risk_level INTEGER, is_quarantine INTEGER DEFAULT 0, " +
                "review_remark TEXT, review_result TEXT, " +
                "is_allow_disposal INTEGER DEFAULT 0, " +
                "pk_quarantine TEXT, " +
                "pk_org TEXT, pk_group TEXT, " +
                "creator TEXT, creationtime TEXT, modifier TEXT, modifiedtime TEXT, " +
                "dr INTEGER DEFAULT 0, ts TEXT)");

        stmt.execute("CREATE TABLE IF NOT EXISTS forest_disposal (" +
                "pk_forest_disposal TEXT PRIMARY KEY, " +
                "pk_trap_record TEXT, disposal_date TEXT, " +
                "disposal_type TEXT, disposal_method TEXT, " +
                "disposal_area REAL, tree_count INTEGER, " +
                "disposal_remark TEXT, has_photo INTEGER DEFAULT 0, " +
                "disposal_status INTEGER DEFAULT 0, " +
                "pk_disposal_team TEXT, " +
                "pk_org TEXT, pk_group TEXT, " +
                "creator TEXT, creationtime TEXT, modifier TEXT, modifiedtime TEXT, " +
                "dr INTEGER DEFAULT 0, ts TEXT)");

        stmt.execute("CREATE TABLE IF NOT EXISTS forest_disposal_photo (" +
                "pk_disposal_photo TEXT PRIMARY KEY, " +
                "pk_forest_disposal TEXT, photo_url TEXT, " +
                "photo_type TEXT, photo_remark TEXT, upload_date TEXT, " +
                "pk_org TEXT, pk_group TEXT, " +
                "creator TEXT, creationtime TEXT, " +
                "dr INTEGER DEFAULT 0, ts TEXT)");

        stmt.close();
    }

    public void initMockData(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM forest_user WHERE dr = 0");
        if (rs.next() && rs.getInt(1) == 0) {
            String now = new Timestamp(System.currentTimeMillis()).toString().substring(0, 19);

            stmt.execute("INSERT INTO forest_user VALUES (" +
                    "'PKU001', 'ranger001', '张护林', 1, '13800138001', '110101199001010001', '123456', " +
                    "'ORG001', 'GRP001', 'system', '" + now + "', 'system', '" + now + "', 0, '" + now + "')");
            stmt.execute("INSERT INTO forest_user VALUES (" +
                    "'PKU002', 'quarantine001', '李检疫', 2, '13800138002', '110101199002020002', '123456', " +
                    "'ORG001', 'GRP001', 'system', '" + now + "', 'system', '" + now + "', 0, '" + now + "')");
            stmt.execute("INSERT INTO forest_user VALUES (" +
                    "'PKU003', 'disposal001', '王处置', 3, '13800138003', '110101199003030003', '123456', " +
                    "'ORG001', 'GRP001', 'system', '" + now + "', 'system', '" + now + "', 0, '" + now + "')");

            stmt.execute("INSERT INTO forest_trap VALUES (" +
                    "'PKT001', 'TRAP001', '东区1号诱捕器', 116.397, 39.908, " +
                    "'东城区山林A区', '松林', '性诱剂', '2025-01-15', " +
                    "0, NULL, 'PKU001', " +
                    "'ORG001', 'GRP001', 'system', '" + now + "', 'system', '" + now + "', 0, '" + now + "')");
            stmt.execute("INSERT INTO forest_trap VALUES (" +
                    "'PKT002', 'TRAP002', '东区2号诱捕器', 116.407, 39.918, " +
                    "'东城区山林B区', '混交林', '灯诱', '2025-01-20', " +
                    "0, NULL, 'PKU001', " +
                    "'ORG001', 'GRP001', 'system', '" + now + "', 'system', '" + now + "', 0, '" + now + "')");
            stmt.execute("INSERT INTO forest_trap VALUES (" +
                    "'PKT003', 'TRAP003', '西区1号诱捕器', 116.387, 39.898, " +
                    "'西城区山林C区', '杉木林', '性诱剂', '2025-02-01', " +
                    "0, NULL, 'PKU001', " +
                    "'ORG001', 'GRP001', 'system', '" + now + "', 'system', '" + now + "', 0, '" + now + "')");

            System.out.println("  初始化模拟数据：3个用户、3个诱捕器点位");
        }
        rs.close();
        stmt.close();
    }
}
