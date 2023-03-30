package Main;

import java.io.PrintStream;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import ClientSocketControl.DataStructure;
import ClientSocketControl.SocketClient;
import TradeControl.TradeController;
import TradeControl.OrderActionConstants.Action;

public class Main {
		
	public static void main(String args[]) throws Exception {
		
		//Login here
		SocketClient dataStreaming = new SocketClient("funganything@gmail.com", "123");
		
		//Form JSON object message for data streaming request
		JSONObject dataStreamingRequest = new JSONObject();
		dataStreamingRequest.put("activity", "TickDataStreaming");
		dataStreamingRequest.put("market", "Future");
		dataStreamingRequest.put("index", "YM");
		dataStreamingRequest.put("startdate", "20210630");
		dataStreamingRequest.put("enddate", "20210705");
		dataStreamingRequest.put("starttime", "000000");
		dataStreamingRequest.put("endtime", "235959");
		dataStreamingRequest.put("interval", "59");
		
		//Send the request to server
		dataStreaming.request(dataStreamingRequest);
		
		/*
		 * This template included a simple account and order management function
		 * You may modify the function to fit your back test
		 */
		TradeController tradeController = new TradeController();
		
		//Initial the ObjectMapper
		ObjectMapper mapper = new ObjectMapper();
		//Initial the JSONObject 
		JSONObject response = null;
		
		while(true) {
			//get the response
			response = dataStreaming.getResponse();
			if(!response.isEmpty()) {
				System.out.flush();
				//Convert response JSON message to Java class object
				DataStructure dataStructure = mapper.readValue(response.toString(), DataStructure.class);
				//Check response finished or not
				if(dataStructure.getDone()!=null) {
					break;
				}
				//Check error caused or not
				if(dataStructure.getError()!=null) {
					System.out.println(dataStructure.getError());
					break;
				}
				//check the order allow to trade or not
				tradeController.tradeCheckingAndBalanceUpdate(dataStructure);
				
				/*
				 * You may write your back test program below within the while loop
				 * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
				 */
				
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
				
				tradeController.placeMarketOrder(dataStructure.getSymbol(), Action.BUY, 1);
				
				System.out.println( tradeController.getProfile() );
				
				/*
				 * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
				 */
			}
		}
	}
}
