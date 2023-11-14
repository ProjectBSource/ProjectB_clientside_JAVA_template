package Indicators;

import java.util.ArrayList;
import java.util.List;

import ClientSocketControl.DataStructure;

public class AdvanceORDecline extends Indicator{

    private List<Double> advances ;
    private List<Double> declines ;
    private List<Double> closes;

    private int period;

    public AdvanceORDecline(){
        super.indicatorName  = "Advance/Decline";
        super.parametersAmount = 1;
    }

    public AdvanceORDecline(int period){
        super.indicatorName  = "Advance/Decline";
        super.parametersAmount = 1;
        this.advances = new ArrayList<>();
        this.declines = new ArrayList<>();
        this.closes = new ArrayList<>();
        this.period = period;
    }

    public void update(DataStructure dataStructure){
        super.dataStructure = dataStructure;
        closes.add(dataStructure.getClose());
        if(closes.size()>1){
            if(closes.get(closes.size()-1) - closes.get(closes.size()-2) > 0){
                advances.add(1);
                declines.add(0);
            }
            else if(closes.get(closes.size()-1) - closes.get(closes.size()-2) == 0){
                advances.add(0);
                declines.add(0);
            }else{
                advances.add(0);
                declines.add(1);
            }
        }
        if (closes.size() > period) {
            advances.remove(0);
            declines.remove(0);
            closes.remove(0);
        }
    }

    public double getAdvance(){
        if(advances.size()>0){
            return advances.get(advances.size()-1);
        }else{
            return 0;
        }
    }

    public double getDecline(){
        if(declines.size()>0){
            return declines.get(declines.size()-1);
        }else{
            return 0;
        }
    }

    public double getADRatio() {
        int dataLength = advances.size();
        double totalAdvances = 0;
        double totalDeclines = 0;
        
        if(dataLength==0) 
            return 0;

        // Calculate total advances and declines
        for (int i = 0; i < dataLength; i++) {
            totalAdvances += advances.get(i);
            totalDeclines += declines.get(i);
        }
        
        // Calculate A/D ratio
        double adr = totalAdvances / totalDeclines;
        
        // Return the A/D ratio
        return adr;
    }
}