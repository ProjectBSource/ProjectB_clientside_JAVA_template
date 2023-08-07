package Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.json.JSONObject;

import Main.LoggerFilter;
import Main.LoggerFormatter;
import Main.LoggerHandler;

public class WebTaskConstants {
	
	public static int serverPort = -1;
	public static ArrayList<Socket> socketList = new ArrayList<Socket>();
	public static ArrayList<Thread> threadList = new ArrayList<Thread>();
	public final static String tickdataStreamingRequest = "TickDataStreaming";
	public final static String intervaldataStreamingRequest = "IntervalDataStreaming";
	public final static String dataStreamingTestingRequest = "Testing";
	public final static String dataStreamingFutureRequest = "Future";
	private static ClassLoader loader = Thread.currentThread().getContextClassLoader();
	private static String loggerstoragepath;
	private static String environment;
	public static Logger logger = Logger.getLogger(new WebTaskConstants().getClass().getName());
	private static Process p;
	private static BufferedReader br;
	private static String [] cmd ={"/bin/sh","-c",null};
	public static String serverIPaddress;
	public static String serverInstanceID;
	public static String serverScreenTaskID;
	public static String serverRunJobTaskID;
	public static float servercpuusage;
	private static DataBaseCommunication dbcommunication;
	private static Date lastServerInformationUpdateDateTime = null;
    public static String clientID = "";
    public static String runJobID = "";
	
	public static void setupEnvironmentProperties() {
		Properties prop = new Properties();
	    try (InputStream resourceAsStream = loader.getResourceAsStream("env.properties")) {
	        prop.load(resourceAsStream);
	        loggerstoragepath = prop.get("env.loggerstoragepath").toString();
	        environment = prop.get("env.environment").toString();
	        logger("setupEnvironmentProperties() completed");
	    } catch (IOException e) {
	    	logger("Unable to load properties file : env.properties");
	    }
	}

    public static void setIPaddress() throws IOException {
		if(environment.equals("dev")) {
			serverIPaddress = "localhost";
		}
		else if(environment.equals("prd")) {
			URL whatismyip = new URL("http://checkip.amazonaws.com");
	  		BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
	  		serverIPaddress = in.readLine();
		}
  		logger("setServerIPaddress() completed, serverIPaddress:"+serverIPaddress);
	}
	
	public static void setupDBconnection() {
		try {
			dbcommunication = new DataBaseCommunication();
		} catch (Exception e) {
			logger("Unable to setupDBconnection() : "+e);
		}
	}
	
	public static int renewDBconnection() {
		try {
			return dbcommunication.renewConnection();
		} catch (Exception e) {
			logger("Unable to renewDBconnection() : "+e);
		}
		return -1;
	}
	
	public static void setupLogger() throws SecurityException, IOException {
		LogManager.getLogManager().readConfiguration(loader.getResourceAsStream("commons-logging.properties"));
		logger.setLevel(Level.WARNING);
		logger.addHandler(new LoggerHandler());
    	//FileHandler file name with max size and number of log files limit
        Handler fileHandler = new FileHandler(loggerstoragepath, 2000, 5);
        fileHandler.setFormatter(new LoggerFormatter());
        fileHandler.setFilter(new LoggerFilter());
        logger.addHandler(fileHandler);
        logger("setupLogger() completed");
	}

    public static void logger(String content) {
		logger.log(Level.WARNING, content);
	}

    public static JSONObject getMessage() throws SQLException {
        String message = dbcommunication.getMessage();
        if(message!=null){
            return new JSONObject(message);
        }
        return null;
	}

    public static void updateTestResult(boolean testPass, StringBuilder testPassDetail){
        dbcommunication.updateTestResult(testPass, testPassDetail);
    }

    /*
    public static boolean requestLogicValidation(ArrayList<String> errorMessage){
        
    }
	*/

    public static boolean requestSyntaxValidation(JSONObject input){
        ArrayList<String> errorMessage = new ArrayList<String>();
        if(input==null){
            errorMessage.add("Require message can not be null");
        }
        if(input.has("clientID")==false) { errorMessage.add("Error: Do not have client ID"); }
		if(input.has("clientID")==true) {
			if(input.isNull("clientID") || input.get("clientID").getClass()!=(String.class) || input.getString("clientID").isEmpty() || input.getString("clientID").isBlank()) {
				errorMessage.add("Error: incorrect clientID");
			}
		}
		
		if(input.has("accessCode")==false) { errorMessage.add("Error: Do not have API access code"); }
		if(input.has("accessCode")==true) {
			if(input.isNull("accessCode") || input.get("accessCode").getClass()!=(String.class) || input.getString("accessCode").isEmpty() || input.getString("accessCode").isBlank()) {
				errorMessage.add("Error: incorrect accessCode");
			}
		}
		
		if(input.has("jobName")==true) { 
			if(input.isNull("jobName") || input.get("jobName").getClass()!=(String.class) || input.getString("jobName").isEmpty() || input.getString("jobName").isBlank()) {
				errorMessage.add("Error: incorrect alias");
			} 
		}
		
		if(input.has("interval")==true) {
			if(input.isNull("interval") || input.get("interval").getClass()!=(Integer.class) ) {
				errorMessage.add("Error: incorrect interval");
			}
		}
		
		if(input.has("dataChangeLimitInPrecentage")==true) {
			if(input.isNull("dataChangeLimitInPrecentage") || input.get("dataChangeLimitInPrecentage").getClass()!=(Integer.class) || input.getDouble("dataChangeLimitInPrecentage")<0) {
				errorMessage.add("Error: incorrect dataChangeLimitInPrecentage");
			}
		}
		
		if(input.has("activity")==false) { errorMessage.add("Error: Do not have the key 'activity'"); }
		else if(input.isNull("activity") || input.get("activity").getClass()!=(String.class) || input.getString("activity").isEmpty() || input.getString("activity").isBlank()) { errorMessage.add("Error: Key 'activity' no value"); }
		else if(input.has("activity")==true) {
			if(
				input.getString("activity").equalsIgnoreCase(tickdataStreamingRequest) == false &&
				input.getString("activity").equalsIgnoreCase(intervaldataStreamingRequest) == false
			) {
				errorMessage.add("Error: Key 'activity' incorrect value");
			}
		}
		
		else if(input.has("market")==false) { errorMessage.add("Error: Do not have the key 'market'"); }
		else if(input.isNull("market") || input.get("market").getClass()!=(String.class) || input.getString("market").isEmpty() || input.getString("market").isBlank()) { errorMessage.add("Error: Key 'market' no value"); }
		else if(input.has("market")==true) {
			if(
				input.getString("market").equalsIgnoreCase(dataStreamingFutureRequest) == false
			) {
				errorMessage.add("Error: Key 'market' incorrect value");
			}
		}
		
		else if(input.getString("market").equalsIgnoreCase(dataStreamingTestingRequest)==false) { 
			if(input.has("index")==false) { errorMessage.add("Error: Do not have the key 'index'"); }
			else if(input.isNull("index") || input.get("index").getClass()!=(String.class) || input.getString("index").isEmpty() || input.getString("index").isBlank()) { errorMessage.add("Error: Key 'index' no value"); }
			
			else if(input.has("startdate")==false) { errorMessage.add("Error: Do not have the key 'startdate'"); }
			else if(input.isNull("startdate") || input.get("startdate").getClass()!=(String.class) || input.getString("startdate").isEmpty() || input.getString("startdate").isBlank()) { errorMessage.add("Error: Key 'startdate' no value"); }
			
			else if(input.has("enddate")==false) { errorMessage.add("Error: Do not have the key 'enddate'"); }
			else if(input.isNull("enddate") || input.get("enddate").getClass()!=(String.class) || input.getString("enddate").isEmpty() || input.getString("enddate").isBlank()) { errorMessage.add("Error: Key 'enddate' no value"); }
			
			else if(input.has("starttime")==false) { errorMessage.add("Error: Do not have the key 'starttime'"); }
			else if(input.isNull("starttime") || input.get("starttime").getClass()!=(String.class) || input.getString("starttime").isEmpty() || input.getString("starttime").isBlank()) { errorMessage.add("Error: Key 'starttime' no value"); }
			
			else if(input.has("endtime")==false) { errorMessage.add("Error: Do not have the key 'endtime'"); }
			else if(input.isNull("endtime") || input.get("endtime").getClass()!=(String.class) || input.getString("endtime").isEmpty() || input.getString("endtime").isBlank()) { errorMessage.add("Error: Key 'endtime' no value"); }
		}

        if(errorMessage.size()>0){
            StringBuilder resultDetail = new StringBuilder();
            for(String e : errorMessage){
                resultDetail.append(e);
                resultDetail.append("\n");
            }
            updateTestResult(false, resultDetail);
            return false;
        }
        return true;
    }
}
