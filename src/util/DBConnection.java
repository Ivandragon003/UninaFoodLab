package util;

import java.sql.Connection;
import java.sql.SQLException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBConnection {
    private static HikariDataSource ds;
    
    static {
        try {
            HikariConfig config = new HikariConfig();
            
            config.setJdbcUrl(
                "jdbc:postgresql://aws-1-eu-central-1.pooler.supabase.com:6543/postgres?sslmode=require&pgbouncer=true&prepareThreshold=0");
            config.setUsername("postgres.knvggfhdcilpqhozvmkp");
            config.setPassword("#federicoII");
            
            config.setMaximumPoolSize(3);
            config.setMinimumIdle(1);
            config.setIdleTimeout(5 * 60_000);     // 5 minuti
            config.setMaxLifetime(10 * 60_000);    // 10 minuti
            config.setConnectionTimeout(10_000);   // 10 secondi
            config.setLeakDetectionThreshold(30_000);
            config.setConnectionTestQuery("SELECT 1");
            config.setValidationTimeout(3_000);
            config.setPoolName("MainHikariPool");
            config.setAutoCommit(true);
            
            ds = new HikariDataSource(config);
            
        } catch (Exception e) {
            System.err.println("❌ ERRORE CRITICO: Impossibile inizializzare HikariCP");
            e.printStackTrace();
            throw new RuntimeException("Errore inizializzazione HikariCP", e);
        }
    }
    
    public static Connection getConnection() throws SQLException {
        if (ds == null) {
            throw new SQLException("DataSource non inizializzato");
        }
        return ds.getConnection();
    }
    
    public static void closeDataSource() {
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
    }
}
