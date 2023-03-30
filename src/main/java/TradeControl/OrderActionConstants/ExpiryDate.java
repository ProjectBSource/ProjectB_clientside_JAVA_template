package TradeControl.OrderActionConstants;

public enum ExpiryDate {
	_200001("200001"),
	_200002("200002");

    public String expirtyDate;

    private ExpiryDate(String expirtyDate) {
        this.expirtyDate = expirtyDate;
    }
    
    public String getDirection() {
    	return expirtyDate;
    }
}
