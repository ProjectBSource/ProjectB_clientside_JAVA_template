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
	private static ClassLoader loader = Thread.currentThread().getContextClassLoader();
	
	
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
}
