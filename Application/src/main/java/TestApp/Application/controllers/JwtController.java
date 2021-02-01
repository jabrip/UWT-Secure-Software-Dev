package TestApp.Application.controllers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
*
* A simple interface for interacting with the Virtual Vehicles API.
* Uses a base url of http://localhost:5000.
* Port number can be changed in application.properties under resources.
*/

@RestController
public class JwtController {
	
	
	/**
     * EX: "http://localhost:5000/jwts?apiKey={apikey}&secret={secret}"
     * @return retString
     */
    @RequestMapping(value = "/jwts", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public String CreateJwt(@RequestParam(value = "apiKey")String apiKey, 
    		@RequestParam(value = "secret")String secret) {
    	String output = "";
    	try {
    		URL url = new URL("http://localhost:8587/jwts?apiKey=" + apiKey
    					 + "&secret=" + secret);
    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    				
    		conn.setRequestMethod("GET");
    		//conn.setRequestProperty("Content-Type", "apiKey/json; charset=UTF-8");
    	    conn.setRequestProperty("Accept", "application/json");
    	   // conn.setDoOutput(true);
    	    
//    	    JSONObject newApp = new JSONObject();
  //  	    newApp.put("apiKey", apiKey);
    //	    newApp.put("secret", secret);
    	    
    	//    OutputStream os = conn.getOutputStream();
    	  //  os.write(newApp.toString().getBytes("UTF-8"));
    	   // os.close();
    	    
    	    BufferedReader br = new BufferedReader(new InputStreamReader(
    	                    (conn.getInputStream())));

    	    output = br.readLine();
    	            
    	    br.close();
    	} catch(Exception e) {
    		System.out.println(e.toString());
    	}
    	System.out.print(output);
    	return output;
    }
    
    
    
}

