package Client;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {

	private static SimpleDateFormat df_yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat df_kkmmss = new SimpleDateFormat("kkmmss");
	private static SimpleDateFormat df_yyyyMMddkkmmss = new SimpleDateFormat("yyyyMMddkkmmss");
	private static JSONObject response = null;
	
	public static void main(String args[]) throws Exception {
		JSONObject obj = new JSONObject();
		obj.put("activity", "TickDataStreaming");
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

				ObjectMapper mapper = new ObjectMapper();
				DataStructure dataStructure = mapper.readValue(response.toString(), DataStructure.class);
				if(dataStructure.getDone()!=null) {
					break;
				}
				if(dataStructure.getError()!=null) {
					System.out.println(dataStructure.getError());
					break;
				}
				
				System.out.println( 
					String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",
						dataStructure.getType(),
						dataStructure.getDatetime(),
						dataStructure.getIndex(),
						dataStructure.getVolumn(),
						dataStructure.getOpen(),
						dataStructure.getHigh(),
						dataStructure.getLow(),
						dataStructure.getClose(),
						dataStructure.getTotal_volumn()
					) 
				);
			}
		}
	}
}
