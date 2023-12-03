package ClientSocketControl;

public class DataStructure {
	String dataSourceID = null;
	String type = null;
	String symbol = null;
	String market = null;
	Integer strike_price = 0;
    String direction = null;
	String date = null;
	String time = null;
	String datetime = null;
	Double index = 0.0;
	Double open = 0.0;
	Double high = 0.0;
	Double low = 0.0;
	Double close = 0.0;
	Integer volume = 0;
	Integer total_volume = 0;
	String expiration_year_month = null;
	String error = null;
	String done = null;
	
	public String getDataSourceID() {
		return dataSourceID;
	}
	public void setDataSourceID(String dataSourceID) {
		this.dataSourceID = dataSourceID;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getMarket() {
		return market;
	}
	public void setMarket(String market) {
		this.market = market;
	}
	public Integer getStrike_price() {
		return strike_price;
	}
	public void setStrike_price(Integer strike_price) {
		this.strike_price = strike_price;
	}
    public String getDirection(){
        return direction;
    }
    public void setDirection(String direction){
        this.direction = direction;
    }
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getDatetime() {
		return datetime;
	}
	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}
	public Double getIndex() {
		return index;
	}
	public void setIndex(Double index) {
		this.index = index;
	}
	public Double getOpen() {
		return open;
	}
	public void setOpen(Double open) {
		this.open = open;
	}
	public Double getHigh() {
		return high;
	}
	public void setHigh(Double high) {
		this.high = high;
	}
	public Double getLow() {
		return low;
	}
	public void setLow(Double low) {
		this.low = low;
	}
	public Double getClose() {
		return close;
	}
	public void setClose(Double close) {
		this.close = close;
	}
	public Integer getVolume() {
		return volume;
	}
	public void setVolume(Integer volume) {
		this.volume = volume;
	}
	public Integer getTotal_volume() {
		return total_volume;
	}
	public void setTotal_volume(Integer total_volume) {
		this.total_volume = total_volume;
	}
	public String getExpiration_year_month() {
		return expiration_year_month;
	}
	public void setExpiration_year_month(String expiration_year_month) {
		this.expiration_year_month = expiration_year_month;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getDone() {
		return done;
	}
	public void setDone(String done) {
		this.done = done;
	}
}
