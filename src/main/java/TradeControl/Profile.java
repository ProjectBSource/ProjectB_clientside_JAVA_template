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
	public HashMap<String, Double> orderID_profits = new HashMap<>();
	public HashMap<String, Double> symbol_accumProfits = new HashMap<>();
	public ArrayList<String> historyInJSON = new ArrayList<>();

	public Profile(){
		JSONObject profileDetailInJSON = new JSONObject();
		profileDetailInJSON.put("holding", this.holding);
		profileDetailInJSON.put("profits", this.profits);
		historyInJSON.add(profileDetailInJSON.toString());
	}
	
	public void update(String symbol, int temp_trade_amount, double price, Order order) {
		if(holding.containsKey(symbol)) {  
			holding.put(symbol, holding.get(symbol)+temp_trade_amount);
		}
		else {
			holding.put(symbol, temp_trade_amount);
		}

		offTrade = order.orderAlias.contains("OFF");
		if(offTrade==false){
			tradePrice.put(order.orderid, order.tradePrice);
			if(symbol_accumProfits.containsKey(order.symbol)==false) {  
				symbol_accumProfits.put(order.symbol, 0.0);
			}
		}else{
			tempProfits = (order.action==Action.SELL?(order.tradePrice - tradePrice.get(order.targetOffTradeId)):(tradePrice.get(order.targetOffTradeId) - order.tradePrice));
			profits.put(order.targetOffTradeId, tempProfits );
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
