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
    //Initial the DataStructure
    public static DataStructure dataStructure = null;
    //Trade controller
    public static TradeController tradeController = null;

    /* Setup the indicatories you need here */
    //############################################################################################################################
    @#indicatories#@
    //############################################################################################################################


	public static void main(String args[]) throws Exception {

        //get the input parameters
        WebVersionJobConstants.runJobID = args[0];
        WebVersionJobConstants.logger("runJobID:"+WebVersionJobConstants.runJobID);

        try{
            //initial
            WebVersionJobConstants.setupEnvironmentProperties();
            WebVersionJobConstants.setupLogger();
            WebVersionJobConstants.setWebVersionJobInstanceID();
            WebVersionJobConstants.setWebVersionJobIPaddress();
            WebVersionJobConstants.setWebVersionJobScreenTaskID();
            WebVersionJobConstants.setWebVersionJobRunJobTaskID();
            //setup Database communication
            WebVersionJobConstants.setupDBconnection();
            WebVersionJobConstants.initialIndicator();
            WebVersionJobConstants.insertWebVersionJobInformation();
            WebVersionJobConstants.logger("WebVersionJob(runJobID:"+WebVersionJobConstants.runJobID+") started up");
        } catch (Exception e) {
			WebVersionJobConstants.logger("Exception :" + e.toString());
		}

        //get task detail
        JSONObject input = WebVersionJobConstants.getRequestMessage();
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
            

            if(dataStreamingRequest.has("slippagePrecentage")){ 
                tradeController = new TradeController();
                tradeController.setSlippage( input.getDouble("slippagePrecentage") );
            }

            boolean onlyIntervalData = false;
            if (input.getString("activity").equalsIgnoreCase(Constants.intervaldataStreamingRequest)) {
                onlyIntervalData = true;
            }

            if (dataStreamingRequest.getString("market").equalsIgnoreCase(Constants.dataStreamingFutureRequest)) {
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

    private static void mainLogicLevel1(ArrayList<JSONObject> dataList) throws Exception{
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

                    @#indicatoriesUpdateLogic#@

                    @#baseLogicResult#@

                    @#logicGatewayResult#@

                    @#actionAndTradeLogic#@
                    
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
}
