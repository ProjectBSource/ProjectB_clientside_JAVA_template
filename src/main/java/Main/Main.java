package Main;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import ClientSocketControl.DataStructure;
import DataController.Future;
import Indicators.BollingerBands;
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
    public static boolean restartAndGenerateData = false;
    public static ArrayList<String> restartAndGenerateDataArrayList = null;

    /* Setup the indicatories you need here */
    //############################################################################################################################
              private static BollingerBands indicator0 = new BollingerBands(20, 2);

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
            WebVersionJobConstants.setWebVersionJobCPUUsage();
            //setup Database communication
            WebVersionJobConstants.setupDBconnection();
            WebVersionJobConstants.initialIndicator();
            //manual restart for checking / testing / bugfix...
            if(args.length > 1){
                if(args[1].equals("restartAndGenerateData")){
                        restartAndGenerateData = true;
                        restartAndGenerateDataArrayList = new ArrayList<String>();
                        WebVersionJobConstants.deleteWebVersionJobInformation();
                }
            }
            if(WebVersionJobConstants.environment.equals("prd")){
                WebVersionJobConstants.insertWebVersionJobInformation();
            }
            WebVersionJobConstants.logger("WebVersionJob(runJobID:"+WebVersionJobConstants.runJobID+") started up");

            //get task detail
            JSONObject input = WebVersionJobConstants.getRequestMessage();
            JSONArray nodeDataArray = input.has("nodeDataArray")==true?input.getJSONArray("nodeDataArray"):null;
            JSONArray linkDataArray = input.has("linkDataArray")==true?input.getJSONArray("linkDataArray"):null;

            //Request testing
            ArrayList<String> errorMessage = new ArrayList<String>();
            WebVersionJobConstants.updateWebJobHistory(false, null, "NULL", "NULL", "Program validating");
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
                WebVersionJobConstants.updateWebJobHistory(false, testResultDetail.toString(), "NULL", "NULL", "Program encounter error, please read the Detail");
            }

            if(requestValidationPass==true){
                //Generate the data request JSON object
                JSONObject dataStreamingRequest = new JSONObject();
                dataStreamingRequest.put("activity", "TickDataStreaming");
                dataStreamingRequest.put("market", "FUTURE");
                dataStreamingRequest.put("index", "HSI");
                dataStreamingRequest.put("startdate", "20230101");
                dataStreamingRequest.put("enddate", "20230107");
                dataStreamingRequest.put("starttime", "000000");
                dataStreamingRequest.put("endtime", "235900");
                dataStreamingRequest.put("interval", 60-1);
                dataStreamingRequest.put("mitigateNoiseWithinPrecentage", 200);
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
                        while(future.processDone == false || future.data.size()>0) {
                            System.out.print("");
                            try{
                                mainLogicLevel1(future.data);
                            }catch(Exception e){
                                WebVersionJobConstants.logger("mainLogicLevel1 error :" + e);
                                break;
                            }
                        }
                        if(future.processDone == true && future.data.size()==0){
                            WebVersionJobConstants.logger("mainLogicLevel1 completed");
                            generateOrderHistoryInJSON();
                            generateProfileInJSON();
                            /*
                            * Manual operation
                            * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                            */
                            if(restartAndGenerateData==true){ manualOperation_generateRestartAndGenerateData(); }
                            /*
                            * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                            */
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
                if(dataList.size()>0) {
            int tempDataListSize = dataList.size();
            for(int i=0; i<tempDataListSize; i++) {
                            JSONObject data = dataList.get(i);
                //get the response
                if(data!=null && !data.isEmpty()) {
                    //System.out.flush();
                    //Convert response JSON message to Java class object
                    DataStructure dataStructure = mapper.readValue(data.toString(), DataStructure.class);
                    //Check response finished or not
                    if(dataStructure.getDone()!=null) {
                        if(restartAndGenerateData==true){
                            restartAndGenerateDataArrayList.add("dataStructure.getDone():"+dataStructure.getDone()+", i="+i+", dataList.size()="+tempDataListSize);
                        }
                        break;
                    }
                    //Check error caused or not
                    if(dataStructure.getError()!=null) {
                        WebVersionJobConstants.logger(dataStructure.getError());
                        if(restartAndGenerateData==true){
                            restartAndGenerateDataArrayList.add("dataStructure.getDone():"+dataStructure.getDone()+", i="+i+", dataList.size()="+tempDataListSize);
                        }
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

                                         if(dataStructure.getType().equals("interval")){ indicator0.addPrice(dataStructure.getIndex()); }


                                         boolean baseLogicResult0 = (indicator0.getPrice() > 0 && indicator0.getUpperBand()  > 0 && indicator0.getPrice() > indicator0.getUpperBand()  );
                     boolean baseLogicResult1 = (indicator0.getPrice() > 0 && indicator0.getLowerBand()  > 0 && indicator0.getPrice() < indicator0.getLowerBand()  );
                     boolean baseLogicResult2 = (indicator0.getPrice() > 0 && indicator0.getMiddleBand()  > 0 && indicator0.getPrice() <= indicator0.getMiddleBand()  );
                     boolean baseLogicResult3 = (indicator0.getPrice() > 0 && indicator0.getMiddleBand()  > 0 && indicator0.getPrice() >= indicator0.getMiddleBand()  );


                    

                    if(dataStructure.getType().equals("tick")){                      if(baseLogicResult0==true){ tradeController.placeOrder("#8953", dataStructure, Action.BUY, 1, false); }
                     if(baseLogicResult1==true){ tradeController.placeOrder("#7727", dataStructure, Action.SELL, 1, false); }
                     if(baseLogicResult2==true){ tradeController.placeOFFOrder("#8953", dataStructure); }
                     if(baseLogicResult3==true){ tradeController.placeOFFOrder("#7727", dataStructure); }
 }

                    
                    /*
                    * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                    */

                    /*
                    * Manual operation
                    * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                    */
                    if(restartAndGenerateData==true){
                        restartAndGenerateDataArrayList.add(
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
                        if(restartAndGenerateDataArrayList.size()%10000==0){
                                manualOperation_generateRestartAndGenerateData();
                        }
                    }
                    /*
                    * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                    */
                            
                }
                        }

            //delete processed data
                        for(int i=0; i<tempDataListSize; i++) {
                                dataList.remove(0); 
                        }

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
    }

    private static void generateOrderHistoryInJSON(){
        try{
                FileWriter fw = new FileWriter("/home/ec2-user/dataSource/webVersion/Jobs/"+WebVersionJobConstants.runJobID+"/OrderHistoryInJSON.json");
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(tradeController.getOrderHistoryInJSON().toString());
                bw.close();
                fw.close();
        }catch(Exception e){}
    }

    private static void generateProfileInJSON(){
        try{
                FileWriter fw = new FileWriter("/home/ec2-user/dataSource/webVersion/Jobs/"+WebVersionJobConstants.runJobID+"/ProfileInJSON.json");
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(tradeController.getProfileInJSON().toString());
                bw.close();
                fw.close();
        }catch(Exception e){}
    }

    private static void manualOperation_generateRestartAndGenerateData(){
        try{
                FileWriter fw = new FileWriter("/home/ec2-user/dataSource/webVersion/Jobs/"+WebVersionJobConstants.runJobID+"/restartAndGenerateData.txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                for(int i=0; i<10000; i++){
                    bw.write( restartAndGenerateDataArrayList.get(i) );
                    bw.write("\n");
                }
                bw.close();
                fw.close();
                for(int i=0; i<10000; i++){
                    restartAndGenerateDataArrayList.remove(0);
                }
                restartAndGenerateDataArrayList = new ArrayList<String>();
        }catch(Exception e){}
    }
}
