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
			}
        }

        //Move the completed OFF trade to completedOrders arraylist
		HashMap<String, Order> tempNewOrders = new HashMap<>(orders);
		for(Map.Entry<String, Order> order : orders.entrySet()){
            if(order.getValue().orderAlias.contains("_OFF")){
                if(order.getValue().remained==0){
                    completedOrders.add(orders.get(order.getKey().substring(0, order.getKey().indexOf("_OFF"))));
                    tempNewOrders.remove( order.getKey().substring(0, order.getKey().indexOf("_OFF")) );
                    completedOrders.add(order.getValue());
                    tempNewOrders.remove(order.getKey());
                }
            }
		}
		orders = tempNewOrders;
		
		//update profile profits
		profile.profits = 0;
		for (Map.Entry<String, Integer> item : profile.holding.entrySet()) {
			if(item.getKey().contains("-")){
				if(item.getKey().equals(ds.getSymbol()+"-"+ds.getAction()+"-"+ds.getStrike_price())) {
					profile.profits += item.getValue() * ds.getIndex();
				}
			}
			else{
				if(item.getKey().equals(ds.getSymbol())) {
					profile.profits += item.getValue() * ds.getIndex();
				}
			}
		}
		profile.profits += profile.cash;  
		
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
	public boolean placeOrder(String id, DataStructure dataStructure, Action action, int quantity, String expiryMonth, String reason) throws Exception {
        if(orders.get(id)==null){
			orders.put(id, new Order(id, dataStructure, action, quantity, expiryMonth, reason));
			return true;
		}
		return false;
	}
    
	/**
     *For Option trading
     */
	public boolean placeOrder(String id, DataStructure dataStructure, Action action, Direction direction, String strikePrice, String expiryMonth, int quantity, String reason) throws Exception {
        if(orders.get(id)==null){
			orders.put(id, new Order(id, dataStructure, action, direction, strikePrice, expiryMonth, quantity, reason));
			return true;
		}
		return false;
	}

	public boolean placeOFFOrder(String targetId, DataStructure dataStructure, String reason) throws Exception {
		Order targetOrder = orders.get(targetId);
		if(targetOrder!=null && orders.get(targetId+"_OFF")==null){
            if(targetOrder.remained>0){
                targetOrder.remained = 0;
                targetOrder.addNewDescription("off trade signal triggered; ");
            }
			if(profile.holding.get(targetOrder.symbol)!=null){
				//For non option trade off
				if(targetOrder.direction==null){
					if(targetOrder.action==Action.BUY){
						orders.put(targetId+"_OFF", new Order(targetId+"_OFF", dataStructure, Action.SELL, targetOrder.totalTraded, targetOrder.expiryMonth, reason));
					}
					else if(targetOrder.action==Action.SELL){
						orders.put(targetId+"_OFF", new Order(targetId+"_OFF", dataStructure, Action.BUY, targetOrder.totalTraded, targetOrder.expiryMonth, reason));
					}
				}
				//For option trade off
				if(targetOrder.direction!=null){
					if(targetOrder.action==Action.BUY){
						orders.put(targetId+"_OFF", new Order(targetId+"_OFF", dataStructure, Action.SELL, targetOrder.direction, targetOrder.strickPrice, targetOrder.expiryMonth, targetOrder.totalTraded, reason));
					}
					else if(targetOrder.action==Action.SELL){
						orders.put(targetId+"_OFF", new Order(targetId+"_OFF", dataStructure, Action.BUY, targetOrder.direction, targetOrder.strickPrice, targetOrder.expiryMonth, targetOrder.totalTraded, reason));
					}
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
			for(String childOrderInJSON : order.historyInJSON){
				history.put(new JSONObject(childOrderInJSON));
			}
        }
        for (Map.Entry<String, Order> order : orders.entrySet()) {
			for(String childOrderInJSON : order.getValue().historyInJSON){
				history.put(new JSONObject(childOrderInJSON));
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
	public JSONObject getProfileHistoryInJSON() throws JsonProcessingException, JSONException {
		JSONArray history = new JSONArray();
        for(String pH : profile.historyInJSON){
			history.put(new JSONObject(pH));
        }
		JSONObject result = new JSONObject();
		result.put("profileHistory", history);
		return result;
	}

    /**
     *Get the Profile information
     */
	public Profile getProfile() {
		return profile;
	}
}
