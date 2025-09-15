package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

	private static final String URL = "jdbc:postgresql://aws-1-eu-central-1.pooler.supabase.com:6543/postgres?sslmode=require&pgbouncer=true";
	private static final String USER = "postgres.knvggfhdcilpqhozvmkp";
	private static final String PASSWORD = "#federicoII";

	public static Connection getConnection() {
		try {
			return DriverManager.getConnection(URL, USER, PASSWORD);
		} catch (SQLException e) {

			System.err.println("Errore di connessione al database: " + e.getMessage());
			e.printStackTrace();

			throw new RuntimeException("Impossibile connettersi al database", e);
		}
	}
}
