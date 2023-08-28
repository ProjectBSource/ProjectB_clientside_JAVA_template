package Indicators;

import java.util.ArrayList;
import java.util.List;

public class BollingerBands extends Indicator{
    
    private List<Double> prices;
    private int period;
    private double multiplier;
    
    public BollingerBands(double period, double multiplier) {
        super.name = "Bollinger Bands";
        super.parametersAmount = 2;
        this.prices = new ArrayList<>();
        this.period = period;
        this.multiplier = multiplier;
    }
    
    public void addPrice(double price) {
        prices.add(price);
        if (prices.size() > period) {
            prices.remove(0);
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
    
    private double getSMA() {
        if (prices.size() < period) {
            return 0;
        }
        double sum = 0.0;
        for (double price : prices) {
            sum += price;
        }
        return sum / period;
    }
    
    private double getStdDev() {
        if (prices.size() < period) {
            return 0;
        }
        double sma = getSMA();
        double sum = 0.0;
        for (double price : prices) {
            double diff = price - sma;
            sum += diff * diff;
        }
        double variance = sum / period;
        return Math.sqrt(variance);
    }
}
