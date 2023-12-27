package TradeControl;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import ClientSocketControl.DataStructure;

class Profile {
	public HashMap<String, Integer> holding = new HashMap<>();
	public double profits;
	public ArrayList<String> historyInJSON = new ArrayList<>();

	public Profile(){
		JSONObject profileDetailInJSON = new JSONObject();
		profileDetailInJSON.put("holding", this.holding);
		profileDetailInJSON.put("profits", this.profits);
		historyInJSON.add(profileDetailInJSON.toString());
	}
	
	public void update(String symbol, int quantity, double price, DataStructure ds) {
		if(holding.containsKey(symbol)) {  
			holding.put(symbol, holding.get(symbol)+quantity);
		}
		else {
			holding.put(symbol, quantity);
		}

		//update profile profits
		profits = 0;
		for (Map.Entry<String, Integer> item : holding.entrySet()) {
			if(item.getKey().contains("-")){
				if(item.getKey().equals(ds.getSymbol()+"-"+ds.getDirection()+"-"+ds.getStrike_price())) {
					profits += item.getValue() * ds.getIndex();
				}
			}
			else{
				if(item.getKey().equals(ds.getSymbol())) {
					profits += item.getValue() * ds.getIndex();
				}
			}
		}
	}

	public void addHistoryNode(DataStructure data){
		JSONObject profileDetailInJSON = new JSONObject();
		profileDetailInJSON.put("orderDateTime", data.getDatetime());
		profileDetailInJSON.put("holding", this.holding);
		profileDetailInJSON.put("profits", this.profits);
		historyInJSON.add(profileDetailInJSON.toString());
	}
}
