package Util;

public class PlaceMarketOrderRequestStructure {
	private String activity;
	private String dataSourceID;
	private final String ordertype = "market";
	private String action;
	private int quantity;
	public PlaceMarketOrderRequestStructure(String activity, String dataSourceID, String action, int quantity) {
		this.activity = activity;
		this.dataSourceID = dataSourceID;
		this.action = action;
		this.quantity = quantity;
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
	
}
