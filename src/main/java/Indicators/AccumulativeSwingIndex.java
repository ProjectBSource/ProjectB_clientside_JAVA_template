package Indicators;

import ClientSocketControl.DataStructure;
import java.util.ArrayList;
import java.util.List;

public class AccumulativeSwingIndex extends Indicator{

    private List<Double> highs;
    private List<Double> lows;
    private List<Double> closes;

    private int period;

    public AccumulativeSwingIndex(){
        super.indicatorName  = "Accumulative Swing Index";
        super.parametersAmount = 1;
    }

    public AccumulativeSwingIndex(int period){
        super.indicatorName  = "Accumulative Swing Index";
        super.parametersAmount = 1;
        this.highs = new ArrayList<>();
        this.lows = new ArrayList<>();
        this.closes = new ArrayList<>();
        this.period = period;
    }

    public void update(DataStructure dataStructure){
        super.dataStructure = dataStructure;
        highs.add(dataStructure.getHigh());
        lows.add(dataStructure.getLow());
        closes.add(dataStructure.getClose());
        if (closes.size() > period) {
            highs.remove(0);
            lows.remove(0);
            closes.remove(0);
        }
    }

    public double getASI() {
        int dataLength = highs.size();
        double[] asiValues = new double[dataLength];

        if(dataLength==0) 
            return 0;
        
        // Calculate ASI values
        for (int i = 0; i < dataLength; i++) {
            double tr = Math.max(highs.get(i) - closes.get(i - 1), Math.max(highs.get(i) - lows.get(i), lows.get(i) - closes.get(i - 1)));
            double r = Math.abs(closes.get(i) - closes.get(i - 1));
            double k = Math.max(highs.get(i) - closes.get(i - 1), lows.get(i) - closes.get(i - 1));
            double si = 50 * ((closes.get(i) - closes.get(i - 1)) + 0.5 * (closes.get(i) - lows.get(i - 1)) + 0.25 * (closes.get(i - 1) - highs.get(i - 1))) * r / (tr * k);
            if (i > 0) {
                asiValues[i] = asiValues[i - 1] + si;
            } else {
                asiValues[i] = si;
            }
        }
        
        // Return the last ASI value
        return asiValues[dataLength - 1];
    }
}