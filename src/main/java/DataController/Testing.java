package DataController;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Testing implements Runnable {

	public ArrayList<String> data = new ArrayList<String>();
	public boolean processDone = false;
	
	public void run() {
		try {
			Date next = null;
			Date target = addSeconds(new Date(), 60);
			while(true) {
				//System.out.println("aaaa:"+(next==null || new Date().after(next)));
				if(next==null || new Date().after(next)) {
					data.add("Temp data at " + new Date());
					next = addSeconds(new Date(), 1);
				}
				if(new Date().after(target)) {
					break;
				}
			}
			processDone = true;
		}catch(Exception e) {
			Constants.logger("System error ["+this.getClass().getName()+":"+e.getMessage()+"], please contact admin");
			data.add("System error ["+this.getClass().getName()+":"+e.getMessage()+"], please contact admin");
			processDone = true;
		}
	}
	
	public static Date addSeconds(Date date, Integer seconds) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.SECOND, seconds);
		return cal.getTime();
	}
}
