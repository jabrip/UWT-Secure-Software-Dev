package com.example.client;

import org.restexpress.RestExpress;

import io.netty.handler.codec.http.HttpMethod;

/**
*
*@author Jabriel Phan
*/
public abstract class Routes {
	
	/**
	 * 
	 * @param config
	 * @param server
	 */
	public static void define(Configuration config, RestExpress server) {
		server.uri("/users/{oid}.{format}", config.getUserController())
			.method(HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE)
			.name(Constants.Routes.SINGLE_USER);
		
		server.uri("/users.{format}", config.getUserController())
			.action("readAll", HttpMethod.GET)
			.method(HttpMethod.POST)
			.name(Constants.Routes.USER_COLELCTION);
		
		server.uri("/users/utils/ping.{format}", config.getDiagnosticController())
			.action("ping", HttpMethod.GET);
	}
}