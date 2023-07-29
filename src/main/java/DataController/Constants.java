package DataController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Constants {
	public static String futuretickdatafilesparentpath = "";
	public static SimpleDateFormat df_yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
	public static SimpleDateFormat df_kkmmss = new SimpleDateFormat("kkmmss");
	public static SimpleDateFormat df_yyyyMMddkkmmss = new SimpleDateFormat("yyyyMMddkkmmss");
	public static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public Constants() {
		Properties prop = new Properties();
	    try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("env.properties")) {
	        prop.load(resourceAsStream);
	        futuretickdatafilesparentpath = prop.get("env.futuretickdatafilesparentpath").toString();
	    } catch (IOException e) {
	        System.err.println("Unable to load properties file : env.properties");
	    }
	}
	
	public static ArrayList<File> requiredFileLiet(String symbol, String market, Date startdate, Date enddate) throws ParseException {
		ArrayList<File> filesList = new ArrayList<File>();
		//Get the files list path
		String fileslistpath = null;
		if(market.equalsIgnoreCase("future")) {
			if(symbol.equalsIgnoreCase("YM")) { fileslistpath = futuretickdatafilesparentpath+"YM//"; }
			if(symbol.equalsIgnoreCase("HSI")) { fileslistpath = futuretickdatafilesparentpath+"HSI//"; }
		}
		
		//Find the required files
		File f = new File(fileslistpath);
		for(String pathname : f.list()) {
			if(pathname.contains("formated.txt")) {
				String filename = pathname.split("[.]")[0];
				Date fileDateRangeStart = df_yyyyMMdd.parse(filename.split("_")[0]);
				Date fileDateRangeEnd = df_yyyyMMdd.parse(filename.split("_")[1]);
				
				//Check the file date range required or not
				boolean within = false;
				if( (startdate.before(fileDateRangeStart) || startdate.equals(fileDateRangeStart)) && (enddate.after(fileDateRangeStart) || enddate.equals(fileDateRangeStart) ) ) { within=true; }
				else if(startdate.after(fileDateRangeStart) && startdate.before(fileDateRangeEnd)) { within=true; }
				else if(enddate.after(fileDateRangeStart) && enddate.before(fileDateRangeEnd)) { within=true; }
				
				if(within==true) {
					filesList.add(new File(fileslistpath+"/"+pathname));
				}
			}
		}
		
		//sort the file
		ArrayList<File> sortedfilesList = new ArrayList<File>();
		if(filesList.size()==0) {
			return null;
		}
		else {
			if(sortedfilesList.size()==0) { sortedfilesList.add(filesList.get(0)); }
			for(int j=0; j<filesList.size(); j++) {
				int filesStartdate = Integer.parseInt(filesList.get(j).getName().split("_")[0]);
				int targetposition = 0;
				for(int i=0; i<sortedfilesList.size(); i++) {
					int sortedfilesStartdate = Integer.parseInt(sortedfilesList.get(i).getName().split("_")[0]);
					if(filesStartdate > sortedfilesStartdate) {
						targetposition = i+1;
					}
					if(filesStartdate == sortedfilesStartdate) {
						targetposition = -1;
					}
				}
				if(targetposition>-1) {
					sortedfilesList.add(targetposition, filesList.get(j));
				}
			}
		}
		
		return sortedfilesList;
	}
	
	public static boolean withinDateOrNot(Date from, Date to, Date target) {
		if( 
			(target.equals(from) || target.after(from)) &&
			(target.equals(to) || target.before(to))
		)return true;
		else return false;
	}
	
	public static boolean outOfEndDateOrNot(Date to, Date target) {
		if(target.after(to))return true;
		else return false;
	}
	
	public boolean withinTimeOrNot(Date from, Date to, Date target) {
		if( 
			(target.equals(from) || target.after(from)) &&
			(target.equals(to) || target.before(to))
		)return true;
		else return false;
	}
	
	public static Date randomStartEndDateRangeForFreeTrail(Date startdate, Date enddate) {
	    Random rand = new Random(); 
	    int randomDay = rand.nextInt(daysBetween(startdate, enddate));
	    int tempcount = 0;
		for(Date sd = startdate; sd.before(enddate) || sd.equals(enddate); sd = addDates(sd, 1)) {
			if(tempcount==randomDay) {
				return sd;
			}
			tempcount++;
		}
		return randomStartEndDateRangeForFreeTrail(startdate, enddate);
	}
	
	public static Date addSeconds(Date date, Integer seconds) {
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    cal.add(Calendar.SECOND, seconds);
	    return cal.getTime();
	}
	
	public static Date addDates(Date date, Integer dates) {
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    cal.add(Calendar.DATE, dates);
	    return cal.getTime();
	}
	
	public static int daysBetween(Date start, Date end) {
	    long diff = end.getTime() - start.getTime();
	    return (int) (diff / (1000 * 60 * 60 * 24));
	}
	
	public static void logger(String content) {
		logger.log(Level.WARNING, content);
	}
}
