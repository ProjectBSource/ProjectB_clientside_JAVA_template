package Util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import Util.WebVersionJobConstants;

import org.json.JSONObject;

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
		boolean reachable = con.isValid(10);
		WebVersionJobConstants.logger("reachable:"+(reachable)+", con==null?"+(con==null));
	}
	
	public int renewConnection() throws SQLException {
		createConnection();
		renewConnectiontimeCount++;
		return renewConnectiontimeCount;
	}

    public static JSONObject getRequestMessage() throws ParseException{
	String message = null;
        try{
			Statement stmt = con.createStatement();  
			ResultSet rs = stmt.executeQuery("SELECT RequestMessage FROM ProjectB_WebJobHistory WHERE RunJobID='"+WebVersionJobConstants.runJobID+"'");  
			boolean result = false;
			while (rs.next()) {
				message = rs.getString("RequestMessage");
			}
		}
        catch (SQLException e) {
            e.printStackTrace();
			WebVersionJobConstants.logger("Exception in DataBaseCommunication.java : " + e.toString());
        }
		if(message!=null){
			message = message.substring(1, message.length()-1);
			return new JSONObject(message);
		}
        return null;
    }
	
    public static void updateWebJobHistory(boolean testPass, StringBuilder testResultDetail, String predictRunTimeInSeconds, String predictTaskFee, String status) throws SQLException{
        Statement stmt = con.createStatement();
        stmt.execute(
			" UPDATE ProjectB_WebJobHistory SET " +
			" EndDateTime=NOW(), " +
			" Status=\""+status+"\", " +
            " UpdateDateTime=NOW(), " +
            " TestPass="+(testPass==false?"FALSE":"TRUE")+", " +
            " TestResultDetail=\""+(testResultDetail==null?"":testResultDetail.toString())+"\", " +
            " PredictRunTimeInSeconds="+predictRunTimeInSeconds+", " +
            " PredictTaskFee="+predictTaskFee+" " +
			" WHERE " +
			" RunJobID =\""+WebVersionJobConstants.runJobID+"\" "
		);
    }

    public void insertWebVersionJobInformation(String instanceID, String instanceIPaddress, String taskID, String runJobID, float CPUusage) throws SQLException, IOException, InterruptedException {
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

    public void updateWebVersionJobInformation() throws IOException, SQLException, InterruptedException {	
        Statement stmt = con.createStatement();
        stmt.execute(
			" UPDATE ProjectB_WebVersionJobsController SET " +
			//" CPUusage="+WebVersionJobConstants.cpuusage+", " +
            " CPUusage=0, " +
			" UpdateDateTime=NOW() " +
			" WHERE " +
			" RunJobID ="+WebVersionJobConstants.runJobID+" "
		);
	}
	
}
