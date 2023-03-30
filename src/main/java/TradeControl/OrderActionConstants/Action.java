package TradeControl.OrderActionConstants;

public enum Action {
	BUY("BUY"),
    SELL("SELL"),
	CLOSE("CLOSE");

    private String action;

    private Action(String action) {
        this.action = action;
    }
    
    public String getAction() {
    	return action;
    }
}
