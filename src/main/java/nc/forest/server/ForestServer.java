package nc.forest.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import nc.forest.db.DatabaseManager;
import nc.forest.handler.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;

public class ForestServer {

    private static final int DEFAULT_PORT = 20452;
    private static ForestServer instance;
    private HttpServer server;
    private int port;

    public static ForestServer getInstance() {
        if (instance == null) {
            instance = new ForestServer();
        }
        return instance;
    }

    public void start() throws Exception {
        start(DEFAULT_PORT);
    }

    public void start(int port) throws Exception {
        this.port = port;

        System.out.println("============================================");
        System.out.println("  林业有害生物诱捕监测系统 启动中...");
        System.out.println("============================================");

        DatabaseManager.getInstance().init();
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            System.out.println("  数据库连接成功");
            DatabaseManager.getInstance().initTables(conn);
            System.out.println("  数据库表结构初始化完成");
            DatabaseManager.getInstance().initMockData(conn);
            System.out.println("  模拟数据初始化完成");
        }

        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new StaticFileHandler());

        server.createContext("/api/forest/user", new ForestUserHandler());
        server.createContext("/api/forest/trap", new ForestTrapHandler());
        server.createContext("/api/forest/record", new TrapRecordHandler());
        server.createContext("/api/forest/review", new ForestReviewHandler());
        server.createContext("/api/forest/disposal", new ForestDisposalHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("============================================");
        System.out.println("  系统启动成功！");
        System.out.println("  服务地址: http://localhost:" + port);
        System.out.println("============================================");
        System.out.println("");
        System.out.println("  三角色使用入口：");
        System.out.println("  护林员: http://localhost:" + port + "/ranger.html");
        System.out.println("  检疫员: http://localhost:" + port + "/quarantine.html");
        System.out.println("  处置队: http://localhost:" + port + "/disposal.html");
        System.out.println("");
        System.out.println("  测试账号：");
        System.out.println("  护林员: ranger001 / 123456");
        System.out.println("  检疫员: quarantine001 / 123456");
        System.out.println("  处置队: disposal001 / 123456");
        System.out.println("============================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("服务已停止");
        }
    }

    public int getPort() {
        return port;
    }

    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("端口参数无效，使用默认端口: " + DEFAULT_PORT);
            }
        }
        ForestServer.getInstance().start(port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ForestServer.getInstance().stop();
        }));
    }
}
