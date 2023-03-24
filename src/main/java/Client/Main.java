package Client;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import Util.DataStreamingRequestStructure;
import Util.DataStructure;
import Util.PlaceLimitOrderRequestStructure;
import Util.PlaceMarketOrderRequestStructure;
import Util.PlaceStopLimitOrderRequestStructure;
import Util.PlaceStopOrderRequestStructure;

public class Main {
	
	public static void main(String args[]) throws Exception {
		
		ObjectMapper mapper = new ObjectMapper();
		DataStreamingRequestStructure drs;
		PlaceMarketOrderRequestStructure marketOrder;
		PlaceLimitOrderRequestStructure limitOrder;
		PlaceStopOrderRequestStructure stopOrder;
		PlaceStopLimitOrderRequestStructure stoplimitOrder;
		DataStructure dataStructure;
		
		drs = new DataStreamingRequestStructure("IntervalDataStreaming", "Future", "YM", "20210630", "20210705", "000000", "235959", 59);
		JSONObject dataStreamingRequest = new JSONObject(mapper.writeValueAsString(drs));

		marketOrder = new PlaceMarketOrderRequestStructure("PlaceOrder", "[dataSourceID]", "BUY", 1);
		JSONObject placeMarketOrderRequest = new JSONObject(mapper.writeValueAsString(marketOrder));
		
		limitOrder = new PlaceLimitOrderRequestStructure("PlaceOrder", "[dataSourceID]", "BUY", 1, 123.456);
		JSONObject placeLimitOrderRequest = new JSONObject(mapper.writeValueAsString(limitOrder));
		
		stopOrder = new PlaceStopOrderRequestStructure("PlaceOrder", "[dataSourceID]", "BUY", 1, 123.456);
		JSONObject placeStopOrderRequest = new JSONObject(mapper.writeValueAsString(stopOrder));
		
		stoplimitOrder = new PlaceStopLimitOrderRequestStructure("PlaceOrder", "[dataSourceID]", "BUY", 1, 12.34, 56.78);
		JSONObject placeStopLimitOrderRequest = new JSONObject(mapper.writeValueAsString(stoplimitOrder));
		
		SocketClient sc = new SocketClient("funganything@gmail.com", "123");
		sc.request(dataStreamingRequest);
		
		while(true) {
			System.out.print("");
			JSONObject response = sc.getResponse();
			if(!response.isEmpty()) {
				mapper = new ObjectMapper();
				if(response.has("type") && response.getString("type").contains("DataStreaming")) {
					dataStructure = mapper.readValue(response.toString(), DataStructure.class);
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
				
				System.out.println(response.toString());
				
				sc.request(placeMarketOrderRequest);
			}
		}
	}
}
