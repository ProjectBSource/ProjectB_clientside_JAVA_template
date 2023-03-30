package TradeControl;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;

import ClientSocketControl.DataStructure;
import TradeControl.OrderActionConstants.Action;
import TradeControl.OrderActionConstants.Direction;
import TradeControl.OrderActionConstants.ExpiryDate;
import TradeControl.OrderActionConstants.StrikePrice;

public class MarketOrder extends Constants{
	private String symbol;
	private Action action;
	private Direction direction;
	private StrikePrice sp;
	private ExpiryDate ed;
	private int quantity;
	private int traded;
	private int remained;
	private double averageTradePrice;
	private String status;
	private Date lastUpdateDateTime;
	private ArrayList<MarketOrder> history = new ArrayList<>();

	public MarketOrder(String symbol, Action action, int quantity) {
		this.symbol = symbol;
		this.orderid = UUID.randomUUID().toString();
		this.orderDateTime = new Date();
		this.action = action;
		this.quantity = quantity;
		this.remained = quantity;
		this.status = status_OPEN;
		this.lastUpdateDateTime = this.orderDateTime;
		this.history.add(new MarketOrder(this));
	}
	
	public MarketOrder(String symbol, Action action, Direction direction, StrikePrice sp, ExpiryDate ed,  int quantity) {
		this.symbol = symbol;
		this.orderid = UUID.randomUUID().toString();
		this.orderDateTime = new Date();
		this.action = action;
		this.direction = direction;
		this.sp = sp;
		this.ed = ed;
		this.quantity = quantity;
		this.remained = quantity;
		this.status = status_OPEN;
		this.lastUpdateDateTime = this.orderDateTime;
		this.history.add(new MarketOrder(this));
	}
	
	private MarketOrder(MarketOrder market) {
		this.symbol = market.symbol;
		this.orderid = market.orderid;
		this.orderDateTime = market.orderDateTime;
		this.action = market.action;
		this.direction = market.direction;
		this.sp = market.sp;
		this.ed = market.ed;
		this.quantity = market.quantity;
		this.traded = market.traded;
		this.remained = market.remained;
		this.averageTradePrice = market.averageTradePrice;
		this.status = market.status;
		this.lastUpdateDateTime = market.lastUpdateDateTime;
	}

	public JSONObject trade(Profile profile, DataStructure data) throws JsonProcessingException, JSONException {
		if(direction==null && sp==null && ed==null) {
			if(remained>0) {
				int temp_trade_amount = (data.getVolumn()>=remained)?remained:data.getVolumn();
				traded += temp_trade_amount;
				remained -= temp_trade_amount;
				averageTradePrice = (averageTradePrice + (temp_trade_amount * data.getIndex())) / traded;
				MarketOrder temp_maket = new MarketOrder(this);
				history.add(temp_maket);
				//Update profle
				if(action == Action.SELL) { temp_trade_amount *= -1; }
				profile.update(symbol, temp_trade_amount, data.getIndex());
				return new JSONObject(this);
			}
		}
		return null;
	}
	
}
