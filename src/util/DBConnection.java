package util;

import java.sql.Connection;
import java.sql.SQLException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBConnection {

    private static HikariDataSource ds;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(
                "jdbc:postgresql://aws-1-eu-central-1.pooler.supabase.com:6543/postgres?sslmode=require&pgbouncer=true&prepareThreshold=0");
        config.setUsername("postgres.knvggfhdcilpqhozvmkp");
        config.setPassword("#federicoII");

        // Pool settings
        config.setMaximumPoolSize(25);       
        config.setIdleTimeout(2 * 60 * 1000);  
        config.setMaxLifetime(30 * 60 * 1000);  
        config.setConnectionTimeout(30 * 1000); 

        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void closeDataSource() {
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
    }
}
