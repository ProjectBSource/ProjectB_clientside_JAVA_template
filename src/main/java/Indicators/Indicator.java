package Indicators;

public class Indicator {
    public String indicatorName = null;
    public int parametersAmount = 0;
    public DataStructure dataStructure = null;
    
    public Double getIndex(){
        if(dataStructure!=null){
            return dataStructure.getIndex();
        }
        return 0;
    }
    public Double getOpen(){
        if(dataStructure!=null){
            return dataStructure.getOpen();
        }
        return 0;
    }
    public Double getHigh() {
        if(dataStructure!=null){
            return dataStructure.getHigh();
        }
        return 0;
	}
	public Double getLow() {
        if(dataStructure!=null){
            return dataStructure.getLow();
        }
        return 0;
	}
	public Double getClose() {
        if(dataStructure!=null){
            return dataStructure.getClose();
        }
        return 0;
	}
	public Integer getVolume() {
        if(dataStructure!=null){
            return dataStructure.getVolume();
        }
        return 0;
	}
	public Integer getTotalVolume() {
        if(dataStructure!=null){
            return dataStructure.getTotal_volume;
        }
        return 0;
	}
}
