import ClientSocketControl.DataStructure;
import Indicators.BollingerBands;
import Main.MainController;
import org.json.JSONObject;


public class Main extends MainController {

    BollingerBands indicator0 = new BollingerBands(20, 2);
    public static void main(String args[]) throws Exception {
        Main main = new Main();
        main.initialSetup();
        try {
            main.run();
            System.out.println(main.getOrderHistoryInJSON());
        }catch(Exception e) {
            System.out.println(e);
        }
    }
    
    public void initialSetup() throws Exception {
        login("funganything@gmail.com", "123");
        
        JSONObject dataStreamingRequest = new JSONObject();
        dataStreamingRequest.put("activity", "TickDataStreaming");
        dataStreamingRequest.put("market", "FUTURE");
        dataStreamingRequest.put("index", "HSI");
        dataStreamingRequest.put("startdate", "20230103");
        dataStreamingRequest.put("enddate", "20230103");
        dataStreamingRequest.put("starttime", "091500");
        dataStreamingRequest.put("endtime", "091600");
        dataStreamingRequest.put("interval", 60-1);
        dataStreamingRequest.put("mitigateNoiseWithinPrecentage", 100);
        
        createDataStreamingRequest(dataStreamingRequest);
        
        setSlippage(0.0005);
    }

    @Override
    public void logicHandler(DataStructure dataStructure) {
        indicator0.addPrice(dataStructure.getIndex());

        System.out.println( 
            String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                dataStructure.getType(),
                dataStructure.getDatetime(),
                dataStructure.getIndex(),
                dataStructure.getVolumn(),
                dataStructure.getOpen(),
                dataStructure.getHigh(),
                dataStructure.getLow(),
                dataStructure.getClose(),
                dataStructure.getTotal_volumn(),
                indicator0.getUpperBand(),
                indicator0.getMiddleBand(),
                indicator0.getLowerBand()
            ) 
        );

        if(dataStructure.getType().equals("tick")){ indicator0.update(dataStructure); }


        boolean baseLogicResult0 = ( indicator0.getPrice()>0 && indicator0.getUpperBand() > 0 && indicator0.getPrice() > indicator0.getUpperBand()  );
        boolean baseLogicResult1 = ( indicator0.getPrice()>0 && indicator0.getLowerBand() > 0 && indicator0.getPrice() < indicator0.getLowerBand()  );
        boolean baseLogicResult2 = ( indicator0.getPrice()>0 && indicator0.getMiddleBand() > 0 && indicator0.getPrice() == indicator0.getMiddleBand()  );


        try{
            if(dataStructure.getType().equals("tick")){ 
                if(baseLogicResult0==true){ 
                    placeOrder("#1", dataStructure, action.BUY, 1, false); 
                }
                if(baseLogicResult1==true){ 
                    placeOrder("#2", dataStructure, action.SELL, 1, false); 
                }
                if(baseLogicResult2==true){ 
                    placeOFFOrder("#1", dataStructure); 
                }
                if(baseLogicResult2==true){ 
                    placeOFFOrder("#2", dataStructure); 
                }
            }
        }catch(Exception e){}
    }
}
