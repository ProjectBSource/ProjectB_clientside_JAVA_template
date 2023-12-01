package TradeControl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import ClientSocketControl.DataStructure;
import TradeControl.OrderActionConstants.Action;
import TradeControl.OrderActionConstants.Direction;
import TradeControl.OrderActionConstants.StrikePrice;

import DataController.Constants;

import Util.WebVersionJobConstants;

public class Order {
	public Random random = new Random();
	public String orderid;
	public String orderAlias;
	public Date orderDateTime;
	public String symbol;
	public Action action;
	public Direction direction;
	public StrikePrice strickPrice;
	public String expiryMonth;
	public int quantity;
	public int totalTraded;
	public int remained;
	public double averageTradePrice;
	public double tradePrice;
	public Date lastUpdateDateTime;
	public Date orderFillDateTime;
	public JSONObject orderDetailInJSON;
	public String description;
	public ArrayList<Order> history = new ArrayList<>();
	public ArrayList<String> historyInJSON = new ArrayList<>();

	public Order(){}

	public Order(String orderAlias, DataStructure dataStructure, Action action, int quantity, String expiryMonth, String reason) throws Exception {
		this.symbol = dataStructure.getSymbol();
		this.orderid = UUID.randomUUID().toString();
		this.orderAlias = orderAlias;
		this.orderDateTime = Constants.df_yyyyMMddkkmmss.parse(dataStructure.getDatetime());
		this.action = action;
		this.expiryMonth = expiryMonth;
		this.quantity = quantity;
		this.remained = quantity;
		this.lastUpdateDateTime = this.orderDateTime;
		this.description = reason;
		orderDetailInJSON = new JSONObject();
		orderDetailInJSON.put("symbol", this.symbol);
		orderDetailInJSON.put("orderid", this.orderid);
		orderDetailInJSON.put("orderAlias", this.orderAlias);
		orderDetailInJSON.put("orderDateTime", this.orderDateTime);
		orderDetailInJSON.put("action", this.action.getAction());
		orderDetailInJSON.put("expiryMonth", this.expiryMonth);
		orderDetailInJSON.put("quantity", this.quantity);
		orderDetailInJSON.put("remained", this.remained);
		orderDetailInJSON.put("lastUpdateDateTime", this.lastUpdateDateTime);
		orderDetailInJSON.put("description", this.description);
	}
	
	public Order(String orderAlias, String symbol, Action action, Direction direction, StrikePrice strickPrice, String expiryMonth,  int quantity, String reason) {
		this.symbol = symbol;
		this.orderid = UUID.randomUUID().toString();
		this.orderAlias = orderAlias;
		this.orderDateTime = new Date();
		this.action = action;
		this.direction = direction;
		this.strickPrice = strickPrice;
		this.expiryMonth = expiryMonth;
		this.quantity = quantity;
		this.remained = quantity;
		this.lastUpdateDateTime = this.orderDateTime;
		this.description = reason;
		orderDetailInJSON = new JSONObject();
		orderDetailInJSON.put("symbol", this.symbol);
		orderDetailInJSON.put("orderid", this.orderid);
		orderDetailInJSON.put("orderAlias", this.orderAlias);
		orderDetailInJSON.put("orderDateTime", this.orderDateTime);
		orderDetailInJSON.put("action", this.action.getAction());
		orderDetailInJSON.put("direction", this.direction.getDirection());
		orderDetailInJSON.put("StrikePrice", this.strickPrice.getStrikePrice());
		orderDetailInJSON.put("expiryMonth", this.expiryMonth);
		orderDetailInJSON.put("quantity", this.quantity);
		orderDetailInJSON.put("remained", this.remained);
		orderDetailInJSON.put("lastUpdateDateTime", this.lastUpdateDateTime);
		orderDetailInJSON.put("description", this.description);
	}
	
	public void copyOrder(Order order) {
		this.symbol = order.symbol;
		this.orderid = order.orderid;
		this.orderAlias = order.orderAlias;
		this.orderDateTime = order.orderDateTime;
		this.action = order.action;
		this.direction = order.direction;
		this.strickPrice = order.strickPrice;
		this.expiryMonth = order.expiryMonth;
		this.quantity = order.quantity;
		this.totalTraded = order.totalTraded;
		this.remained = order.remained;
		this.averageTradePrice = order.averageTradePrice;
		this.lastUpdateDateTime = order.lastUpdateDateTime;
		this.tradePrice = order.tradePrice;
		this.orderFillDateTime = order.orderFillDateTime;
		this.orderDetailInJSON = order.orderDetailInJSON;
		this.description = order.description;
	}

    public void addNewDescription(String description){
        this.description += description;
        this.orderDetailInJSON.put("description", this.description);
    }

	public JSONObject trade(Profile profile, DataStructure data, double slippage) throws Exception {
		if(data.getType().equals("tick")) {
			if(this.remained>0) {
				//For future trading
				if(direction==null && strickPrice==null) {
					if(expiryMonth.equals(data.getExpiration_year_month())){
						int temp_trade_amount = (data.getVolume()>=this.remained)?this.remained:data.getVolume();
						tradePrice = data.getIndex() + ( (data.getIndex() *  slippage) * (random.nextInt(2)==0?1:-1) );
						averageTradePrice = ((averageTradePrice * totalTraded) + (temp_trade_amount * tradePrice)) / (totalTraded + temp_trade_amount);
						totalTraded += temp_trade_amount;
						this.remained -= temp_trade_amount;
						orderFillDateTime = Constants.df_yyyyMMddkkmmss.parse(data.getDatetime());
						orderDetailInJSON.put("traded", temp_trade_amount);
						orderDetailInJSON.put("totalTraded", totalTraded);
						orderDetailInJSON.put("remained", this.remained);
						orderDetailInJSON.put("tradePrice", tradePrice);
						orderDetailInJSON.put("averageTradePrice", averageTradePrice);
						orderDetailInJSON.put("orderFillDateTime", orderFillDateTime);
						//update order history node
						history.add(new Order());
						history.get(history.size()-1).copyOrder(this);
						historyInJSON.add(orderDetailInJSON.toString());
						//Update profle
						if(action == Action.SELL) { temp_trade_amount *= -1; }
						profile.update(symbol, temp_trade_amount, tradePrice);

						return orderDetailInJSON;
					}
				}
			}
			return null;
		}
		return null;
	}
	
}
