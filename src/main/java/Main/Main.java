package Main;

import ClientSocketControl.DataStructure;
import ClientSocketControl.SocketClient;
import Indicators.BollingerBands;
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


				System.out.println( 
					String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
						dataStructure.getType(),
						dataStructure.getDatetime(),
						dataStructure.getIndex(),
						dataStructure.getVolumn(),
						dataStructure.getOpen(),
						dataStructure.getHigh(),
						dataStructure.getLow(),
						dataStructure.getClose(),
						dataStructure.getTotal_volumn(),
                        indicator0.getUpperBand(),
                        indicator0.getMiddleBand(),
                        indicator0.getLowerBand()
					) 
				);

                if(dataStructure.getType().equals("tick")){ indicator0.update(dataStructure); }


                boolean baseLogicResult0 = ( indicator0.getPrice()>0 && indicator0.getUpperBand() > 0 && indicator0.getPrice() > indicator0.getUpperBand()  );
                boolean baseLogicResult1 = ( indicator0.getPrice()>0 && indicator0.getLowerBand() > 0 && indicator0.getPrice() < indicator0.getLowerBand()  );
                boolean baseLogicResult2 = ( indicator0.getPrice()>0 && indicator0.getMiddleBand() > 0 && indicator0.getPrice() == indicator0.getMiddleBand()  );


                
                if(dataStructure.getType().equals("tick")){ 
                    if(baseLogicResult0==true){ 
                        tradeController.placeOrder("#1", dataStructure, Action.BUY, 1, false); 
                    }
                    if(baseLogicResult1==true){ 
                        tradeController.placeOrder("#2", dataStructure, Action.SELL, 1, false); 
                    }
                    if(baseLogicResult2==true){ 
                        tradeController.placeOFFOrder("#1", dataStructure); 
                    }
                    if(baseLogicResult2==true){ 
                        tradeController.placeOFFOrder("#2", dataStructure); 
                    }
                }
                                
				/*
				 * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
				 */
			}
		}
        
        System.out.println(tradeController.getOrderHistoryInJSON());
	}
}
