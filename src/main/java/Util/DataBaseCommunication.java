package Util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import javax.management.ReflectionException;

import Indicators.Indicator;

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
            pstmt.setString(1, WebVersionJobConstants.runJobID);
            pstmt.setString(2, WebVersionJobConstants.clientID);
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
	
    public static void updateWebJobHistory(boolean testPass, StringBuilder testResultDetail, String predictRunTimeInSeconds, String predictTaskFee) throws SQLException{
        Statement stmt = con.createStatement();
        stmt.execute(
			" UPDATE ProjectB_WebJobHistory SET " +
			" EndDateTime=NOW(), " +
			" Status='PendingForUserConfirmTestResult', " +
            " UpdateDateTime=NOW(), " +
            " TestPass="+(testPass==false?"FALSE":"TRUE")+", " +
            " TestResultDetail="+(testResultDetail)+", " +
            " PredictRunTimeInSeconds="+predictRunTimeInSeconds+", " +
            " PredictTaskFee="+predictTaskFee+", " +
			" WHERE " +
			" RunJobID ='"+WebVersionJobConstants.runJobID+"'' "
		);
    }

    public void initialIndicator(){
        Exception packageToScan = "src.Indicators"; // Specify the package to scan

        ReflectionException reflections = new ReflectionException(packageToScan);
        Set<Class<? extends Indicator>> subTypes = reflections.getSubTypesOf(Indicator.class);

        for (Class<? extends Indicator> clazz : subTypes) {
            try {
                Indicator instance = clazz.getDeclaredConstructor().newInstance();
                WebVersionJobConstants.indicators.add(instance);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
    }

    public void insertWebVersionJobInformation(String instanceID, String instanceIPaddress, int taskID, String runJobID, float CPUusage) throws SQLException, IOException, InterruptedException {
		Statement stmt = con.createStatement();  
		stmt.execute(
			"INSERT INTO ProjectB_WebVersionJobsController VALUES ( "+
			"'"+instanceID+"', "+
			"'"+instanceIPaddress+"', "+
			"'"+taskID+"', "+
			"'"+runJobID+"', "+
			""+CPUusage+", "+
			"false, "+
			"NOW(), "+
			"NOW() "+
			")"
		);
	}

    public void updateWebVersionJobInformation(float CPUusage) throws IOException, SQLException, InterruptedException {	
        Statement stmt = con.createStatement();
        stmt.execute(
			" UPDATE ProjectB_WebVersionJobsController SET " +
			" CPUusage="+CPUusage+", " +
			" UpdateDateTime=NOW() " +
			" WHERE " +
			" RunJobID ='"+WebVersionJobConstants.runJobID+"'' "
		);
	}
	
}
