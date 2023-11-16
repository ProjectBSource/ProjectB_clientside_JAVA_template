package Indicators;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

import ClientSocketControl.DataStructure;

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

    public String getOutput(){
        dataDetail = new JSONObject();
        dataDetail.put("getASI", getASI()+"");
        return dataDetail.toString();
    }

    public void update(DataStructure dataStructure){
        if(closes.size()==0){
            closes.add(dataStructure.getIndex());
            highs.add(dataStructure.getHigh());
            lows.add(dataStructure.getLow());
        }
        if(dataStructure.getType().equals("tick")){
            super.dataStructure = dataStructure;
            highs.set(highs.size()-1, dataStructure.getHigh());
            lows.set(lows.size()-1, dataStructure.getLow());
            closes.set(closes.size()-1, dataStructure.getIndex());
        }
        else if(dataStructure.getType().equals("interval")){
            closes.add(dataStructure.getClose());
            highs.add(dataStructure.getHigh());
            lows.add(dataStructure.getLow());
            if (closes.size() > period) {
                highs.remove(0);
                lows.remove(0);
                closes.remove(0);
            }
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
