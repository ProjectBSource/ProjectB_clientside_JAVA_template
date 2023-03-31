package TradeControl;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;

import ClientSocketControl.DataStructure;
import TradeControl.OrderActionConstants.Action;
import TradeControl.OrderActionConstants.Direction;
import TradeControl.OrderActionConstants.ExpiryDate;
import TradeControl.OrderActionConstants.StrikePrice;

public class TradeController {
	Profile profile = new Profile();
	ArrayList<Order> orders = new ArrayList<>();
	JSONArray trade_notification_list = null;
	JSONObject trade_notification = null;
	Gson gson = null;
	Double slippage = 0D;
	
	/**
     *A necessary method to check the order allow to trade or not, you should call this method every read the data streaming message.
     */
	public JSONArray tradeCheckingAndBalanceUpdate(DataStructure ds) throws JsonProcessingException, JSONException {
		//check and update the order
		trade_notification_list = new JSONArray();
		trade_notification = null;
		for(Order order : orders) {
			trade_notification = order.trade(profile, ds, slippage);
			if(trade_notification==null) {
				trade_notification_list.put(trade_notification);
			}
		}
		
		//update profile balance
		profile.balance = 0;
		for (Map.Entry<String, Integer> item : profile.holding.entrySet()) {
		    if(item.getKey().equals(ds.getSymbol())) {
		    	profile.balance += item.getValue() * ds.getIndex();
		    }
		}
		profile.balance += profile.cash;  
		
		//return 
		if(trade_notification_list.isEmpty()) return null;
		else return trade_notification_list;
	}
	
	/**
     *You can set the slippage to make the trading more real
     *Program will according to the slippage to random trading price within the percentage change
     */
	public void setSlippage(double percentage) {
		this.slippage = percentage;
	}
	
	/**
     *For Stock and Future trading
     */
	public void placeOrder(String symbol, Action action, int quantity) {
		orders.add(new Order(symbol, action, quantity));
	}
    
	/**
     *For Option trading
     */
	public void placeOrder(String symbol, Action action, Direction direction, StrikePrice sp, ExpiryDate ed, int quantity) {
		orders.add(new Order(symbol, action, direction, sp, ed, quantity));
	}
	
	/**
     *Get the Profile information 
     */
	public JSONObject getProfile() {
		//return profile.toJSONObject();
		gson = new Gson();
		String jsonString = gson.toJson(profile);
		if(jsonString!=null) {
			return new JSONObject(jsonString);
		}
		return null;
	}
}
