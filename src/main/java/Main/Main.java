package Main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ClientSocketControl.DataStructure;
import DataController.Future;
import Indicators.BollingerBands;
import TradeControl.TradeController;
import Util.WebVersionJobConstants;

public class Main {
		
    //Initial the ObjectMapper
    public static ObjectMapper mapper = new ObjectMapper();
    //Initial the DataStructure
    public static DataStructure dataStructure = null;
    //Trade controller
    public static TradeController tradeController = null;

    public static BollingerBands bollingerBands = null;

	public static void main(String args[]) throws Exception {

        //get the input parameters
        WebVersionJobConstants.clientID = args[0];
        WebVersionJobConstants.logger("runJobID:"+WebVersionJobConstants.runJobID);

        try{
            //initial
            WebVersionJobConstants.setupEnvironmentProperties();
            WebVersionJobConstants.setupLogger();
            WebVersionJobConstants.setWebVersionJobInstanceID();
            WebVersionJobConstants.setWebVersionJobIPaddress();
            WebVersionJobConstants.setWebVersionJobScreenTaskID();
            WebVersionJobConstants.setWebVersionJobRunJobTaskID();
            WebVersionJobConstants.setWebVersionJobCPUUsage();
            //setup Database communication
            WebVersionJobConstants.setupDBconnection();
            WebVersionJobConstants.initialIndicator();
            WebVersionJobConstants.insertWebVersionJobInformation();
            WebVersionJobConstants.logger("WebVersionJob(runJobID:"+WebVersionJobConstants.runJobID+") started up");
        } catch (Exception e) {
			WebVersionJobConstants.logger("Exception :" + e.toString());
		}

        //get task detail
        JSONObject input = WebVersionJobConstants.getMessage();
        JSONArray nodeDataArray = input.has("nodeDataArray")==true?input.getJSONArray("nodeDataArray"):null;
        JSONArray linkDataArray = input.has("linkDataArray")==true?input.getJSONArray("linkDataArray"):null;

        //Request testing
        ArrayList<String> errorMessage = new ArrayList<String>();
        errorMessage.addAll(WebVersionJobConstants.subscribedDataListValidation(nodeDataArray));
        errorMessage.addAll(WebVersionJobConstants.commonIndicatorListValidation(nodeDataArray));
        errorMessage.addAll(WebVersionJobConstants.indicatorOutputValidation(nodeDataArray));
        errorMessage.addAll(WebVersionJobConstants.tradeActionValidation(nodeDataArray));

        boolean requestValidationPass = true;
        if(errorMessage.size()>0){
            requestValidationPass = false; 
            StringBuilder testResultDetail = new StringBuilder();
            for(String s : errorMessage){ 
                testResultDetail.append(s); testResultDetail.append("\n"); 
            }
            WebVersionJobConstants.updateWebJobHistory(false, testResultDetail, "NULL", "NULL");
        }

        if(requestValidationPass==true){
            //Generate the data request JSON object
            HashMap<String, JSONObject> dataStreamingRequest = new HashMap<String, JSONObject>();
            for(JSONObject node : nodeDataArray){
                if(node.has("subscribedDataList")){
                    dataStreamingRequest.put(
                        node.getString("key"), 
                        WebVersionJobConstants.jsonParser.parse(node.getString("data"))
                    );
                }
            }

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
	}

    private static void mainLogicLevel1(ArrayList<JSONObject> dataList) throws JSONException, JsonParseException, JsonMappingException, IOException{
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
