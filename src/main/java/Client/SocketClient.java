package Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONObject;

public class SocketClient implements Runnable {

	private JSONObject JSONrequest;
	private String serverIPaddress;
	private int serverPort = 8888;
	private Socket client;
	private BufferedWriter bw;
	private String messageFromServer;
	private BufferedReader br;
	private String apiAccessCode;
	private String clientID;
	private ArrayList<JSONObject> JSONresponse = new ArrayList<JSONObject>();
	
	public SocketClient(String loginname, String password) throws Exception {
		JSONObject obj = new JSONObject();
		obj.put("clientID", loginname);
		obj.put("password", password);
		JSONObject result = postRequest("https://www.projectb.click/ProjectB/APIgetAccessCode.php", obj.toString());
		if(result.has("type") && result.getString("type").equals("error")) {
			throw new Exception(result.getString("message"));
		}else {
			this.clientID = result.getString("clientID");
			this.apiAccessCode = result.getString("accessCode");
			getTheServerIPaddress();
		}
	}
	
	public void getTheServerIPaddress() throws Exception {
		JSONObject result = postRequest("https://www.projectb.click/ProjectB/GetTheServerIPaddress.php", null);
		if(result.has("type") && result.getString("type").equals("error")) {
			throw new Exception(result.getString("message"));
		}else {
			this.serverIPaddress = result.getString("ipaddress");
		}
	}
	
	public void request(JSONObject request) throws Exception {
		if(clientID==null || apiAccessCode==null) {
			throw new Exception("No available API access code");
		}else {
			this.JSONrequest = request;
			JSONrequest.put("clientID", clientID);
			JSONrequest.put("accessCode", apiAccessCode);
			//Run program
			Thread thread = new Thread(this);
			thread.start();
		}
	}
	
	@Override
	public void run() {
		try {
			//connect to server
			client = new Socket(serverIPaddress, serverPort);
			
			//send request from server
			InputStream is = client.getInputStream();
			OutputStream os = client.getOutputStream();
			bw = new BufferedWriter(new OutputStreamWriter(os));
			bw.write(JSONrequest.toString());
			bw.flush();
			
			//get response from server
			JSONresponse = new ArrayList<JSONObject>();
			while(true) {
				br = new BufferedReader(new InputStreamReader(is));
				while((messageFromServer = br.readLine())!=null) {
					if(messageFromServer.contains("done")) {
						JSONresponse.add(null);
						break; 
					}
					JSONresponse.add(new JSONObject(messageFromServer));
				}
			}
		} catch (java.io.IOException e) {
			System.out.println("Socket連線有問題 !");
			System.out.println("IOException :" + e.toString());
		} catch (Exception e) {
			System.out.println("Exception :" + e.toString());
		} 
		finally {
			try {
				bw.close();
				client.close();
			} catch (IOException e) {}
		}
	}
	
	private JSONObject postRequest(String url, String message) throws IOException {
		URL obj = new URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0");

        // For POST only - START
        if(message!=null) {
	        httpURLConnection.setDoOutput(true);
	        OutputStream os = httpURLConnection.getOutputStream();
	        os.write(message.getBytes());
	        os.flush();
	        os.close();
        }
        // For POST only - END

        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in .readLine()) != null) {
                response.append(inputLine);
            } in .close();

            // print result
            return new JSONObject(response.toString());
        } else {
            System.out.println("POST request not worked");
        }
        
        return null;
	}
	
	public JSONObject getResponse() {
		if(JSONresponse.size()>0) {
			JSONObject temp = JSONresponse.get(0);
			JSONresponse.remove(0);
			return temp;
		}
		else{
			return new JSONObject();
		}
	}
}
