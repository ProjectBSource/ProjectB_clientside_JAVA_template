package Util;

public class PlaceStopOrderRequestStructure {
	private String activity;
	private final String ordertype = "Stop";
	private String action;
	private int quantity;
	private double stpprice;
	public PlaceStopOrderRequestStructure(String activity, String action, int quantity, double stpprice) {
		this.activity = activity;
		this.action = action;
		this.quantity = quantity;
		this.stpprice = stpprice;
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
	public double getStpprice() {
		return stpprice;
	}
	public void setStpprice(double stpprice) {
		this.stpprice = stpprice;
	}
	
}
