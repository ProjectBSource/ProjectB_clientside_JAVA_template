package TradeControl.OrderActionConstants;

public enum Action {
	BUY("BUY"),
    SELL("SELL"),
	OFF("OFF");

    private String action;

    private Action(String action) {
        this.action = action;
    }
    
    public String getAction() {
    	return action;
    }
}
