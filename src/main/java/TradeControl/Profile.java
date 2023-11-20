package TradeControl;

import java.util.HashMap;

class Profile {
	public HashMap<String, Integer> holding = new HashMap<>();
	public double profits;
	public double cash;
	
	public void update(String symbol, int quantity, double price) {
		if(holding.containsKey(symbol)) { 
			holding.put(symbol, holding.get(symbol)+quantity); 
		}
		else {
			holding.put(symbol, quantity);
		}
		cash -= (quantity * price);
	}
}
