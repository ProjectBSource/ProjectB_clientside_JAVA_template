package TradeController;

import java.util.ArrayList;

import org.json.JSONObject;

import Client.DataStructure;
import TradeController.OrderActionConstants.Action;
import TradeController.OrderActionConstants.Direction;
import TradeController.OrderActionConstants.ExpiryDate;
import TradeController.OrderActionConstants.StrikePrice;

public class TradeController {
	ArrayList<MarketOrder> marketOrders = new ArrayList<>();
	
	public void tradeChecking(DataStructure ds) {
		
	}
	
	public void placeMarketOrder(Action action, int quantity) {
		marketOrders.add(new MarketOrder(action, quantity));
	}
	
	public void placeMarketOrder(Action action, Direction direction, StrikePrice sp, ExpiryDate ed, int quantity) {
		marketOrders.add(new MarketOrder(action, direction, sp, ed, quantity));
	}
}
