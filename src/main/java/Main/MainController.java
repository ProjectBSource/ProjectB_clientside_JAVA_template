package Main;

import ClientSocketControl.DataStructure;
import ClientSocketControl.SocketClient;
import TradeControl.TradeController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import TradeControl.OrderActionConstants.Action;
import TradeControl.OrderActionConstants.Direction;
import TradeControl.OrderActionConstants.ExpiryDate;
import TradeControl.OrderActionConstants.StrikePrice;

public abstract class MainController {

    private static SocketClient dataStreaming = null;
    private JSONObject dataStreamingRequest = null;
    private static TradeController tradeController = null;
    private static ObjectMapper mapper = new ObjectMapper();
    private static JSONObject response = null;

    public Action action;
    public Direction direction;
    public ExpiryDate expiryDate;
    public StrikePrice strikePrice;

    public void login(String loginname, String password) throws Exception{
        try{
            dataStreaming = new SocketClient(loginname, password);
        }catch(Exception e){
            dataStreaming = null;
            throw e;
        }
    }

    public void createDataStreamingRequest(JSONObject dataStreamingRequest) throws Exception {
        if(dataStreaming==null){
            throw new Exception("Please login first");
        }else{
            dataStreaming.request(dataStreamingRequest);
        }
    }

    public void setSlippage(double slippageRangeInPercentage){
        tradeController = new TradeController();
        tradeController.setSlippage(slippageRangeInPercentage);
    }

    public void placeOrder(String id, DataStructure datastructure, Action action, int quality, boolean oneTimeTrade) throws Exception{
	    tradeController.placeOrder(id, datastructure, action, quality, oneTimeTrade); 
    }

    public void placeOrder(String id, String symbol, Action action, Direction direction, StrikePrice sp, ExpiryDate ed, int quantity, boolean oneTimeTradeCheck) {
	    tradeController.placeOrder(id, symbol, action, direction, sp, ed, quantity, oneTimeTradeCheck); 
    }

    public void placeOFFOrder(String id, DataStructure datastructure) throws Exception{
	    tradeController.placeOFFOrder(id, datastructure); 
    }

    public abstract void logicHandler(DataStructure datastructure);

    public void run() throws Exception{
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
				
                logicHandler(dataStructure);
				
				/*
				 * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
				 */
			}
		}
    }
    
}
