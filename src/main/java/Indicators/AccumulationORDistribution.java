package Indicators;

import ClientSocketControl.DataStructure;
import java.util.ArrayList;
import java.util.List;

public class AccumulationORDistribution extends Indicator{

    private List<Double> highs;
    private List<Double> lows;
    private List<Double> closes;
    private List<Integer> volumes;

    private int period;

    public AccumulationORDistribution(){
        super.indicatorName  = "AccumulationORDistribution";
        super.parametersAmount = 1;
    }

    public AccumulationORDistribution(int period){
        super.indicatorName  = "AccumulationORDistribution";
        super.parametersAmount = 1;
        this.highs = new ArrayList<>();
        this.lows = new ArrayList<>();
        this.closes = new ArrayList<>();
        this.volumes = new ArrayList<>();
        this.period = period;
    }

    public void update(DataStructure dataStructure){
        if(closes.size()==0){
            closes.add(dataStructure.getIndex());
            highs.add(dataStructure.getHigh());
            lows.add(dataStructure.getLow());
            volumes.add(dataStructure.getTotal_volume());
        }
        if(dataStructure.getType().equals("tick")){
            super.dataStructure = dataStructure;
            highs.set(highs.size()-1, dataStructure.getHigh());
            lows.set(lows.size()-1, dataStructure.getLow());
            closes.set(closes.size()-1, dataStructure.getIndex());
            volumes.set(volumes.size()-1, dataStructure.getTotal_volume());
        }
        else if(dataStructure.getType().equals("interval")){
            closes.add(dataStructure.getClose());
            highs.add(dataStructure.getHigh());
            lows.add(dataStructure.getLow());
            volumes.add(dataStructure.getTotal_volume());
            if (closes.size() > period) {
                highs.remove(0);
                lows.remove(0);
                closes.remove(0);
                volumes.remove(0);
            }
        }
    }

    public double getAD() {
        int dataLength = closes.size();
        
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
