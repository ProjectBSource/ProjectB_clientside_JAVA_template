package Util;

public class PlaceLimitOrderRequestStructure {
	private String activity;
	private String dataSourceID;
	private final String ordertype = "Limit";
	private String action;
	private int quantity;
	private double lmtprice;
	public PlaceLimitOrderRequestStructure(String activity, String dataSourceID, String action, int quantity, double lmtprice) {
		this.activity = activity;
		this.dataSourceID = dataSourceID;
		this.action = action;
		this.quantity = quantity;
		this.lmtprice = lmtprice;
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
	
}
