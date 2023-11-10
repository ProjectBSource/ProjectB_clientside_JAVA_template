package DataController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONObject;




public class Future implements Runnable {
	private String runJobID;
	private FileReader fr;
	private BufferedReader br;
	private String linedata;
	private String[] sbArray;
    private static Constants constants = new Constants();
	public boolean processDone = false;
	private JSONObject previousDataDetail = null;
	public ArrayList<JSONObject> data = new ArrayList<JSONObject>();
	private String symbol = null;
	private Date startdate = null;
	private Date enddate = null;
	private Date startdatetime = null;
	private Date starttime = null;
	private Date endtime = null;
	private Date enddatetime = null;
	private Date data_date = null; private Date prev_data_date = null;
	private Date data_time = null; private Date prev_data_time = null;
	private Date data_datetime = null; private Date prev_data_datetime = null;
	private int data_price = -1;
	private int data_volumn = -1;
	private int interval_in_seconds = 0;
	private double mitigateNoiseWithPrecentage = -1;
	private boolean onlyIntervalData = true;
	private boolean dataSubscriptedOrNot = true;
	private Date interval_starttime = null;
	private Date interval_endtime = null;
	private String data_date_within_interval = null;
	private String data_time_within_interval = null;
	private String data_datetime_within_interval = null;
	private String data_index_within_interval = null;
	private String data_open_within_interval = null;
	private String data_high_within_interval = null;
	private String data_low_within_interval = null; 
	private String data_close_within_interval = null;
	private String data_sumupvolumn_within_interval = null;
	private JSONObject dataDetail = null;
	private boolean without_time_reset_interval_startendtime = false;

	//Manual operation variables
    public static boolean restartAndGenerateData = true;
    public static ArrayList<String> restartAndGenerateDataArrayList = null;
	
	public Future(JSONObject input, boolean onlyIntervalData) {
		try {
			this.runJobID = runJobID;
			this.symbol = input.getString("index");
			this.startdate = Constants.df_yyyyMMdd.parse(input.getString("startdate"));
			this.enddate = Constants.df_yyyyMMdd.parse(input.getString("enddate"));
			this.starttime = Constants.df_kkmmss.parse(input.getString("starttime"));
			this.endtime = Constants.df_kkmmss.parse(input.getString("endtime"));
			this.startdatetime = Constants.df_yyyyMMddkkmmss.parse(input.getString("startdate") + input.getString("starttime"));
			this.enddatetime = Constants.df_yyyyMMddkkmmss.parse(input.getString("enddate") + input.getString("endtime"));
			if(input.has("interval")==true) { 
				this.interval_in_seconds = input.getInt("interval");
			}
			if(input.has("mitigateNoiseWithPrecentage")==true) { 
				this.mitigateNoiseWithPrecentage = input.getInt("mitigateNoiseWithPrecentage");
			}
			this.onlyIntervalData = onlyIntervalData;
			this.dataSubscriptedOrNot = dataSubscriptedOrNot;
		} catch (Exception e) {
			Constants.logger("System error ["+this.getClass().getName()+":"+e.getMessage()+"], please contact admin");
			dataDetail = new JSONObject();
			dataDetail.put("error", "System error ["+this.getClass().getName()+":"+e.getMessage()+"], please contact admin");
			data.add(dataDetail);
			processDone = true;
		}
	}
	
 @Override
	public void run() {
		try {
			if(dataSubscriptedOrNot==false) {
				this.startdate = Constants.randomStartEndDateRangeForFreeTrail(startdate, enddate);
				this.enddate = Constants.addDates(this.startdate, 1);
			}
			
			ArrayList<File> fileslist = Constants.requiredFileLiet(symbol, "future", startdate, enddate);
			if(fileslist!=null) {
				for(File f : fileslist) {
					fr = new FileReader(f.getAbsolutePath());
					br = new BufferedReader(fr);
					while(true) {
						int firstchar = br.read();
						if(firstchar==-1) { 
							break; 
						}
						else {
							//wait for data clean
							if(data.size()>=30000) {
								/*
								* Manual operation
								* >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
								*/
								if(restartAndGenerateData==true){
									for(JSONObject d : data){
										restartAndGenerateDataArrayList.add( d.toString() );
									}
									manualOperation_generateRestartAndGenerateData();
								}
								/*
								* <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
								*/
								Thread.sleep(3500);
							}
							
							//Read data
							linedata = (((char)firstchar)+br.readLine());
							sbArray = linedata.toString().split(",");
							data_date = Constants.df_yyyyMMdd.parse(sbArray[0]);
							data_time = Constants.df_kkmmss.parse(sbArray[1]);
							data_datetime = Constants.df_yyyyMMddkkmmss.parse(sbArray[0]+sbArray[1]);
							data_price = Integer.parseInt(sbArray[2]);
							data_volumn = Integer.parseInt(sbArray[3]);
							
							//check data read finished or not
							if(Constants.outOfEndDateOrNot(enddatetime, data_datetime)==true) {
								break;
							}
							
							//select data
							if(Constants.withinDateOrNot(startdatetime, enddatetime, data_datetime)==true) {
								//check within time or not
								if(Constants.withinDateOrNot(starttime, endtime, data_time)==false) {
									without_time_reset_interval_startendtime = true;
									continue; 
								}
								
								//initial the interval time range
								if(interval_starttime==null) {
									interval_starttime = (data_datetime.after(startdatetime)?data_datetime:startdatetime);
									interval_endtime = Constants.addSeconds(interval_starttime, interval_in_seconds);
								}
								if(without_time_reset_interval_startendtime==true) {
									interval_starttime = data_datetime;
									interval_endtime = Constants.addSeconds(interval_starttime, interval_in_seconds);
									without_time_reset_interval_startendtime = false;
								}
								
								//reset the interval time range
								if(Constants.withinDateOrNot(interval_starttime, interval_endtime, data_datetime)==false) {
									//sum up data
									sumData();
								}
								
								//select data within the interval
								if(Constants.withinDateOrNot(interval_starttime, interval_endtime, data_datetime)==true) {
									//setup date
									if(data_date_within_interval==null) { 
										data_date_within_interval = sbArray[0]; 
									}
									//setup time
									if(data_time_within_interval==null) { 
										data_time_within_interval = sbArray[1]; 
									}
									//setup datetime
									if(data_datetime_within_interval==null) { 
										data_datetime_within_interval = sbArray[0]+sbArray[1]; 
									}
									//setup index
									if(data_index_within_interval==null) { 
										data_index_within_interval = sbArray[2]; 
									}
									//setup open
									if(data_open_within_interval==null) { 
										data_open_within_interval = sbArray[2]; 
									}
									//setup high
									if(data_high_within_interval==null || Integer.parseInt(data_high_within_interval)<Integer.parseInt(sbArray[2]) ) {
										data_high_within_interval = sbArray[2]; 
									}
									//setup low
									if(data_low_within_interval==null || Integer.parseInt(data_low_within_interval)>Integer.parseInt(sbArray[2]) ) {
										data_low_within_interval = sbArray[2]; 
									}
									//setup close
									if(true) { 
										data_close_within_interval = sbArray[2]; 
									}
									//setup sum up volumn
									if(true) { 
										data_sumupvolumn_within_interval = (data_sumupvolumn_within_interval==null?0:Integer.parseInt(data_sumupvolumn_within_interval)) + Integer.parseInt(sbArray[3]) + ""; 
									}
									if(onlyIntervalData==false) {
										Double newIndex = Double.parseDouble(sbArray[2]);
										
										//skip if the index defined as noise
										boolean noise = false;
										if(mitigateNoiseWithPrecentage>-1) {
											if(previousDataDetail!=null) {
												Double prevIndex = previousDataDetail.getDouble("index");
												if( Math.abs((newIndex - prevIndex) / prevIndex * 100) < mitigateNoiseWithPrecentage) 
													noise = true;
											}
										}
										
										if(noise == false) {
											//setup other information
											dataDetail = new JSONObject();
											dataDetail.put("dataSourceID", runJobID);
											dataDetail.put("type", "tick");
											dataDetail.put("symbol", symbol);
											dataDetail.put("market", "future");
											dataDetail.put("date", sbArray[0]);
											dataDetail.put("time", sbArray[1]);
											dataDetail.put("datetime", sbArray[0]+sbArray[1]);
											dataDetail.put("index", newIndex);
											dataDetail.put("open", Double.parseDouble(data_open_within_interval));
											dataDetail.put("high", Double.parseDouble(data_high_within_interval));
											dataDetail.put("low", Double.parseDouble(data_low_within_interval));
											dataDetail.put("volumn", Integer.parseInt(sbArray[3]));
											dataDetail.put("total_volumn", Integer.parseInt(data_sumupvolumn_within_interval));
											
											//insert into data
											data.add(dataDetail);
											previousDataDetail = dataDetail;
										}
									}
								}
							}
							
							//Set previous data
							prev_data_date = data_date;
							prev_data_time = data_time;
							prev_data_datetime = data_datetime;
						}
					}
					
					//sum up data
					sumData();
				}
			}
		}catch(Exception e) {
			Constants.logger("System error ["+this.getClass().getName()+":"+e.getMessage()+"], please contact admin");
			dataDetail = new JSONObject();
			dataDetail.put("error", "System error ["+this.getClass().getName()+":"+e.getMessage()+"], please contact admin");
			data.add(dataDetail);
			processDone = true;
		}
		processDone = true;
	}
	
	private void sumData() {
		if(data_date_within_interval!=null) {
			//sum up data
			dataDetail = new JSONObject();
			dataDetail.put("dataSourceID", runJobID);
			dataDetail.put("type", "interval");
			dataDetail.put("symbol", symbol);
			dataDetail.put("market", "future");
			dataDetail.put("date", data_date_within_interval);
			dataDetail.put("time", data_time_within_interval);
			dataDetail.put("datetime", data_datetime_within_interval);
			dataDetail.put("index", Double.parseDouble(data_index_within_interval));
			dataDetail.put("open", Double.parseDouble(data_open_within_interval));
			dataDetail.put("high", Double.parseDouble(data_high_within_interval));
			dataDetail.put("low", Double.parseDouble(data_low_within_interval));
			dataDetail.put("close", Double.parseDouble(data_close_within_interval));
			dataDetail.put("total_volumn", Integer.parseInt(data_sumupvolumn_within_interval));
			//insert into data
			data.add(dataDetail);
		}
		//rest
		data_date_within_interval = null;
		data_time_within_interval = null;
		data_datetime_within_interval = null;
		data_index_within_interval = null;
		data_open_within_interval = null;
		data_high_within_interval = null;
		data_low_within_interval = null;
		data_close_within_interval = null;
		data_sumupvolumn_within_interval = null;
		interval_starttime = Constants.addSeconds(interval_endtime, 1);
		interval_endtime = Constants.addSeconds(interval_starttime, interval_in_seconds);
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
