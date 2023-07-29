package Main;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ClientSocketControl.DataStructure;
import DataController.Future;
import Indicators.BollingerBands;
import TradeControl.TradeController;

public class Main {
		
    //Initial the ObjectMapper
    public static ObjectMapper mapper = new ObjectMapper();
    //Initial the DataStructure
    public static DataStructure dataStructure = null;
    //Trade controller
    public static TradeController tradeController = null;

    public static BollingerBands bollingerBands = null;

	public static void main(String args[]) throws Exception {
		
        //Form JSON object message for data streaming request
		JSONObject input = new JSONObject(args[0]);
		/*
		 * This template included a simple account and order management function
		 * You may modify the function to fit your back test
		 */

        if(input.has("dataChangeLimitInPrecentage")){ 
            tradeController = new TradeController();
            tradeController.setSlippage( input.getDouble("dataChangeLimitInPrecentage") );
        }

        if(input.has("Indicator")){
            for(Object ind : input.getJSONArray("Indicator")){
                JSONObject indJSON = (JSONObject)ind;
                if(indJSON.getString("name").equals("BollingerBands")){
                    bollingerBands = new BollingerBands(indJSON.getInt("period"), indJSON.getDouble("multiplier"));
                }
            }
        }

        boolean onlyIntervalData = false;
        if (input.getString("activity").equalsIgnoreCase(Constants.intervaldataStreamingRequest)) {
            onlyIntervalData = true;
        }
        if (input.getString("market").equalsIgnoreCase(Constants.dataStreamingFutureRequest)) {
            Future future = new Future(input, onlyIntervalData);
            Thread thread = new Thread(future);
            thread.start();
            while(future.processDone == false || future.data.size()>0) {
                System.out.print("");
                mainLogicLevel1(future.data);
            }
        }
	}

    private static void mainLogicLevel1(ArrayList<JSONObject> dataList) throws JsonProcessingException, JSONException{
		if(dataList.size()>0) {
            int tempDataListSize = dataList.size();
            for(int i=0; i<tempDataListSize; i++) {
			    JSONObject data = dataList.get(i);
                //get the response
                if(data!=null && !data.isEmpty()) {
                    System.out.flush();
                    //Convert response JSON message to Java class object
                    DataStructure dataStructure = mapper.readValue(data.toString(), DataStructure.class);
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
                    if(tradeController!=null){
                        tradeController.tradeCheckingAndBalanceUpdate(dataStructure);
                    }
                    
                    /*
                    * You may write your back test program below within the while loop
                    * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                    */

                    if(bollingerBands!=null){ bollingerBands.addPrice(dataStructure.getIndex()); }

                    mainLogicLevel2(dataStructure);
                    
                    /*
                    * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                    */
                }
			}
            //delete processed data
			for(int i=0; i<tempDataListSize; i++) {
				dataList.remove(0); 
			}
		}
    }

    private static void mainLogicLevel2(DataStructure dataStructure){
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
                bollingerBands.getUpperBand(),
                bollingerBands.getMiddleBand(),
                bollingerBands.getLowerBand()
            ) 
        );
        


        /*
        if(tradeController.getProfile().holding.size()==0){
            tradeController.placeOrder(dataStructure.getSymbol(), Action.BUY, 1);
        }
        */
        //System.out.println( tradeController.getProfile() );
    }

}
