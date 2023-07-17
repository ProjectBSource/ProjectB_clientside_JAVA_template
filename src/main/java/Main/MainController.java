package Main;

import ClientSocketControl.DataStructure;
import ClientSocketControl.SocketClient;
import TradeControl.TradeController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

    public void createDataStreamingRequest(String activity, String market, String index, Date startdate, Date enddate, Date starttime, Date endtime, int interval) throws Exception {
        if(dataStreaming==null){
            throw new Exception("Please login first");
        }else{
            dataStreamingRequest = new JSONObject();
            dataStreamingRequest.put("activity", activity);
            dataStreamingRequest.put("market", market);
            dataStreamingRequest.put("index", index);
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(startdate);
            dataStreamingRequest.put("startdate", String.format("%04d%02d%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH)) );
            calendar.setTime(enddate);
            dataStreamingRequest.put("enddate", String.format("%04d%02d%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH)) );
            calendar.setTime(starttime);
            dataStreamingRequest.put("starttime", String.format("%02d%02d%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND)) );
            calendar.setTime(endtime);
            dataStreamingRequest.put("endtime", String.format("%02d%02d%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND)) );
            dataStreamingRequest.put("interval", interval);
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
