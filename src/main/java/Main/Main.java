package Main;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import ClientSocketControl.DataStructure;
import DataController.Future;
import Indicators.*;
import TradeControl.TradeController;
import TradeControl.OrderActionConstants.Action;
import TradeControl.OrderActionConstants.Direction;
import TradeControl.OrderActionConstants.StrikePrice;
import Util.WebVersionJobConstants;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
		
    //Initial the ObjectMapper
    public static ObjectMapper mapper = new ObjectMapper();
    //Initial the DataStructure
    public static DataStructure dataStructure = null;
    //Trade controller
    public static TradeController tradeController = null;

    //Variables for update task real time information
    private static Date lastUpdateTime = null;


    //Manual operation variables
    public static boolean restartAndGenerateData = true;
    public static ArrayList<String> restartAndGenerateDataArrayList = new ArrayList<String>();


    /* Setup the indicatories you need here */
    //############################################################################################################################
    @#indicatories#@
    //############################################################################################################################


	public static void main(String args[]) throws Exception {

        //get the input parameters
        WebVersionJobConstants.runJobID = args[0];
        WebVersionJobConstants.logger("runJobID:"+WebVersionJobConstants.runJobID);

        //Manual operation variables
        if(args.length>1 && args[1].equals("restartAndGenerateData")){
            restartAndGenerateData = true;
        }

        try{
            //initial
            WebVersionJobConstants.setupEnvironmentProperties();
            WebVersionJobConstants.setupLogger();
            WebVersionJobConstants.setWebVersionJobInstanceID();
            WebVersionJobConstants.setWebVersionJobIPaddress();
            WebVersionJobConstants.setWebVersionJobScreenTaskID();
            WebVersionJobConstants.setWebVersionJobCPUUsage();
            //setup Database communication
            WebVersionJobConstants.setupDBconnection();
            WebVersionJobConstants.initialIndicator();
	        if(WebVersionJobConstants.environment.equals("prd")){
                WebVersionJobConstants.insertWebVersionJobInformation();
            }
            WebVersionJobConstants.logger("WebVersionJob(runJobID:"+WebVersionJobConstants.runJobID+") started up");

            //get task detail
	    WebVersionJobConstants.logger("Download request message");
            JSONObject input = WebVersionJobConstants.getRequestMessage();
            JSONArray nodeDataArray = input.has("nodeDataArray")==true?input.getJSONArray("nodeDataArray"):null;
            JSONArray linkDataArray = input.has("linkDataArray")==true?input.getJSONArray("linkDataArray"):null;

            //Request testing
	    WebVersionJobConstants.logger("Request testing");
            ArrayList<String> errorMessage = new ArrayList<String>();
            WebVersionJobConstants.updateWebJobHistory(false, null, "NULL", "NULL", "Program validating");
            errorMessage.addAll(WebVersionJobConstants.subscribedDataListValidation(nodeDataArray));
            errorMessage.addAll(WebVersionJobConstants.commonIndicatorListValidation(nodeDataArray));
            errorMessage.addAll(WebVersionJobConstants.indicatorOutputValidation(nodeDataArray));
            errorMessage.addAll(WebVersionJobConstants.tradeActionValidation(nodeDataArray));

            boolean requestValidationPass = true;
            if(errorMessage.size()>0){
		WebVersionJobConstants.logger("Request test result fail");
                requestValidationPass = false; 
                StringBuilder testResultDetail = new StringBuilder();
                for(String s : errorMessage){ 
                    testResultDetail.append(s); testResultDetail.append("\n"); 
                }
                WebVersionJobConstants.updateWebJobHistory(false, testResultDetail.toString(), "NULL", "NULL", "Program encounter error, please read the Detail");
            }

            if(requestValidationPass==true){
		WebVersionJobConstants.logger("Request test result pass");
                //Generate the data request JSON object
                JSONObject dataStreamingRequest = new JSONObject();
                dataStreamingRequest.put("activity", "@#activity#@");
                dataStreamingRequest.put("market", "@#market#@");
                dataStreamingRequest.put("index", "@#index#@");
                dataStreamingRequest.put("startdate", "@#startdate#@");
                dataStreamingRequest.put("enddate", "@#enddate#@");
                dataStreamingRequest.put("starttime", "@#starttime#@");
                dataStreamingRequest.put("endtime", "@#endtime#@");
                dataStreamingRequest.put("interval", @#interval#@);
                dataStreamingRequest.put("mitigateNoiseWithinPrecentage", @#mitigateNoiseWithinPrecentage#@);
                WebVersionJobConstants.logger("dataStreamingRequest :" + dataStreamingRequest.toString());
                
                for(Object objectNode : nodeDataArray){
                    JSONObject node = (JSONObject) objectNode;
                    if(node.has("subscribedDataList")){
                        if(node.has("data")){
                            JSONObject data = new JSONObject(node.getString("data"));
                            if(data.has("slippagePrecentage")==true) { 
                                tradeController = new TradeController();
                                tradeController.setSlippage( Double.parseDouble(data.getString("slippagePrecentage")) );
                                WebVersionJobConstants.logger("slippagePrecentage :" + data.getString("slippagePrecentage") );
                                break;
                            }
                        }
                    }
                }

                boolean onlyIntervalData = false;
                if(dataStreamingRequest.has("activity")){ 
                    if (dataStreamingRequest.getString("activity").equalsIgnoreCase(Constants.intervaldataStreamingRequest)) {
                        onlyIntervalData = true;
                        WebVersionJobConstants.logger("onlyIntervalData : true");
                    }
                }

                if(dataStreamingRequest.has("market")){ 
                    if (dataStreamingRequest.getString("market").equalsIgnoreCase(Constants.dataStreamingFutureRequest)) {
                        WebVersionJobConstants.logger("market : FUTURE");
                        Future future = new Future(dataStreamingRequest, onlyIntervalData);
                        Thread thread = new Thread(future);
                        thread.start();
                        WebVersionJobConstants.updateWebJobHistory(false, null, "NULL", "NULL", "Program running");
                        while(future.processDone == false) {
                            System.out.print("");
			                try{
                                if(future.dataForReading.size()>0){
                                    mainLogicLevel1(future.dataForReading);
                                    //delete processed data
                                    future.dataForReading = new ArrayList<JSONObject>();
                                }
                            }catch(Exception e){
                                WebVersionJobConstants.logger("mainLogicLevel1 error :" + e);
                                break;
                            }
                        }
                        if(future.processDone == true){
                            WebVersionJobConstants.logger("mainLogicLevel1 completed");
                            JSONObject testResultDetailInJSON = new JSONObject();
			                WebVersionJobConstants.logger("Length in OrderHistoryInJSON:" + tradeController.getOrderHistoryInJSON().toString().length());
                            testResultDetailInJSON.put("OrderHistory", tradeController.getOrderHistoryInJSON().toString().substring(1,tradeController.getOrderHistoryInJSON().toString().length()-1));
			                WebVersionJobConstants.logger("Length in ProfileInJSON:" + tradeController.getProfileInJSON().toString().length());
                            testResultDetailInJSON.put("Profile", tradeController.getProfileInJSON().toString());
                            testResultDetailInJSON.put("runJobID", WebVersionJobConstants.runJobID);
                            WebVersionJobConstants.postRequest("https://projectb.click/ProjectB/WebVersion/TestResult/save.php?password=AIDkrepkclkdsaf123JK", testResultDetailInJSON.toString());                            
                            WebVersionJobConstants.updateWebJobHistory(true, "", "(TIMESTAMPDIFF(SECOND, StartDateTime, EndDateTime))", "(TIMESTAMPDIFF(SECOND, StartDateTime, EndDateTime)*0.00003)", "Program running completed");
                        }
                    }
                }
            }
        } catch (Exception e) {
			WebVersionJobConstants.logger("Exception in Main.java :" + e.toString());
		}
    }

    private static void mainLogicLevel1(ArrayList<JSONObject> dataList) throws Exception{
        for(JSONObject data : dataList) {
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
                    WebVersionJobConstants.logger(dataStructure.getError());
                    break;
                }
                //check the order allow to trade or not
                if(tradeController!=null){
                    tradeController.tradeCheckingAndBalanceUpdate(dataStructure);
                }
                
                /*
                * Manual operation
                * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                */
                if(restartAndGenerateData==true){
                    restartAndGenerateDataArrayList.add(
                        String.format("%s@#manualOpertionOutputControl1#@",data.toString()@#manualOpertionOutputControl2#@)
                    );
                }
                /*
                * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                */

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

        /*
        * Manual operation
        * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        */
        if(restartAndGenerateData==true){ manualOperation_generateRestartAndGenerateData(); }
        /*
        * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        */

        //update task real time information
        if(WebVersionJobConstants.environment.equals("prd")){
            if(lastUpdateTime==null){
                lastUpdateTime = new Date();
            }
            else{
                long diff = (new Date()).getTime() - (lastUpdateTime).getTime();
                long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
                if(seconds >=10){
                    WebVersionJobConstants.updateWebVersionJobInformation();
                    lastUpdateTime = new Date();
                }
            }
        }
    }

    private static void manualOperation_generateRestartAndGenerateData(){
        try{
            FileWriter fw = new FileWriter("/home/ec2-user/dataSource/webVersion/Jobs/"+WebVersionJobConstants.runJobID+"/"+WebVersionJobConstants.runJobID+"_restartAndGenerateData.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            for(String s : restartAndGenerateDataArrayList){
                bw.write( s );
                bw.write("\n");
            }
            bw.close();
            fw.close();
            restartAndGenerateDataArrayList = new ArrayList<String>();
        }catch(Exception e){}
    }
}
