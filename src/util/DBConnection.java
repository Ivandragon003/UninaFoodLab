
package util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;


public class DBConnection {

    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();

            // URL JDBC Supabase PostgreSQL
            config.setJdbcUrl("jdbc:postgresql://aws-1-eu-central-1.pooler.supabase.com:6543/postgres?sslmode=require&pgbouncer=true");
            config.setUsername("postgres.knvggfhdcilpqhozvmkp");
            config.setPassword("#federicoII");

            // Configurazioni HikariCP
            config.setMaximumPoolSize(5);       // max connessioni
            config.setMinimumIdle(2);            // min connessioni sempre pronte
            config.setIdleTimeout(30000);        // 30s per connessioni inattive
            config.setConnectionTimeout(30000);  // timeout per ottenere connessione
            config.setLeakDetectionThreshold(60000); // 60s per individuare connessioni non chiuse

            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante la configurazione di HikariCP", e);
        }
    }

    /**
     * Ottiene una connessione dal pool HikariCP
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Chiude il pool quando l'applicazione termina
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
