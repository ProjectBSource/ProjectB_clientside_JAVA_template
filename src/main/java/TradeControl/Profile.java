package TradeControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import ClientSocketControl.DataStructure;
import TradeControl.OrderActionConstants.Action;

class Profile {
	public HashMap<String, Integer> holding = new HashMap<>();
	public HashMap<String, Double> orderID_tradePrice = new HashMap<>();
	public ArrayList<Object[]> orderID_profits = new ArrayList<>();
	public HashMap<String, Double> symbol_accumProfits = new HashMap<>();
	public ArrayList<String> historyInJSON = new ArrayList<>();

	public Profile(){
		JSONObject profileDetailInJSON = new JSONObject();
		profileDetailInJSON.put("holding", this.holding);
		profileDetailInJSON.put("accum.Profits", this.symbol_accumProfits);
		profileDetailInJSON.put("profits", this.orderID_profits);
		historyInJSON.add(profileDetailInJSON.toString());
	}
	
	public void update(String symbol, int temp_trade_amount, double price, Order order) {
		if(holding.containsKey(symbol)) {  
			holding.put(symbol, holding.get(symbol)+temp_trade_amount);
		}
		else {
			holding.put(symbol, temp_trade_amount);
		}

		boolean offTrade = order.orderAlias.contains("OFF");
		if(offTrade==false){
			orderID_tradePrice.put(order.orderid, order.tradePrice);
			if(symbol_accumProfits.containsKey(order.symbol)==false) {  
				symbol_accumProfits.put(order.symbol, 0.0);
			}
		}else{
			double tempProfits = (order.action==Action.SELL?(order.tradePrice - orderID_tradePrice.get(order.targetOffTradeId)):(orderID_tradePrice.get(order.targetOffTradeId) - order.tradePrice));
			orderID_profits.put(new Object[]{order.targetOffTradeId, new Double(tempProfits)} );
			symbol_accumProfits.put(order.symbol, symbol_accumProfits.get(order.symbol)+tempProfits );
		}
	}

	public void addHistoryNode(DataStructure data){
		JSONObject profileDetailInJSON = new JSONObject();
		profileDetailInJSON.put("orderDateTime", data.getDatetime());
		profileDetailInJSON.put("holding", this.holding);
		profileDetailInJSON.put("accum.Profits", this.symbol_accumProfits);
		profileDetailInJSON.put("profits", this.orderID_profits);
		historyInJSON.add(profileDetailInJSON.toString());
	}
}
