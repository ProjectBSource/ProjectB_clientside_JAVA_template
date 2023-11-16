package Indicators;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

import ClientSocketControl.DataStructure;

public class AdvanceORDecline extends Indicator{

    private List<Integer> advances ;
    private List<Integer> declines ;
    private List<Double> closes;

    private int period;

    public AdvanceORDecline(){
        super.indicatorName  = "AdvanceORDecline";
        super.parametersAmount = 1;
    }

    public AdvanceORDecline(int period){
        super.indicatorName  = "AdvanceORDecline";
        super.parametersAmount = 1;
        this.advances = new ArrayList<>();
        this.declines = new ArrayList<>();
        this.closes = new ArrayList<>();
        this.period = period;
    }

    public String getOutput(){
        dataDetail = new JSONObject();
        dataDetail.put("getAdvanceSum", getAdvanceSum()+"");
        dataDetail.put("getDeclineSum", getDeclineSum()+"");
        dataDetail.put("getADRatio", getADRatio()+"");
        return dataDetail.toString();
    }

    public void update(DataStructure dataStructure){
        if(closes.size()==0){
            closes.add(dataStructure.getIndex());
            advances.add(0);
            declines.add(0);
        }
        if(dataStructure.getType().equals("tick")){
            super.dataStructure = dataStructure;
            closes.set(closes.size()-1, dataStructure.getIndex());
            if(closes.size()>1){
                if(closes.get(closes.size()-1) - closes.get(closes.size()-2) > 0){
                    advances.set(advances.size()-1, 1);
                    declines.set(declines.size()-1, 0);
                }
                else if(closes.get(closes.size()-1) - closes.get(closes.size()-2) == 0){
                    advances.set(advances.size()-1, 0);
                    declines.set(declines.size()-1, 0);
                }else{
                    advances.set(advances.size()-1, 0);
                    declines.set(declines.size()-1, 1);
                }
            }
        }
        else if(dataStructure.getType().equals("interval")){
            closes.add(dataStructure.getClose());
            if (closes.size() > period) {
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
                advances.remove(0);
                declines.remove(0);
                closes.remove(0);
            }
        }
    }

    public int getAdvanceSum(){
        if(advances.size()>0){
            int sumup = 0;
            for(int advance : advances){
                sumup += advance;
            }
            return sumup;
        }else{
            return 0;
        }
    }

    public int getDeclineSum(){
        if(declines.size()>0){
            int sumup = 0;
            for(int decline : declines){
                sumup += decline;
            }
            return sumup;
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
