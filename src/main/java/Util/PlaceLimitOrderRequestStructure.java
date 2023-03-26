package Util;

public class PlaceLimitOrderRequestStructure {
	private String activity;
	private final String ordertype = "Limit";
	private String action;
	private int quantity;
	private double lmtprice;
	public PlaceLimitOrderRequestStructure(String activity, String action, int quantity, double lmtprice) {
		this.activity = activity;
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
