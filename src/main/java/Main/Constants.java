package Main;

import java.io.BufferedReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

public class Constants {
	
	public static int serverPort = -1;
	public static ArrayList<Socket> socketList = new ArrayList<Socket>();
	public static ArrayList<Thread> threadList = new ArrayList<Thread>();
	public final static String tickdataStreamingRequest = "TickDataStreaming";
	public final static String intervaldataStreamingRequest = "IntervalDataStreaming";
	public final static String dataStreamingTestingRequest = "Testing";
	public final static String dataStreamingFutureRequest = "Future";
	public final static String dataStreamingOptionRequest = "Option";
	private static ClassLoader loader = Thread.currentThread().getContextClassLoader();
	private static String loggerstoragepath;
	private static String environment;
	public static Logger logger = Logger.getLogger(new Constants().getClass().getName());
	private static Process p;
	private static BufferedReader br;
	private static String [] cmd ={"/bin/sh","-c",null};
	public static String taskIPaddress;
	public static String taskInstanceID;
	public static String serverScreenTaskID;
	public static String serverRunJobTaskID;
	public static float cpuusage;
	private static Date lastServerInformationUpdateDateTime = null;
	
	
}
