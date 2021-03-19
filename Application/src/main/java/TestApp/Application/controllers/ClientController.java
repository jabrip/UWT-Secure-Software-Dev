package TestApp.Application.controllers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * A simple interface for interacting with the Virtual Vehicles API.
 * Uses a base url of http://localhost:5000.
 * Port number can be changed in application.properties under resources.
 */
@RestController
public class ClientController {

	/**
	 * EX: "http://localhost:5000"
	 * @return Welcome string
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public String defaultView() {
		return "Welcome to the Virtual Vehicles Interface App";
	}
	
    /**
     * EX: "http://localhost:5000/viewclients"
     * @return retString
     */
    @RequestMapping(value = "/viewclients", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public String viewClients() {
    	String output = "";
    	String retString = "Applications:<br/>----------------<br/>";
    	JSONArray myArr = null;
    	JSONObject obj = null;
    	
    	try {
    		URL url = new URL("http://localhost:8587/clients");
    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    				
    		conn.setRequestMethod("GET");
    	    conn.setRequestProperty("Accept", "application/json");
    	    BufferedReader br = new BufferedReader(new InputStreamReader(
    	                    (conn.getInputStream())));

    	    output = br.readLine();
    	            
    	    obj = new JSONObject(output); 
    	    br.close();
    	} catch(Exception e) {
    		System.out.println(e.toString());
    	}
    	
    	myArr = obj.getJSONObject("_embedded").getJSONArray("clients");
    		
    	for(int i = 0; i < myArr.length(); i++) {
    		JSONObject app = myArr.getJSONObject(i);
    		retString += clientToString(app) + "<br/>";

    	}
        return retString;
    }
    
    /**
     * EX: "http://localhost:5000/readclient?oid={ObjectID}"
     * @param theClient
     * @return retString
     */
    @GetMapping("/readclient")
    @ResponseStatus(HttpStatus.OK)
    public String readClient(@RequestParam(value = "oid")String oid) {
//    	if(myArr == null) {
//    		return "You must reload \"/viewclients\" page before proceeding";
//    	}
    	
    	String output = "";
    	JSONObject obj = null;
    	try {
    		URL url = new URL("http://localhost:8587/clients/" + oid);
    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    				
    		conn.setRequestMethod("GET");
    	    conn.setRequestProperty("Accept", "application/json");
    	    BufferedReader br = new BufferedReader(new InputStreamReader(
    	                    (conn.getInputStream())));

    	    output = br.readLine();
    	            
    	    obj = new JSONObject(output);
    	    br.close();
    	} catch(Exception e) {
    		System.out.println(e.toString());
    	}	
    	
//    	if(obj == null) {
//    		return "Error: Object not found";
//    	}
    	
		return clientToString(obj);
    }
    
    /**
     * EX: "http://localhost:5000/addclient?application={Name}&secret={Secret}"
     * @param application
     * @return
     */
    @GetMapping("/addclient")
    @ResponseStatus(HttpStatus.CREATED)
    public String addClient(@RequestParam(value = "application")String application, 
    		@RequestParam(value = "secret")String secret) {
    	String output = "";
    	JSONObject obj = null;
    	try {
    		URL url = new URL("http://localhost:8587/clients");
    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    				
    		conn.setRequestMethod("POST");
    		conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    	    conn.setRequestProperty("Accept", "application/json");
    	    conn.setDoOutput(true);
    	    
    	    JSONObject newApp = new JSONObject();
    	    newApp.put("application", application);
    	    newApp.put("secret", secret);
    	    
    	    OutputStream os = conn.getOutputStream();
    	    os.write(newApp.toString().getBytes("UTF-8"));
    	    os.close();
    	    
    	    BufferedReader br = new BufferedReader(new InputStreamReader(
    	                    (conn.getInputStream())));

    	    output = br.readLine();
    	            
    	    obj = new JSONObject(output); 
    	    br.close();
    	} catch(Exception e) {
    		System.out.println(e.toString());
    	}
    	
    	return clientToString(obj);
    }
    
    /**
     * EX: "http://localhost:5000/deleteclient?oid={ObjectID}"
     * @param oid
     * @return retString
     */
    @GetMapping("/deleteclient")
    @ResponseStatus(HttpStatus.OK)
    public String deleteClient(@RequestParam(value = "oid")String oid) {
    	
    	
    	try {
    		URL url = new URL("http://localhost:8587/clients/" + oid);
    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    		
    	    conn.setRequestProperty("Accept", "application/json");
    	    conn.setDoOutput(true);		
    		conn.setRequestMethod("DELETE");
    	    //conn.connect();
    		System.out.println(conn.getResponseCode());
    	    
    	} catch(Exception e) {
    		System.out.println(e.toString());
    	}
    	
//    	if(obj == null) {
//    		return "Error: Object not found";
//    	}
//    	
		return "Client Successfully deleted.";
    }
    
    /**
     * EX: "http://localhost:5000/ping"
     * @return retString
     */
    @GetMapping("/ping")
    @ResponseStatus(HttpStatus.OK)
    public String ping() {
    	String retString = "";
    	try {
    		URL url = new URL("http://localhost:8587/clients/utils/ping");
    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    		
    	    conn.setRequestProperty("Accept", "application/json");	
    		conn.setRequestMethod("GET");
    		if(conn.getResponseCode() == 200) {
    			retString = "Authentication service is currently running";
    		}
    	} catch(Exception e) {
    		System.out.println(e.toString());
    		retString = "Warning: Cannot connect to Client Authentication Service";
    	}
    	return retString;
    }
    
    /**
     * 
     * @param theClient
     * @return retString
     */
    private String clientToString(JSONObject theClient) {
    	String retString = "";
    	retString += "Application Name: " + theClient.getString("application");
    	retString += "<br/>Secret: " + theClient.getString("secret");
    	retString += "<br/>API Key: " + theClient.getString("apiKey");
    	retString += "<br/>Object ID: " + theClient.getString("id");
    	retString += "<br/>Create At: " + theClient.getString("createdAt");
    	retString += "<br/>Updated At: " + theClient.getString("updatedAt") + "<br/>";
    	return retString;
    }
}
