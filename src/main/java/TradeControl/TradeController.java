package TradeControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ClientSocketControl.DataStructure;
import TradeControl.OrderActionConstants.Action;
import TradeControl.OrderActionConstants.Direction;
import TradeControl.OrderActionConstants.ExpiryDate;
import TradeControl.OrderActionConstants.StrikePrice;


public class TradeController {
	Profile profile = new Profile();
	HashMap<String, Order> orders = new HashMap<>();
    ArrayList<Order> completedOrders = new ArrayList();

	JSONArray trade_notification_list = null;
	JSONObject trade_notification = null;
	Double slippage = 0D;
	
	/**
     *A necessary method to check the order allow to trade or not, you should call this method every read the data streaming message.
     */
	public JSONArray tradeCheckingAndBalanceUpdate(DataStructure ds) throws Exception {
		//check and update the order
		trade_notification_list = new JSONArray();
		trade_notification = null;

        for (Map.Entry<String, Order> order : orders.entrySet()) {
            trade_notification = order.getValue().trade(profile, ds, slippage);
			if(trade_notification!=null) {
				trade_notification_list.put(trade_notification);
				//update order history node
				order.getValue().history.add(new Order());
				order.getValue().history.get(order.getValue().history.size()-1).copyOrder(order);
			}
        }

		//Move the completed OFF trade to completedOrders arraylist
		HashMap<String, Order> tempNewOrders = new HashMap<>(orders);
		for(Map.Entry<String, Order> order : orders.entrySet()){
			if(order.getKey().contains("_OFF")){
				if(order.getValue().remained==0){
					completedOrders.add(order.getValue());
					tempNewOrders.remove(order.getKey());
				}
			}
		}
		orders = tempNewOrders;
		
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
	public boolean placeOrder(String id, DataStructure dataStructure, Action action, int quantity, boolean oneTimeTradeCheck) throws Exception {
		if(orders.get(id)==null){
			orders.put(id, new Order(id, dataStructure, action, quantity, oneTimeTradeCheck));
			return true;
		}
		return false;
	}
    
	/**
     *For Option trading
     */
	public boolean placeOrder(String id, String symbol, Action action, Direction direction, StrikePrice sp, ExpiryDate ed, int quantity, boolean oneTimeTradeCheck) {
		if(orders.get(id)==null){
			orders.put(id, new Order(id, symbol, action, direction, sp, ed, quantity, oneTimeTradeCheck));
			return true;
		}
		return false;
	}

	public boolean placeOFFOrder(String targetId, DataStructure dataStructure) throws Exception {
		Order order = orders.get(targetId);
		if(order!=null){
			if(profile.holding.get(order.symbol)!=null){
				//For non option trade off
				if(order.direction==null){
					if(order.action==Action.BUY){
						orders.put(targetId+"_OFF", new Order(targetId+"_OFF", dataStructure, Action.SELL, order.traded, order.oneTimeTradeCheck));
					}
					else if(order.action==Action.SELL){
						orders.put(targetId+"_OFF", new Order(targetId+"_OFF", dataStructure, Action.BUY, order.traded, order.oneTimeTradeCheck));
					}
				}
				//For option trade off
				if(order.direction!=null){
					if(order.action==Action.BUY){
						orders.put(targetId+"_OFF", new Order(targetId+"_OFF", order.symbol, Action.SELL, order.direction, order.sp, order.ed, order.traded, order.oneTimeTradeCheck));
					}
					else if(order.action==Action.SELL){
						orders.put(targetId+"_OFF", new Order(targetId+"_OFF", order.symbol, Action.BUY, order.direction, order.sp, order.ed, order.traded, order.oneTimeTradeCheck));
					}
				}
				if(order.oneTimeTradeCheck==false){
					completedOrders.add(order);
					orders.remove(targetId);
				}
				return true;
			}
		}
		return false;
	}

	/**
     *Get the order history in JSON
	 * @throws JSONException
	 * @throws JsonProcessingException
     */
	public JSONObject getOrderHistoryInJSON() throws JsonProcessingException, JSONException {
		JSONArray history = new JSONArray();
        for(Order order : completedOrders){
			for(Order childOrder : order.history){
				history.put(childOrder.orderDetailInJSON);
			}
        }
        for (Map.Entry<String, Order> order : orders.entrySet()) {
			for(Order childOrder : order.getValue().history){
				history.put(childOrder.orderDetailInJSON);
			}
        }
		JSONObject result = new JSONObject();
		result.put("orderHistory", history);
		return result;
	}
	
	/**
     *Get the Profile information in JSON
	 * @throws JSONException
	 * @throws JsonProcessingException
     */
	public JSONObject getProfileInJSON() throws JsonProcessingException, JSONException {
		//return profile.toJSONObject();
        ObjectMapper mapper = new ObjectMapper();
		JSONObject p = new JSONObject(mapper.writeValueAsString(profile));
		if(p!=null) {
			return p;
		}
		return null;
	}

    /**
     *Get the Profile information
     */
	public Profile getProfile() {
		return profile;
	}
}
