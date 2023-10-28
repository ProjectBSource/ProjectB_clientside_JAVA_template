package TradeControl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;

import ClientSocketControl.DataStructure;
import TradeControl.OrderActionConstants.Action;
import TradeControl.OrderActionConstants.Direction;
import TradeControl.OrderActionConstants.ExpiryDate;
import TradeControl.OrderActionConstants.StrikePrice;

public class Order {
	private Random random = new Random();
	private String orderid;
	private Date orderDateTime;
	private String symbol;
	private Action action;
	private Direction direction;
	private StrikePrice sp;
	private ExpiryDate ed;
	private int quantity;
	private int traded;
	private int remained;
	private double averageTradePrice;
	private Date lastUpdateDateTime;
	private ArrayList<Order> history = new ArrayList<>();

	public Order(DataStructure dataStructure, Action action, int quantity) {
		this.symbol = dataStructure.getSymbol();
		this.orderid = UUID.randomUUID().toString();
		this.orderDateTime = new Date();
		this.action = action;
		this.quantity = quantity;
		this.remained = quantity;
		this.lastUpdateDateTime = this.orderDateTime;
		this.history.add(new Order(this));
	}
	
	public Order(DataStructure dataStructure, Action action, Direction direction, StrikePrice sp, ExpiryDate ed,  int quantity) {
		this.symbol = dataStructure.getSymbol();
		this.orderid = UUID.randomUUID().toString();
		this.orderDateTime = new Date();
		this.action = action;
		this.direction = direction;
		this.sp = sp;
		this.ed = ed;
		this.quantity = quantity;
		this.remained = quantity;
		this.lastUpdateDateTime = this.orderDateTime;
		this.history.add(new Order(this));
	}
	
	private Order(Order market) {
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
		this.lastUpdateDateTime = market.lastUpdateDateTime;
	}

	public JSONObject trade(Profile profile, DataStructure data, double slippage) throws JsonProcessingException, JSONException {
		if(data.getType().equals("interval")==false) {
			if(direction==null && sp==null && ed==null) {
				if(remained>0) {
					int temp_trade_amount = (data.getVolumn()>=remained)?remained:data.getVolumn();
					traded += temp_trade_amount;
					remained -= temp_trade_amount;
					double temp_trade_price = data.getIndex() + ( (data.getIndex() *  slippage) * (random.nextInt(2)==0?1:-1) );
					averageTradePrice = (averageTradePrice + (temp_trade_amount * temp_trade_price)) / traded;
					Order temp_maket = new Order(this);
					history.add(temp_maket);
					//Update profle
					if(action == Action.SELL) { temp_trade_amount *= -1; }
					profile.update(symbol, temp_trade_amount, temp_trade_price);
					return new JSONObject(this);
				}
			}
			return null;
		}
		return null;
	}
	
}
