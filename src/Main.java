import ClientSocketControl.DataStructure;
import Indicators.BollingerBands;
import Main.MainController;
import org.json.JSONObject;


public class Main extends MainController {

    BollingerBands bollingerBands = new BollingerBands(20, 2);
	public static void main(String args[]) throws Exception {
		Main main = new Main();
		main.initialSetup();
		try {
			main.run();
		}catch(Exception e) {
			System.out.println(e);
		}
	}
	
	public void initialSetup() throws Exception {
		login("funganything@gmail.com", "123");
		
		JSONObject dataStreamingRequest = new JSONObject();
		dataStreamingRequest.put("activity", "IntervalDataStreaming");
		dataStreamingRequest.put("market", "Future");
		dataStreamingRequest.put("index", "HSI");
		dataStreamingRequest.put("startdate", "20230101");
		dataStreamingRequest.put("enddate", "20230531");
		dataStreamingRequest.put("starttime", "000000");
		dataStreamingRequest.put("endtime", "235959");
		dataStreamingRequest.put("interval", 60-1);
        dataStreamingRequest.put("mitigateNoiseWithinPrecentage", 200);
        
		createDataStreamingRequest(dataStreamingRequest);
		
		projectBTradeController(0.0005);
	}

	@Override
	public void logicHandler(DataStructure dataStructure) {
        bollingerBands.addPrice(dataStructure.getIndex());

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
                bollingerBands.getUpperBand(),
                bollingerBands.getMiddleBand(),
                bollingerBands.getLowerBand()
            ) 
        );
	}
}
