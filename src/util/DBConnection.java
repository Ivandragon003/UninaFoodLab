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

            // Pool settings
            config.setMaximumPoolSize(5);   
            config.setMinimumIdle(1);       
            config.setIdleTimeout(10 * 60_000);  
            config.setMaxLifetime(30 * 60_000);  
            config.setConnectionTimeout(30_000); 

        
            ds = new HikariDataSource(config);

        } catch (Exception e) {
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
