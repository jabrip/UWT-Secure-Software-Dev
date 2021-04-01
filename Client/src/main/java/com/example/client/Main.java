package com.example.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.restexpress.Flags;
import org.restexpress.RestExpress;
import org.restexpress.exception.BadRequestException;
import org.restexpress.exception.ConflictException;
import org.restexpress.exception.NotFoundException;
import org.restexpress.pipeline.SimpleConsoleLogMessageObserver;
import org.restexpress.plugin.hyperexpress.HyperExpressPlugin;
import org.restexpress.plugin.hyperexpress.Linkable;
import org.restexpress.util.Environment;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.example.client.serialization.SerializationProvider;
import com.strategicgains.repoexpress.exception.DuplicateItemException;
import com.strategicgains.repoexpress.exception.InvalidObjectIdException;
import com.strategicgains.repoexpress.exception.ItemNotFoundException;
import com.strategicgains.restexpress.plugin.cache.CacheControlPlugin;
import com.strategicgains.restexpress.plugin.cors.CorsHeaderPlugin;
import com.strategicgains.restexpress.plugin.metrics.MetricsConfig;
import com.strategicgains.restexpress.plugin.metrics.MetricsPlugin;
import com.strategicgains.restexpress.plugin.swagger.SwaggerPlugin;
import com.strategicgains.syntaxe.ValidationException;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.restexpress.Flags.Auth.PUBLIC_ROUTE;

/**
*
*@author Jabriel Phan
**/
public class Main {
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		RestExpress server = initializeServer(args);
		server.awaitShutdown();
	}
	
	/**
	 * 
	 * @param theArgs
	 * @return server
	 * @throws IOException
	 */
	public static RestExpress initializeServer(String[] theArgs) throws IOException {
		RestExpress.setDefaultSerializationProvider(new SerializationProvider());
		
		Configuration config = loadEnvironment(theArgs);
		RestExpress server = new RestExpress()
				.setName(config.getServiceName())
				.setBaseUrl(config.getBaseUrl())
				.setExecutorThreadCount(config.getExecutorThreadPoolSize)
				.addMessageObserver(new SimpleConsoleLogMessageObserver());
		Routes.define(config, server);
		Relationships.define(server);
		configurePlugins(config, server);
		mapExceptions(server);
		server.bind(config.getPort());
		return server;
	}
	
	/**
	 * 
	 * @param theConfig
	 * @param theServer
	 */
	private static void configurePlugins(Configuration theConfig, RestExpress theServer) {
		configureMetrics(config, server);
		
		new SwaggerPlugin()
			.flag(PUBLIC_ROUTE)
			.register(theServer);
		
		new CacheControlPlugin()
			.register(theServer);
		
		new HyperExpressPlugin(Linkable.class)
			.register(theServer);
		
		new CorsHeaderPlugin("*")
			.flag(PUBLIC_ROUTE)
			.allowHeaders(CONTENT_TYPE, ACCEPT, AUTHORIZATION, REFERER, LOCATION)
			.exposeHeaders(LOCATION)
			.register(theServer);
	}
	
	/**
	 * 
	 * @param theConfig
	 * @param theServer
	 */
	private static void configureMetrics(Configuration theConfig, RestExpress theServer) {
		MetricsConfig mc = config.getMetricsConfig();
		
		if(mc.isEnabled()) {
			MetricRegistry registry = new MetricRegistry();
			new MetricsPlugin(registry)
				.register(theServer);
			
			if(mc.isGraphiteEnabled()) {
				final Graphite graphite = new Graphite(new InetSocketAddress(mc.getGraphiteHost(), mc.getGraphitePort()));
				final GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
						.prefixedWith(mc.getPrefix())
						.convertRatesTo(TimeUnit.SECONDS)
						.convertDurationsTo(TimeUnit.MILLISECONDS)
						.filter(MetricFilter.ALL)
						.build(graphite);
				reporter.start(mc.getPublishSeconds(), TimeUnit.SECONDS);
			}
		}
	}
	
	/**
	 * 
	 * @param theServer
	 */
	private static void mapExceptions(RestExpress theServer) {
		theServer
			.mapException(ItemNotFoundException.class, NotFoundException.class)
			.mapException(DuplicateItemException.class, ConflictException.class)
			.mapException(ValidationException.class, BadRequestException.class)
			.mapException(InvalidObjectIdException.class, BadRequestException.class);
	}
	
	/**
	 * 
	 * @param theArgs
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static Configuration loadEnvironment(String[] theArgs)
			throws FileNotFoundException, IOException{
		if(theArgs.length > 0) {
			return Environment.from(theArgs[0], Configuration.class);
		}
		
		return Environment.fromDefault(Configuration.class);
	}
}