package TradeController;

import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Constants {
	public String orderid;
	public Date orderDateTime;
	public static ObjectMapper mapper;
	public static String status_OPEN = "OPEN";
	public static String status_PARTIAL_FILL = "PARTIAL_FILL";
	public static String status_CLOSE = "CLOSE";
}
