package TradeControl;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;

import ClientSocketControl.DataStructure;
import TradeControl.OrderActionConstants.Action;
import TradeControl.OrderActionConstants.Direction;
import TradeControl.OrderActionConstants.ExpiryDate;
import TradeControl.OrderActionConstants.StrikePrice;

public class TradeController {
	Profile profile = new Profile();
	ArrayList<MarketOrder> marketOrders = new ArrayList<>();
	JSONArray trade_notification_list = null;
	JSONObject trade_notification = null;
	Gson gson = null;
	
	/**
     *A necessary method to check the order allow to trade or not, you should call this method every read the data streaming message.
     */
	public JSONArray tradeCheckingAndBalanceUpdate(DataStructure ds) throws JsonProcessingException, JSONException {
		//check and update the order
		trade_notification_list = new JSONArray();
		trade_notification = null;
		for(MarketOrder order : marketOrders) {
			trade_notification = order.trade(profile, ds);
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
     *For Stock and Future trading
     */
	public void placeMarketOrder(String symbol, Action action, int quantity) {
		marketOrders.add(new MarketOrder(symbol, action, quantity));
	}
    
	/**
     *For Option trading
     */
	public void placeMarketOrder(String symbol, Action action, Direction direction, StrikePrice sp, ExpiryDate ed, int quantity) {
		marketOrders.add(new MarketOrder(symbol, action, direction, sp, ed, quantity));
	}
	
	/**
     *Get the Profile information 
     */
	public JSONObject getProfile() {
		//return profile.toJSONObject();
		return new JSONObject(profile);
	}
}
