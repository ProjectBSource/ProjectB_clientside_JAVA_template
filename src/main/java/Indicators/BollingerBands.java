package Indicators;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

import ClientSocketControl.DataStructure;

public class BollingerBands extends Indicator{
    
    private List<Double> closes;

    private int period;
    private double multiplier;

    public BollingerBands(){
        super.indicatorName  = "Bollinger Bands";
        super.parametersAmount = 2;
    }
    
    public BollingerBands(int period, double multiplier) {
        super.indicatorName  = "Bollinger Bands";
        super.parametersAmount = 2;
        this.closes = new ArrayList<>();
        this.period = period;
        this.multiplier = multiplier;
    }

    public String getOutput(){
        dataDetail = new JSONObject();
        dataDetail.put("getUpperBand", getUpperBand());
        dataDetail.put("getMiddleBand", getMiddleBand());
        dataDetail.put("getLowerBand", getLowerBand());
        dataDetail.put("getSMA", getSMA());
        dataDetail.put("getStdDev", getStdDev());
        return dataDetail.toString();
    }
    
    public void update(DataStructure dataStructure) {
        if(closes.size()==0){
            closes.add(dataStructure.getIndex());
        }
        if(dataStructure.getType().equals("tick")){
            super.dataStructure = dataStructure;
            closes.set(closes.size()-1, dataStructure.getIndex());
        }
        else if(dataStructure.getType().equals("interval")){
            closes.add(dataStructure.getClose());
            if (closes.size() > period) {
                closes.remove(0);
            }
        }
    }

    public double getUpperBand() {
        double sma = getSMA();
        double stdDev = getStdDev();
        return sma + (stdDev * multiplier);
    }
    
    public double getMiddleBand() {
        return getSMA();
    }
    
    public double getLowerBand() {
        double sma = getSMA();
        double stdDev = getStdDev();
        return sma - (stdDev * multiplier);
    }
    
    public double getSMA() {
        if (closes.size() < period) {
            return 0;
        }
        double sum = 0.0;
        for (double price : closes) {
            sum += price;
        }
        return sum / period;
    }
    
    public double getStdDev() {
        if (closes.size() < period) {
            return 0;
        }
        double sma = getSMA();
        double sum = 0.0;
        for (double price : closes) {
            double diff = price - sma;
            sum += diff * diff;
        }
        double variance = sum / period;
        return Math.sqrt(variance);
    }
}
