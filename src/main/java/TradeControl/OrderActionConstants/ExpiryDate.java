package TradeControl.OrderActionConstants;

public enum ExpiryDate {
	_200001("200001"),
	_200002("200002");

    public String expirtyDate;

    private ExpiryDate(String expiryDate) {
        this.expirtyDate = expirtyDate;
    }
    
    public String getExpiryDate() {
    	return expirtyDate;
    }
}
