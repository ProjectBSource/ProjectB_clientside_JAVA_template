package Client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;

public class LocalTradingSimulator {
	private HashMap<String, JSONObject> pending_trade_record = new HashMap<String, JSONObject>();
	private HashMap<String, JSONObject> partialfill_trade_record = new HashMap<String, JSONObject>();
	private HashMap<String, JSONObject> fullyfill_trade_record = new HashMap<String, JSONObject>();
	private HashMap<String, JSONObject> balance = new HashMap<String, JSONObject>();
	private JSONObject tempTradeRecord;
	
	public void dataCheck(JSONObject data) {
		//Handling pending_trade_record
		for (Map.Entry<String, JSONObject> p_t_r : pending_trade_record.entrySet()) {
			switch(p_t_r.getValue().getString("tradeType")) {
				case "Market Order":
					
					break;
				case "Limit Order":
					
					break;	
				case "Stop Order":
									
					break;	
				case "Stop Limit Order":
					
					break;	
				case "Trailing Stop Order":
					
					break;	
				case "Bracket Order":
					
					break;	
				case "Good-Till-Canceled Order":
					
					break;	
				case "One-Cancels-the-Other Order":
					
					break;	
				case "Immediate-or-Cancel Order":
					
					break;	
				case "Fill-or-Kill Order":
					
					break;	
			}
		}
	}
	
	public String MarketOrder(String symbol ,int quantity, float price) {
		return newTradeOrder("Market Order", symbol, quantity, price);
	}
	
	public String LimitOrder(String symbol ,int quantity, float price) {
		return newTradeOrder("Limit Order", symbol, quantity, price);
	}
	
	public String StopOrder(String symbol ,int quantity, float price) {
		return newTradeOrder("Stop Order", symbol, quantity, price);
	}
	
	public String StopLimitOrder(String symbol ,int quantity, float price) {
		return newTradeOrder("Stop Limit Order", symbol, quantity, price);
	}
	
	public String TrailinStopOrder(String symbol ,int quantity, float price) {
		return newTradeOrder("Trailing Stop Order", symbol, quantity, price);
	}

	public String BracketOrder(String symbol ,int quantity, float price) {
		return newTradeOrder("Bracket Order", symbol, quantity, price);
	}
	
	public String GoodTillCanceledOrder(String symbol ,int quantity, float price) {
		return newTradeOrder("Good-Till-Canceled Order", symbol, quantity, price);
	}
	
	public String OneCancelsTheOtherOrder(String symbol ,int quantity, float price) {
		return newTradeOrder("One-Cancels-the-Other Order", symbol, quantity, price);
	}
	
	public String ImmediateOrCancelOrder(String symbol ,int quantity, float price) {
		return newTradeOrder("Immediate-or-Cancel Order", symbol, quantity, price);
	}
	
	public String FillOrKillOrder(String symbol ,int quantity, float price) {
		return newTradeOrder("Fill-or-Kill Order", symbol, quantity, price);
	}
	
	private String newTradeOrder(String tradeType, String symbol ,int quantity, float price) {
		tempTradeRecord = new JSONObject();
		tempTradeRecord.put("tradeType", tradeType);
		tempTradeRecord.put("symbol", symbol);
		tempTradeRecord.put("quantity", quantity);
		tempTradeRecord.put("price", price);
		pending_trade_record.put(UUID.randomUUID().toString(), tempTradeRecord);
		return String.format("%s created, symbol:%s quantity:%s price:%s", tradeType, symbol, quantity, price);
	}
	
}


