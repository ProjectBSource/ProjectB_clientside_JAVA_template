package TradeControl;

import java.util.HashMap;

class Profile {
	public HashMap<String, Integer> holding = new HashMap<>();
	public double profits;
	public double cash;
	public ArrayList<String> historyInJSON = new ArrayList<>();
	
	public void update(String symbol, int quantity, double price) {
		if(holding.containsKey(symbol)) {  
			holding.put(symbol, holding.get(symbol)+quantity);
		}
		else {
			holding.put(symbol, quantity);
		}
		cash -= (quantity * price);
	}

	public void addHistoryNode(DataStructure data){
		JSONObject profileDetailInJSON = new JSONObject();
		orderDetailInJSON.put("orderDateTime", this.orderDateTime);
		profileDetailInJSON.put("holding", this.holding);
		profileDetailInJSON.put("profits", this.profits);
		profileDetailInJSON.put("cash", this.cash);
		historyInJSON.add(profileDetailInJSON.toString());
	}
}
