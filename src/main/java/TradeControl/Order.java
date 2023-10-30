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
import TradeControl.OrderActionConstants.ExpiryDate;
import TradeControl.OrderActionConstants.StrikePrice;

import DataController.Constants;

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
	public JSONObject orderDetailInJSON;
	private ArrayList<Order> history = new ArrayList<>();

	public Order(DataStructure dataStructure, Action action, int quantity) throws Exception {
		this.symbol = dataStructure.getSymbol();
		this.orderid = UUID.randomUUID().toString();
		this.orderDateTime = Constants.df_yyyyMMddkkmmss.parse(dataStructure.getDatetime());
		this.action = action;
		this.quantity = quantity;
		this.remained = quantity;
		this.lastUpdateDateTime = this.orderDateTime;
		orderDetailInJSON = new JSONObject();
		orderDetailInJSON.put("symbol", this.symbol);
		orderDetailInJSON.put("orderid", this.orderid);
		orderDetailInJSON.put("orderDateTime", this.orderDateTime);
		orderDetailInJSON.put("action", this.action.getAction());
		orderDetailInJSON.put("quantity", this.quantity);
		orderDetailInJSON.put("remained", this.remained);
		orderDetailInJSON.put("lastUpdateDateTime", this.lastUpdateDateTime);
		this.history.add(new Order(this));
	}
	
	public Order(String symbol, Action action, Direction direction, StrikePrice sp, ExpiryDate ed,  int quantity) {
		this.symbol = symbol;
		this.orderid = UUID.randomUUID().toString();
		this.orderDateTime = new Date();
		this.action = action;
		this.direction = direction;
		this.sp = sp;
		this.ed = ed;
		this.quantity = quantity;
		this.remained = quantity;
		this.lastUpdateDateTime = this.orderDateTime;
		orderDetailInJSON = new JSONObject();
		orderDetailInJSON.put("symbol", this.symbol);
		orderDetailInJSON.put("orderid", this.orderid);
		orderDetailInJSON.put("orderDateTime", this.orderDateTime);
		orderDetailInJSON.put("action", this.action.getAction());
		orderDetailInJSON.put("direction", this.direction.getDirection());
		orderDetailInJSON.put("sp", this.sp.getStrikePrice());
		orderDetailInJSON.put("ed", this.ed.getExpiryDate());
		orderDetailInJSON.put("quantity", this.quantity);
		orderDetailInJSON.put("remained", this.remained);
		orderDetailInJSON.put("lastUpdateDateTime", this.lastUpdateDateTime);
		this.history.add(new Order(this));
	}
	
	private Order(Order order) {
		this.symbol = order.symbol;
		this.orderid = order.orderid;
		this.orderDateTime = order.orderDateTime;
		this.action = order.action;
		this.direction = order.direction;
		this.sp = order.sp;
		this.ed = order.ed;
		this.quantity = order.quantity;
		this.traded = order.traded;
		this.remained = order.remained;
		this.averageTradePrice = order.averageTradePrice;
		this.lastUpdateDateTime = order.lastUpdateDateTime;
		this.orderDetailInJSON = order.orderDetailInJSON;
	}

	public JSONObject trade(Profile profile, DataStructure data, double slippage) throws Exception {
		if(data.getType().equals("interval")==false) {
			if(direction==null && sp==null && ed==null) {
				if(remained>0) {
					int temp_trade_amount = (data.getVolumn()>=remained)?remained:data.getVolumn();
					traded += temp_trade_amount;
					remained -= temp_trade_amount;
					double temp_trade_price = data.getIndex() + ( (data.getIndex() *  slippage) * (random.nextInt(2)==0?1:-1) );
					averageTradePrice = (averageTradePrice + (temp_trade_amount * temp_trade_price)) / traded;
					Order temp_market = new Order(this);
					history.add(temp_market);
					//Update profle
					if(action == Action.SELL) { temp_trade_amount *= -1; }
					profile.update(symbol, temp_trade_amount, temp_trade_price);

                    return orderDetailInJSON;
				}
			}
			return null;
		}
		return null;
	}
	
}
