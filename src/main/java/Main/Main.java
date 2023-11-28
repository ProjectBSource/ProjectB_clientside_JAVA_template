package Main;

import ClientSocketControl.DataStructure;
import ClientSocketControl.SocketClient;
import Indicators.*;
import TradeControl.TradeController;
import TradeControl.OrderActionConstants.Action;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

public class Main {
		
    //Initial the ObjectMapper
    public static ObjectMapper mapper = new ObjectMapper();
    //Initial the JSONObject 
    public static JSONObject response = null;
    //Initial the DataStructure
    public static DataStructure dataStructure;

	public static void main(String args[]) throws Exception {
		
		//Login here
		SocketClient dataStreaming = new SocketClient("funganything@gmail.com", "123");
		
		//Form JSON object message for data streaming request
		JSONObject dataStreamingRequest = new JSONObject();
        dataStreamingRequest.put("activity", "TickDataStreaming");
        dataStreamingRequest.put("market", "FUTURE");
        dataStreamingRequest.put("index", "HSI");
        dataStreamingRequest.put("startdate", "20230103");
        dataStreamingRequest.put("enddate", "20230103");
        dataStreamingRequest.put("starttime", "091500");
        dataStreamingRequest.put("endtime", "091600");
        dataStreamingRequest.put("interval", 60-1);
        dataStreamingRequest.put("mitigateNoiseWithinPrecentage", 100);
		
		//Send the request to server
		dataStreaming.request(dataStreamingRequest);
		
		/*
		 * This template included a simple account and order management function
		 * You may modify the function to fit your back test
		 */
		TradeController tradeController = new TradeController();
		tradeController.setSlippage(0.0005);
		
        //Setup the indicatories you need here
        BollingerBands indicator0 = new BollingerBands(20,2);
		
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
				_____          _ _               _                   
				/ ____|        | (_)             | |                  
			   | |     ___   __| |_ _ __   __ _  | |__   ___ _ __ ___ 
			   | |    / _ \ / _` | | '_ \ / _` | | '_ \ / _ \ '__/ _ \
			   | |___| (_) | (_| | | | | | (_| | | | | |  __/ | |  __/
				\_____\___/ \__,_|_|_| |_|\__, | |_| |_|\___|_|  \___|
										   __/ |                      
										  |___/                       							
				/*
				 * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
				 */
			}
		}
        
        System.out.println(tradeController.getOrderHistoryInJSON());
	}
}
