package Indicators;

import java.util.ArrayList;
import java.util.List;

import ClientSocketControl.DataStructure;

public class AccumulationORDistribution extends Indicator{

    private List<Double> highs;
    private List<Double> lows;
    private List<Double> closes;
    private List<Double> volumes;

    private int period;

    public AccumulationORDistribution(){
        super.indicatorName  = "Accumulation/Distribution";
        super.parametersAmount = 1;
    }

    public AccumulationORDistribution(int period){
        super.indicatorName  = "Accumulation/Distribution";
        super.parametersAmount = 1;
        this.highs = new ArrayList<>();
        this.lows = new ArrayList<>();
        this.closes = new ArrayList<>();
        this.volumes = new ArrayList<>();
        this.period = period;
    }

    public void update(DataStructure dataStructure){
        super.dataStructure = dataStructure;
        highs.add(dataStructure.getHigh());
        lows.add(dataStructure.getLow());
        closes.add(dataStructure.getClose());
        volumes.add(dataStructure.getTotal_volume());
        if (closes.size() > period) {
            highs.remove(0);
            lows.remove(0);
            closes.remove(0);
            volumes.remove(0);
        }
    }

    public double getAD() {
        int dataLength = highs.size();
        
        if(dataLength==0) 
            return 0;

        double[] adValues = new double[dataLength];
        
        // Calculate A/D values
        for (int i = 0; i < dataLength; i++) {
            double ad = ((closes.get(i) - lows.get(i)) - (highs.get(i) - closes.get(i))) / (highs.get(i) - lows.get(i)) * volumes.get(i);
            if (i > 0) {
                ad += adValues[i - 1];
            }
            adValues[i] = ad;
        }
        
        // Return the last A/D value
        return adValues[dataLength - 1];
    }
}