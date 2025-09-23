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
				"jdbc:postgresql://aws-1-eu-central-1.pooler.supabase.com:6543/postgres?sslmode=require&pgbouncer=true");
		config.setUsername("postgres.knvggfhdcilpqhozvmkp");
		config.setPassword("#federicoII");
		config.setMaximumPoolSize(25);
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
