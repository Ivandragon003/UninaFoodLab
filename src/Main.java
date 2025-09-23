import util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Main {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT NOW()");
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                System.out.println("Data/Ora DB: " + rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeDataSource(); // chiude pool a fine programma
        }
    }
}
