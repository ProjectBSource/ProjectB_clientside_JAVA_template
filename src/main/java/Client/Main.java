package Client;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
		
	public static void main(String args[]) throws Exception {
		
		SocketClient dataStreaming = new SocketClient("funganything@gmail.com", "123");
		JSONObject dataStreamingRequest = new JSONObject();
		dataStreamingRequest.put("activity", "TickDataStreaming");
		dataStreamingRequest.put("market", "Future");
		dataStreamingRequest.put("index", "YM");
		dataStreamingRequest.put("startdate", "20210630");
		dataStreamingRequest.put("enddate", "20210705");
		dataStreamingRequest.put("starttime", "000000");
		dataStreamingRequest.put("endtime", "235959");
		dataStreamingRequest.put("interval", "59");
		dataStreaming.request(dataStreamingRequest);
		
		while(true) {
			System.out.print("");
			JSONObject response = dataStreaming.getResponse();
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
