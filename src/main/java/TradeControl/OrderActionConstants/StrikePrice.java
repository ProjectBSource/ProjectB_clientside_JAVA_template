package TradeControl.OrderActionConstants;

public enum StrikePrice {
	_100(100),
    _200(200);

    public int strikePrice;

    private StrikePrice(int strikePrice) {
        this.strikePrice = strikePrice;
    }
    
    public int getDirection() {
    	return strikePrice;
    }
}
