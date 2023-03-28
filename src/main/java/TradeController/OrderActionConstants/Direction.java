package TradeController.OrderActionConstants;

public enum Direction {
    CALL("CALL"),
    PUT("PUT");

    public String direction;

    private Direction(String action) {
        this.direction = action;
    }
    
    public String getDirection() {
    	return direction;
    }
}
