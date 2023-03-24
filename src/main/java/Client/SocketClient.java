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
	private int serverPort = -1;
	private Socket client;
	private BufferedWriter bw;
	private String messageFromServer;
	private BufferedReader br;
	private String apiAccessCode;
	private String clientID;
	private ArrayList<JSONObject> JSONresponse = new ArrayList<JSONObject>();
	private InputStream is;
	private OutputStream os;
	
	public SocketClient(String loginname, String password) throws Exception {
		/*
		JSONObject obj = new JSONObject();
		obj.put("clientID", loginname);
		obj.put("password", password);
		JSONObject result = postRequest("https://www.projectb.click/ProjectB/APIgetAccessCode.php", obj.toString());
		if(result.has("type") && result.getString("type").equals("error")) {
			throw new Exception(result.getString("message"));
		}else {
			this.clientID = result.getString("clientID");
			this.apiAccessCode = result.getString("accessCode");
			System.out.println("Login successful");
			getTheServerIPaddress();
		}
		*/
		this.clientID = "636e7543ea44b";
		this.apiAccessCode = "65839231";
		this.serverIPaddress = "localhost";
		this.serverPort = 1101;
	}
	
	public void getTheServerIPaddress() throws Exception {
		JSONObject obj = new JSONObject();
		obj.put("clientID", clientID);
		obj.put("apiAccessCode", apiAccessCode);
		JSONObject result = postRequest("https://www.projectb.click/ProjectB/GetTheServerIPaddress.php", obj.toString());
		if(result.has("type") && result.getString("type").equals("error")) {
			throw new Exception(result.getString("message"));
		}else {
			this.serverIPaddress = result.getString("ipaddress");
			this.serverPort = result.getInt("port");
			System.out.println("Server "+serverIPaddress+", Port "+serverPort+" available");
		}
	}
	
	public void request(JSONObject request) throws Exception {
		if(clientID==null || apiAccessCode==null) {
			throw new Exception("No available API access code");
		}else {
			this.JSONrequest = request;
			JSONrequest.put("clientID", clientID);
			JSONrequest.put("accessCode", apiAccessCode);
			if(client!=null && client.isClosed()==false) {
				//send request from server
				sendResquestToServer(JSONrequest);
			}else {
				//Run program
				Thread thread = new Thread(this);
				thread.start();
			}
		}
	}
	
	@Override
	public void run() {
		try {
			//connect to server
			client = new Socket(serverIPaddress, serverPort);
			
			//send request from server
			sendResquestToServer(JSONrequest);
			
			//get response from server
			JSONresponse = new ArrayList<JSONObject>();
			boolean stopstreaming = false;
			while(!stopstreaming) {
				if(!client.isConnected()) {
					throw new Exception("Lost server connection, please contact admin");
				}
				is = client.getInputStream();
				br = new BufferedReader(new InputStreamReader(is));
				while((messageFromServer = br.readLine())!=null) {
					if(messageFromServer.contains("done")) {
						JSONresponse.add(new JSONObject().put("done", " "));
						stopstreaming = true;
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
	
	private void sendResquestToServer(JSONObject request) throws IOException {
		//send request from server
		is = client.getInputStream();
		os = client.getOutputStream();
		bw = new BufferedWriter(new OutputStreamWriter(os));
		bw.write(request.toString());
		bw.flush();
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
			if(temp!=null) {
				return temp;
			}
		}
		return new JSONObject();
	}
}
