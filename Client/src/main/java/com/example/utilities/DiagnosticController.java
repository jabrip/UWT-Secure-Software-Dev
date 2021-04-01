package com.example.utilities;

import org.restexpress.Request;
import org.restexpress.Response;

import io.netty.handler.codec.http.HttpResponseStatus;

public class DiagnosticController {

	/**
	 * 
	 */
	public DiagnosticController() {
		super();
	}
	
	/**
	 * 
	 * @param theRequest
	 * @param theResponse
	 */
	public void ping(Request theRequest, Response theResponse) {
		theResponse.setResponseStatus(HttpResponseStatus.OK);
		theResponse.setResponseCode(200);
		theResponse.setBody(true);
	}
}
