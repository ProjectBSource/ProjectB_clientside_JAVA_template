package Indicators;

public class ArnaudLegouxMovingAverage extends Indicator{

    private List<Double> closes;

    private int period;
    private double offset;
    private double sigma;

    public ArnaudLegouxMovingAverage(){
        super.indicatorName  = "Arnaud Legoux Moving Average";
        super.parametersAmount = 3;
    }

    public ArnaudLegouxMovingAverage(int period){
        super.indicatorName  = "Arnaud Legoux Moving Average";
        super.parametersAmount = 3;
        this.closes = new ArrayList<>();
        this.period = period;
    }

    public void update(DataStructure dataStructure){
        super.dataStructure = dataStructure;
        closes.add(dataStructure.getClose());
        if (closes.size() > period) {
            closes.remove(0);
        }
    }

    public double getALMA() {
        int dataLength = closes.size();
        double[] almaValues = new double[dataLength];

        if(dataLength==0) 
            return 0;
        
        // Calculate ALMA values
        for (int i = period - 1; i < dataLength; i++) {
            double sum = 0;
            double divisor = 0;
            
            for (int j = 0; j < period; j++) {
                double weight = Math.exp(-1 * (Math.pow(j, 2) / (2 * Math.pow(sigma, 2))));
                sum += weight * closes.get(i - period + 1 + j);
                divisor += weight;
            }
            
            almaValues[i] = sum / divisor;
        }
        
        // Return the last ALMA value
        return almaValues[dataLength - 1];
    }
}