package DataController;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONObject;


public class Option implements Runnable {
	public boolean processDone = false;
	public ArrayList<String> data = new ArrayList<String>();
	private JSONObject tempdata = new JSONObject();
	private String index = null;
	private Date startdate = null;
	private Date enddate = null;
	private Date starttime = null;
	private Date endtime = null;
	
	public Option(String index, String startdate, String enddate, String starttime, String endtime) {
		try {
			this.index = index;
			this.startdate = new SimpleDateFormat("").parse(startdate);
			this.enddate = new SimpleDateFormat("").parse(enddate);
			this.starttime = new SimpleDateFormat("").parse(starttime);
			this.endtime = new SimpleDateFormat("").parse(endtime);
		} catch (Exception e) {
			Constants.logger("System error ["+this.getClass().getName()+":"+e.getMessage()+"], please contact admin");
			data.add("System error ["+this.getClass().getName()+":"+e.getMessage()+"], please contact admin");
			processDone = true;
		}
	}
	
	public void run() {
		try {
			FileReader fr = new FileReader("");
			BufferedReader br = new BufferedReader(fr);
			StringBuilder sb = new StringBuilder();
			while(true) {
				if(br.read()==-1) { break; }
				else {
					sb = new StringBuilder(br.readLine());
					tempdata = new JSONObject();
					tempdata.put("datetime", "20100101010100");
					tempdata.put("index", index);
					tempdata.put("open", "999");
					tempdata.put("high", "111");
					tempdata.put("low", "222");
					tempdata.put("close", "333");
					tempdata.put("volumn", "444");
					data.add(tempdata.toString());
				}
			}
		}catch(Exception e) {
			Constants.logger("System error ["+this.getClass().getName()+":"+e.getMessage()+"], please contact admin");
			data.add("System error ["+this.getClass().getName()+":"+e.getMessage()+"], please contact admin");
			processDone = true;
		}
		processDone = true;
	}
}
