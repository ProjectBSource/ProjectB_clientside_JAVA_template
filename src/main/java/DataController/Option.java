package DataController;

import DataController.IntervalData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class Option implements Runnable {
	private String runJobID;
	private String symbol = null;
	private Date startdate = null;
	private Date enddate = null;
	private Date starttime = null;
	private Date endtime = null;
	private Date startdatetime = null;
	private Date enddatetime = null;
	private int interval_in_seconds = 0;
	private double mitigateNoiseWithPrecentage = -1;
	private boolean onlyIntervalData = true;
	private boolean dataSubscriptedOrNot = true;

	private JSONObject dataDetail = null;

	private String linedata;
	private String[] sbArray;
	private Date data_date = null; private Date prev_data_date = null;
	private Date data_time = null; private Date prev_data_time = null;
	private Date data_datetime = null; private Date prev_data_datetime = null;

	private Date interval_starttime = null;
	private Date interval_endtime = null;
	private boolean without_time_reset_interval_startendtime = false;

	private FileReader fr;
	private BufferedReader br;
    private static Constants constants = new Constants();
	public boolean processDone = false;
	public HashMap<String, JSONObject> previousDataDetail = new HashMap<String, JSONObject>();
	public ArrayList<JSONObject> data = new ArrayList<JSONObject>();
	public HashMap<String, IntervalData> intervalData_of_diff_contract = new HashMap<String, IntervalData>();
	public ArrayList<JSONObject> dataForReading = new ArrayList<JSONObject>();
	
	public Option(JSONObject input, boolean onlyIntervalData) {
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
			
			ArrayList<File> fileslist = Constants.requiredFileList(symbol, startdate, enddate);
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
							if(data.size()>=30000 && dataForReading.size()==0) {
								dataForReading = new ArrayList<JSONObject>(data);
								data = new ArrayList<JSONObject>();
								Thread.sleep(3000);
							}
							
							//Read data
							linedata = (((char)firstchar)+br.readLine());
							sbArray = linedata.toString().split(",");
							data_date = Constants.df_yyyyMMdd.parse(sbArray[0]);
							data_time = Constants.df_kkmmss.parse(sbArray[1]);
							data_datetime = Constants.df_yyyyMMddkkmmss.parse(sbArray[0]+sbArray[1]);
							
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
									IntervalData tempIntervalData = null;
									if(sbArray.length>6){
										if(intervalData_of_diff_contract.get(sbArray[4]+"_"+sbArray[5]+"_"+sbArray[6])==null){ 
											intervalData_of_diff_contract.put(sbArray[4]+"_"+sbArray[5]+"_"+sbArray[6], new IntervalData()); 
										}
										tempIntervalData = intervalData_of_diff_contract.get(sbArray[4]+"_"+sbArray[5]+"_"+sbArray[6]);
									}else{
										if(intervalData_of_diff_contract.get(sbArray[4])==null){ 
											intervalData_of_diff_contract.put(sbArray[4], new IntervalData()); 
										}
										tempIntervalData = intervalData_of_diff_contract.get(sbArray[4]);
									}

									if(tempIntervalData != null){
										//setup date
										if(tempIntervalData.data_date_within_interval==null) { 
											tempIntervalData.data_date_within_interval = sbArray[0]; 
										}
										//setup time
										if(tempIntervalData.data_time_within_interval==null) { 
											tempIntervalData.data_time_within_interval = sbArray[1]; 
										}
										//setup datetime
										if(tempIntervalData.data_datetime_within_interval==null) { 
											tempIntervalData.data_datetime_within_interval = sbArray[0]+sbArray[1]; 
										}
										//setup index
										if(tempIntervalData.data_index_within_interval==null) { 
											tempIntervalData.data_index_within_interval = sbArray[2]; 
										}
										//setup open
										if(tempIntervalData.data_open_within_interval==null) { 
											tempIntervalData.data_open_within_interval = sbArray[2]; 
										}
										//setup high
										if(tempIntervalData.data_high_within_interval==null || Integer.parseInt(tempIntervalData.data_high_within_interval)<Integer.parseInt(sbArray[2]) ) {
											tempIntervalData.data_high_within_interval = sbArray[2]; 
										}
										//setup low
										if(tempIntervalData.data_low_within_interval==null || Integer.parseInt(tempIntervalData.data_low_within_interval)>Integer.parseInt(sbArray[2]) ) {
											tempIntervalData.data_low_within_interval = sbArray[2]; 
										}
										//setup close
										if(true) { 
											tempIntervalData.data_close_within_interval = sbArray[2]; 
										}
										//setup sum up volume
										if(true) { 
											tempIntervalData.data_sumupvolume_within_interval = (tempIntervalData.data_sumupvolume_within_interval==null?0:Integer.parseInt(tempIntervalData.data_sumupvolume_within_interval)) + Integer.parseInt(sbArray[3]) + ""; 
										}

										if(onlyIntervalData==false) {
											Double newIndex = Double.parseDouble(sbArray[2]);
											
											//skip if the index defined as noise
											boolean noise = false;
											if(mitigateNoiseWithPrecentage>-1) {
												if(sbArray.length>6){
													if(previousDataDetail.get(sbArray[4]+"_"+sbArray[5]+"_"+sbArray[6])!=null) {
														Double prevIndex = previousDataDetail.get(sbArray[4]+"_"+sbArray[5]+"_"+sbArray[6]).getDouble("index");
														if( Math.abs((newIndex - prevIndex) / prevIndex * 100) < mitigateNoiseWithPrecentage) 
															noise = true;
													}
												}else{
													if(previousDataDetail.get(sbArray[4])!=null) {
														Double prevIndex = previousDataDetail.get(sbArray[4]).getDouble("index");
														if( Math.abs((newIndex - prevIndex) / prevIndex * 100) < mitigateNoiseWithPrecentage) 
															noise = true;
													}
												}
											}
											
											dataDetail = null;
											if(noise == false) {
												dataDetail = new JSONObject();
												//setup other information
												dataDetail.put("dataSourceID", runJobID);
												dataDetail.put("type", "tick");
												dataDetail.put("symbol", symbol);
												dataDetail.put("market", "future");
												dataDetail.put("date", sbArray[0]);
												dataDetail.put("time", sbArray[1]);
												dataDetail.put("datetime", sbArray[0]+sbArray[1]);
												dataDetail.put("index", newIndex);
												dataDetail.put("open", Double.parseDouble(tempIntervalData.data_open_within_interval));
												dataDetail.put("high", Double.parseDouble(tempIntervalData.data_high_within_interval));
												dataDetail.put("low", Double.parseDouble(tempIntervalData.data_low_within_interval));
												dataDetail.put("volume", Integer.parseInt(sbArray[3]));
												dataDetail.put("total_volume", Integer.parseInt(tempIntervalData.data_sumupvolume_within_interval));
												dataDetail.put("expiration_year_month", sbArray[4]);
												if(sbArray.length>6){
													dataDetail.put("strike_price", sbArray[5]);
													dataDetail.put("direction", sbArray[6]);
												}
												
												//insert into data
												data.add(dataDetail);
											}

											if(sbArray.length>6){
												previousDataDetail.put(sbArray[4]+"_"+sbArray[5]+"_"+sbArray[6], dataDetail);
											}else{
												previousDataDetail.put(sbArray[4], dataDetail);
											}
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
			dataForReading = new ArrayList<JSONObject>(data);
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
		for (Map.Entry<String, IntervalData> intervalData : intervalData_of_diff_contract.entrySet()) {
			IntervalData tempIntervalData = intervalData.getValue();
			if(tempIntervalData.data_date_within_interval!=null) {
				//sum up data
				dataDetail = new JSONObject();
				dataDetail.put("dataSourceID", runJobID);
				dataDetail.put("type", "interval");
				dataDetail.put("symbol", symbol);
				dataDetail.put("market", "future");
				dataDetail.put("date", tempIntervalData.data_date_within_interval);
				dataDetail.put("time", tempIntervalData.data_time_within_interval);
				dataDetail.put("datetime", tempIntervalData.data_datetime_within_interval);
				dataDetail.put("index", Double.parseDouble(tempIntervalData.data_index_within_interval));
				dataDetail.put("open", Double.parseDouble(tempIntervalData.data_open_within_interval));
				dataDetail.put("high", Double.parseDouble(tempIntervalData.data_high_within_interval));
				dataDetail.put("low", Double.parseDouble(tempIntervalData.data_low_within_interval));
				dataDetail.put("close", Double.parseDouble(tempIntervalData.data_close_within_interval));
				dataDetail.put("total_volume", Integer.parseInt(tempIntervalData.data_sumupvolume_within_interval));
				if(!intervalData.getKey().contains("_")){
					dataDetail.put("expiration_year_month", intervalData.getKey());
				}else{
					dataDetail.put("expiration_year_month", intervalData.getKey().split("_")[0]);
					dataDetail.put("strike_price", Integer.parseInt(intervalData.getKey().split("_")[1]));
					dataDetail.put("direction", intervalData.getKey().split("_")[2].equals("C")?"CALL":"PUT");
				}
				//insert into data
				data.add(dataDetail);
			}
			//rest
			tempIntervalData.data_date_within_interval = null;
			tempIntervalData.data_time_within_interval = null;
			tempIntervalData.data_datetime_within_interval = null;
			tempIntervalData.data_index_within_interval = null;
			tempIntervalData.data_open_within_interval = null;
			tempIntervalData.data_high_within_interval = null;
			tempIntervalData.data_low_within_interval = null;
			tempIntervalData.data_close_within_interval = null;
			tempIntervalData.data_sumupvolume_within_interval = null;
			interval_starttime = Constants.addSeconds(interval_endtime, 1);
			interval_endtime = Constants.addSeconds(interval_starttime, interval_in_seconds);
		}
		intervalData_of_diff_contract = new HashMap<String, IntervalData>();
	}
}
