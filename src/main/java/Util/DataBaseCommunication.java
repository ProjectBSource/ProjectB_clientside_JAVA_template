package Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataBaseCommunication {
	
	static Connection con;
    static DataBaseCommunication dbcommunication;
	static int renewConnectiontimeCount = 0;
	
	public DataBaseCommunication() throws SQLException {
		createConnection();
	}
	
	public void createConnection() throws SQLException {
        String unicode="useSSL=false&autoReconnect=true&useUnicode=yes&characterEncoding=UTF-8";
		con=DriverManager.getConnection("jdbc:mysql://151.106.124.51/u628315660_projectB?"+unicode,"u628315660_projectB","wtfWTF0506536");
	}
	
	public int renewConnection() throws SQLException {
		createConnection();
		renewConnectiontimeCount++;
		return renewConnectiontimeCount;
	}

    public static String getMessage(){
        String message = null;
        try(PreparedStatement pstmt = con.prepareStatement("SELECT * FROM ProjectB_WebJobHistory WHERE RunJobID=? AND ClientID=? ");) {
            pstmt.setString(1, WebTaskConstants.runJobID);
            pstmt.setString(2, WebTaskConstants.clientID);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                message = rs.getString("Message");
            }
        }
        // Handle any errors that may have occurred.
        catch (SQLException e) {
            e.printStackTrace();
        }
        return message;
    }
	
    public static void updateTestResult(boolean testPass, StringBuilder testPassDetail){
        try(PreparedStatement pstmt = con.prepareStatement("UPDATE ProjectB_WebJobHistory SET TestPass=?, TestResultDetail=? WHERE RunJobID=? AND ClientID=? ");) {
            pstmt.setBoolean(1, testPass);
            pstmt.setString(2, testPassDetail.toString());
            pstmt.setString(3, WebTaskConstants.runJobID);
            pstmt.setString(4, WebTaskConstants.clientID);
            pstmt.executeQuery();
        }
        // Handle any errors that may have occurred.
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
	
}
