package Main;

import ClientSocketControl.DataStructure;
import ClientSocketControl.SocketClient;
import TradeControl.TradeController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

public abstract class MainController {

    private static SocketClient dataStreaming = null;
    private JSONObject dataStreamingRequest = null;
    private static TradeController tradeController = null;
    private static ObjectMapper mapper = new ObjectMapper();
    private static JSONObject response = null;

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

    public void projectBTradeController(double slippageRangeInPercentage){
        tradeController = new TradeController();
        tradeController.setSlippage(slippageRangeInPercentage);
    }

    public abstract void logicHandler(DataStructure datastructure);

    public void run() throws JsonMappingException, JsonProcessingException{
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
