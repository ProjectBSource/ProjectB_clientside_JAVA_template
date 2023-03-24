package Util;

public class PlaceStopLimitOrderRequestStructure {
	private String activity;
	private String dataSourceID;
	private final String ordertype = "StopLimit";
	private String action;
	private int quantity;
	private double lmtprice;
	private double stpprice;
	public PlaceStopLimitOrderRequestStructure(String activity, String dataSourceID, String action, int quantity, double lmtprice, double stpprice) {
		this.activity = activity;
		this.dataSourceID = dataSourceID;
		this.action = action;
		this.quantity = quantity;
		this.lmtprice = lmtprice;
		this.stpprice = stpprice;
	}
	public String getActivity() {
		return activity;
	}
	public void setActivity(String activity) {
		this.activity = activity;
	}
	public String getDataSourceID() {
		return dataSourceID;
	}
	public void setDataSourceID(String dataSourceID) {
		this.dataSourceID = dataSourceID;
	}
	public String getOrdertype() {
		return ordertype;
	}
	private void setOrdertype() {
		//
	}
	public String getaction() {
		return action;
	}
	public void setaction(String action) {
		this.action = action;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public double getLmtprice() {
		return lmtprice;
	}
	public void setLmtprice(double lmtprice) {
		this.lmtprice = lmtprice;
	}
	public double getStpprice() {
		return stpprice;
	}
	public void setStpprice(double stpprice) {
		this.stpprice = stpprice;
	}
	
}
