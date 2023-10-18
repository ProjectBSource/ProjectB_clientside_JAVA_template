package Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import Indicators.Indicator;
import Main.LoggerFilter;
import Main.LoggerFormatter;
import Main.LoggerHandler;

public class WebVersionJobConstants {
	
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
	public static Logger logger = Logger.getLogger(new WebVersionJobConstants().getClass().getName());
	private static Process p;
	private static BufferedReader br;
	private static String [] cmd ={"/bin/sh","-c",null};
	public static String serverIPaddress;
	public static String serverInstanceID;
	public static String serverScreenTaskID;
	public static String serverRunJobTaskID;
	public static float cpuusage;
	private static DataBaseCommunication dbcommunication;
	private static Date lastWebVersionJobInformationUpdateDateTime = null;
    public static String clientID = "";
    public static String runJobID = "";
	public static ArrayList<Indicator> indicators = new ArrayList<>();
	
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
	
	public static void setWebVersionJobIPaddress() throws IOException {
		if(environment.equals("dev")) {
			serverIPaddress = "localhost";
		}
		else if(environment.equals("prd")) {
			URL whatismyip = new URL("http://checkip.amazonaws.com");
	  		BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
	  		serverIPaddress = in.readLine();
		}
  		logger("setWebVersionJobIPaddress() completed, serverIPaddress:"+serverIPaddress);
	}
	
	public static void setWebVersionJobInstanceID() throws IOException, InterruptedException {
		if(environment.equals("dev")) {
			serverInstanceID = "abcd1234";
		}
		else if(environment.equals("prd")) {
			cmd[2] = "ec2-metadata -i | awk '{print $2}'";
			p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
	        br = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        serverInstanceID = br.readLine();
	        br.close();
	        p.destroy();
		}
        logger("setWebVersionJobInstanceID() completed");
	}
	
	public static void setWebVersionJobScreenTaskID() throws IOException, InterruptedException {
		if(environment.equals("dev")) {
			serverScreenTaskID = "0000";
		}
		else if(environment.equals("prd")) {
			cmd[2] = "ps -ef | grep 'java -jar /home/ec2-user/dataSource/webVersion/Jobs/"+runJobID+"/ProjectB_clientside_JAVA_template/ProjectB_clientside_JAVA_template-0.0.1-SNAPSHOT-jar-with-dependencies.jar "+runJobID+"' | awk '{print $2}'"; 
			p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			serverScreenTaskID = br.readLine();
	        br.close();
	        p.destroy();
		}
        logger("setWebVersionJobScreenTaskID() completed, serverScreenTaskID:"+serverScreenTaskID);
	}
	
	public static void setWebVersionJobRunJobTaskID() throws IOException, InterruptedException {
		if(environment.equals("dev")) {
			serverScreenTaskID = "1111";
		}
		else if(environment.equals("prd")) {
			cmd[2] = "ps --no-headers --ppid "+serverScreenTaskID+" | awk '{print $1}'"; 
			p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			serverRunJobTaskID = br.readLine();
	        br.close();
	        p.destroy();
		}
        logger("setWebVersionJobRunJobTaskID() completed, serverRunJobTaskID:"+serverRunJobTaskID);
	}
	
	public static void setWebVersionJobCPUUsage() throws IOException, InterruptedException {
		if(environment.equals("dev")) {
			cpuusage = 0;
		}
		else if(environment.equals("prd")) {
			cmd[2] = "ps -eo %cpu,pid | grep "+serverRunJobTaskID+" | awk '{print $1}'";
			p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
	        br = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        cpuusage = Float.parseFloat(br.readLine());
	        br.close();
	        p.destroy();
		}
        logger("setWebVersionJobCPUUsage() completed, cpuusage:"+cpuusage);
	}
	
	public static void stopWebVersionJob() throws IOException, InterruptedException {
		cmd[2] = "screen -X -S "+serverScreenTaskID+" quit"; 
		p = Runtime.getRuntime().exec(cmd);
		p.waitFor();
        p.destroy();
        logger("stopWebVersionJob() completed");
	}

	public static JSONObject getRequestMessage() throws ParseException{
		return dbcommunication.getRequestMessage();
    }
	
	public static void updateWebJobHistory(boolean testPass, StringBuilder testResultDetail, String predictRunTimeInSeconds, String predictTaskFee) throws SQLException{
		dbcommunication.updateWebJobHistory(testPass, testResultDetail, predictRunTimeInSeconds, predictTaskFee);
    }

	public static void updateWebVersionJobInformation() throws IOException, SQLException, InterruptedException{		
		//refresh the CPU usage 
		setWebVersionJobCPUUsage();
		
		dbcommunication.updateWebVersionJobInformation(cpuusage);
	}

	public static ArrayList<String> subscribedDataListValidation(JSONArray nodeDataArray) throws Exception{
		ArrayList<String> errorMessage = new ArrayList<String>();
		int subscribedDataListCount = 0;
		for(Object objectNode : nodeDataArray){
            JSONObject node = (JSONObject) objectNode;
			if(node.has("subscribedDataList")){
				subscribedDataListCount++;
				if(node.has("data")){
					JSONObject data = new JSONObject(node.getString("data"));

					if(data.has("activity")==false) { errorMessage.add("Error: Do not have the key 'activity'"); }
					else if(data.isNull("activity") || data.get("activity").getClass()!=(String.class) || data.getString("activity").isEmpty()) { errorMessage.add("Error: Key 'activity' no value"); }
					else if(data.has("activity")==true) {
						if(
							data.getString("activity").equalsIgnoreCase(tickdataStreamingRequest) == false &&
							data.getString("activity").equalsIgnoreCase(intervaldataStreamingRequest) == false
						) {
							errorMessage.add("Error: Key 'activity' incorrect value");
						}
					}

					else if(data.has("market")==false) { errorMessage.add("Error: Do not have the key 'market'"); }
					else if(data.isNull("market") || data.get("market").getClass()!=(String.class) || data.getString("market").isEmpty()) { errorMessage.add("Error: Key 'market' no value"); }
					else if(data.has("market")==true) {
						if(
							data.getString("market").equalsIgnoreCase(dataStreamingFutureRequest) == false
						) {
							errorMessage.add("Error: Key 'market' incorrect value");
						}
					}

					else if(data.getString("market").equalsIgnoreCase(dataStreamingTestingRequest)==false) { 
						if(data.has("index")==false) { errorMessage.add("Error: Do not have the key 'index'"); }
						else if(data.isNull("index") || data.get("index").getClass()!=(String.class) || data.getString("index").isEmpty()) { errorMessage.add("Error: Key 'index' no value"); }
						else if(data.has("startdate")==false) { errorMessage.add("Error: Do not have the key 'startdate'"); }
						else if(data.isNull("startdate") || data.get("startdate").getClass()!=(String.class) || data.getString("startdate").isEmpty()) { errorMessage.add("Error: Key 'startdate' no value"); }
						else if(data.has("enddate")==false) { errorMessage.add("Error: Do not have the key 'enddate'"); }
						else if(data.isNull("enddate") || data.get("enddate").getClass()!=(String.class) || data.getString("enddate").isEmpty()) { errorMessage.add("Error: Key 'enddate' no value"); }
						else if(data.has("starttime")==false) { errorMessage.add("Error: Do not have the key 'starttime'"); }
						else if(data.isNull("starttime") || data.get("starttime").getClass()!=(String.class) || data.getString("starttime").isEmpty()) { errorMessage.add("Error: Key 'starttime' no value"); }
						else if(data.has("endtime")==false) { errorMessage.add("Error: Do not have the key 'endtime'"); }
						else if(data.isNull("endtime") || data.get("endtime").getClass()!=(String.class) || data.getString("endtime").isEmpty()) { errorMessage.add("Error: Key 'endtime' no value"); }
						else if(data.has("interval") && data.get("interval").getClass()!=(Double.class) || data.getDouble("interval")<=0) { errorMessage.add("Error: Key 'endtime' no value"); }
					}
				}
				else{
					errorMessage.add("Error: data element not found, please contact admin");
				}
			}
		}
		if(subscribedDataListCount==0){ 
			errorMessage.add("Error: Subscribed Data not found"); 
		}

		return errorMessage;
    }

	public static ArrayList<String> commonIndicatorListValidation(JSONArray nodeDataArray) throws Exception{
		ArrayList<String> errorMessage = new ArrayList<String>();
		int commonIndicatorListCount = 0;
		for(Object objectNode : nodeDataArray){
            JSONObject node = (JSONObject) objectNode;
			if(node.has("commonIndicatorList")){
				commonIndicatorListCount++;
				if(node.has("data")){
					JSONObject data = new JSONObject(node.getString("data"));

					if(data.has("indicatorName")==false) { errorMessage.add("Error: Do not have the key 'indicatorName'"); }
					else if(data.isNull("indicatorName") || data.get("indicatorName").getClass()!=(String.class) || data.getString("indicatorName").isEmpty()) { errorMessage.add("Error: Key 'indicatorName' no value"); }
					else if(data.has("indicatorName")==true) {
						boolean indicatorFunctionExist = false;
						Indicator tempIndicator = null;
						for(Indicator i : indicators){
							if(i.indicatorName.equals("Bollinger Bands")){
								indicatorFunctionExist=true; 
								tempIndicator = i;
								break; 
							}
						}
						if(indicatorFunctionExist==false) {
							errorMessage.add("Error: Key 'indicatorName' incorrect value");
						}else{
							int parametersCount = 0;
							for(int i=0; i<5; i++){
								if(data.has("parameter"+i)==true){
									if(data.isNull("parameter"+i) || data.get("parameter"+i).getClass()!=(Double.class) || data.getDouble("parameter"+i)<=0) { errorMessage.add("Error: Key 'parameter"+i+"' no value"); }
									else if(data.has("parameter"+i)==true){ parametersCount++; }
								}
							}
							if(parametersCount != tempIndicator.parametersAmount){
								errorMessage.add("Error: parameter(s) amount mismatched");
							}
						}
					}
				}
				else{
					errorMessage.add("Error: data element of Common Indicator not found, please contact admin");
				}
			}
		}
		if(commonIndicatorListCount==0){ 
			errorMessage.add("Error: Common Indicator not found"); 
		}

		return errorMessage;
    }

	public static ArrayList<String> indicatorOutputValidation(JSONArray nodeDataArray) throws Exception{
		ArrayList<String> errorMessage = new ArrayList<String>();
		int indicatorOutputCount = 0;
		for(Object objectNode : nodeDataArray){
            JSONObject node = (JSONObject) objectNode;
			if(node.has("category")){
				if(node.getString("category").equals("IndicatorOutput")){
					indicatorOutputCount++;
					if(node.has("data")){
						JSONObject data = new JSONObject(node.getString("data"));
						
						if(data.has("outputParameter")==false) { errorMessage.add("Error: Do not have the key 'outputParameter' in 'IndicatorOutput'"); }
						else if(data.isNull("outputParameter") || data.get("outputParameter").getClass()!=(String.class) || data.getString("outputParameter").isEmpty()) { errorMessage.add("Error: Key 'outputParameter' no value"); }
						if(data.has("operator")==false) { errorMessage.add("Error: Do not have the key 'operator' in 'IndicatorOutput'"); }
						else if(data.isNull("operator") || data.get("operator").getClass()!=(String.class) || data.getString("operator").isEmpty()) { errorMessage.add("Error: Key 'operator' no value"); }
						if(data.has("constant0")==false) { errorMessage.add("Error: Do not have the key 'constant0' in 'IndicatorOutput'"); }
						else if(data.isNull("constant0") || data.get("constant0").getClass()!=(String.class) || data.getString("constant0").isEmpty()) { errorMessage.add("Error: Key 'constant0' no value"); }
					}
				}
			}
		}
		if(indicatorOutputCount==0){ 
			errorMessage.add("Error: Indicator Output not found"); 
		}

		return errorMessage;
	}

	public static ArrayList<String> tradeActionValidation(JSONArray nodeDataArray) throws Exception{
		ArrayList<String> errorMessage = new ArrayList<String>();
		int tradeActionCount = 0;
		for(Object objectNode : nodeDataArray){
            JSONObject node = (JSONObject) objectNode;
			if(node.has("category")){
                if(node.getString("category").equals("SELL") || node.getString("category").equals("BUY")){
					tradeActionCount++;
					if(node.has("action")){
						JSONObject data = new JSONObject(node.getString("data"));
						
						if(data.has("action")==false) { errorMessage.add("Error: Do not have the key 'action' in Trade Action"); }
						else if(data.isNull("action") || data.get("action").getClass()!=(String.class) || data.getString("action").isEmpty()) { errorMessage.add("Error: Key 'action' no value"); }
						else if(data.getString("action")!="BUY" && data.getString("action")!="SELL") { errorMessage.add("Error: Key 'action' incorrect value"); }
						if(data.has("quality")==false) { errorMessage.add("Error: Do not have the key 'quality' in Trade Action"); }
						else if(data.isNull("quality") || data.get("quality").getClass()!=(Double.class) || data.getString("action").isEmpty()) { errorMessage.add("Error: Key 'action' no value"); }
						else if(data.getDouble("quality")<=0) { errorMessage.add("Error: Key 'quality' incorrect value"); }
					}
				}
			}
		}
		if(tradeActionCount==0){ 
			errorMessage.add("Error: Trade Action not found"); 
		}

		return errorMessage;
	}

    public static void initialIndicator() {
    }

    public static void insertWebVersionJobInformation() {
    }
}
