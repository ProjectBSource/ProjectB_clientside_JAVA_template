package Client;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

public class Main {

	private static SimpleDateFormat df_yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat df_kkmmss = new SimpleDateFormat("kkmmss");
	private static SimpleDateFormat df_yyyyMMddkkmmss = new SimpleDateFormat("yyyyMMddkkmmss");
	private static JSONObject response = null;
	
	public static void main(String args[]) throws Exception {
		JSONObject obj = new JSONObject();
		obj.put("activity", "DataStreaming");
		obj.put("market", "Future");
		obj.put("index", "YM");
		obj.put("startdate", "20210630");
		obj.put("enddate", "20220228");
		obj.put("starttime", "000000");
		obj.put("endtime", "235959");
		obj.put("interval", "59");
		
		SocketClient sc = new SocketClient("funganything@gmail.com", "123");
		sc.request(obj);
		
		while(true) {
			response = sc.getResponse();
			if(!response.isEmpty()) {
				if(doneOrNot(response)==true) {
					break;
				}
				System.out.println( 
					String.format("%s,%s,%s,%s,%s,%s,%s,%s",
						getDateTime(response),
						getIndex(response),
						getVolumn(response),
						getOpen(response),
						getHigh(response),
						getLow(response),
						getClose(response),
						getTotalVolumn(response)
					) 
				);
			}
		}
	}
	
	public static Date getDate(JSONObject JSONresponse) throws Exception {
		if( JSONresponse!=null && JSONresponse.has("date") ) {
			return df_yyyyMMdd.parse(JSONresponse.getString("date"));
		}else {
			return null;
		}
	}
	
	public static Date getTime(JSONObject JSONresponse) throws Exception {
		if( JSONresponse!=null && JSONresponse.has("time") ) {
			return df_kkmmss.parse(JSONresponse.getString("time"));
		}else {
			return null;
		}
	}
	
	public static Date getDateTime(JSONObject JSONresponse) throws Exception {
		if( JSONresponse!=null && JSONresponse.has("datetime") ) {
			return df_yyyyMMddkkmmss.parse(JSONresponse.getString("datetime"));
		}else {
			return null;
		}
	}
	
	public static float getIndex(JSONObject JSONresponse) throws Exception {
		if( JSONresponse!=null && JSONresponse.has("index") ) {
			return Float.parseFloat(JSONresponse.getString("index"));
		}else {
			return -1;
		}
	}
	
	public static int getVolumn(JSONObject JSONresponse) throws Exception {
		if( JSONresponse!=null && JSONresponse.has("volumn") ) {
			return Integer.parseInt(JSONresponse.getString("volumn"));
		}else {
			return -1;
		}
	}
	
	public static float getOpen(JSONObject JSONresponse) throws Exception {
		if( JSONresponse!=null && JSONresponse.has("open") ) {
			return Float.parseFloat(JSONresponse.getString("open"));
		}else {
			return -1;
		}
	}
	
	public static float getHigh(JSONObject JSONresponse) throws Exception {
		if( JSONresponse!=null && JSONresponse.has("high") ) {
			return Float.parseFloat(JSONresponse.getString("high"));
		}else {
			return -1;
		}
	}
	
	public static float getLow(JSONObject JSONresponse) throws Exception {
		if( JSONresponse!=null && JSONresponse.has("low") ) {
			return Float.parseFloat(JSONresponse.getString("low"));
		}else {
			return -1;
		}
	}
	
	public static float getClose(JSONObject JSONresponse) throws Exception {
		if( JSONresponse!=null && JSONresponse.has("close") ) {
			return Float.parseFloat(JSONresponse.getString("close"));
		}else {
			return -1;
		}
	}
	
	public static int getTotalVolumn(JSONObject JSONresponse) throws Exception {
		if( JSONresponse!=null && JSONresponse.has("total volumn") ) {
			return Integer.parseInt(JSONresponse.getString("total volumn"));
		}else {
			return -1;
		}
	}
	
	public static boolean doneOrNot(JSONObject JSONresponse) throws Exception {
		if( JSONresponse!=null && JSONresponse.has("done") ) {
			return true;
		}else {
			return false;
		}
	}
}
