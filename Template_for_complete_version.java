package Main;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import ClientSocketControl.DataStructure;
import DataController.Future;
import Indicators.BollingerBands;
import TradeControl.TradeController;
import TradeControl.OrderActionConstants.Action;
import Util.WebVersionJobConstants;

public class Main {
        
    //Initial the ObjectMapper
    public static ObjectMapper mapper = new ObjectMapper();
    //Initial the JSONObject 
    public static JSONObject response = null;
    //Initial the DataStructure
    public static DataStructure dataStructure;

      public static void main(String args[]) throws Exception {
        
          //Login here
          SocketClient dataStreaming = new SocketClient(/*email*/, /*password*/);
          
          //Form JSON object message for data streaming request
          JSONObject dataStreamingRequest = new JSONObject();
          dataStreamingRequest.put("activity", "@#activity#@");
          dataStreamingRequest.put("market", "@#market#@");
          dataStreamingRequest.put("index", "@#index#@");
          dataStreamingRequest.put("startdate", "@#startdate#@");
          dataStreamingRequest.put("enddate", "@#enddate#@");
          dataStreamingRequest.put("starttime", "@#starttime#@");
          dataStreamingRequest.put("endtime", "@#endtime#@");
          dataStreamingRequest.put("interval", @#interval#@-1);
          dataStreamingRequest.put("mitigateNoiseWithinPrecentage", @#mitigateNoiseWithinPrecentage#@);
          
          //Send the request to server
          dataStreaming.request(dataStreamingRequest);
          
          /*
           * This template included a simple account and order management function
           * You may modify the function to fit your back test
           */
          TradeController tradeController = new TradeController();
          tradeController.setSlippage(@#slippage#@);
          
          //Setup the indicatories you need here
@#indicatories#@
          
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
    
@#indicatoriesUpdateLogic#@

@#baseLogicResult#@

@#logicGatewayResult#@
                             
@#actionAndTradeLogic#@
    
                    /*
                     * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     */
                }
            }
        }
}
