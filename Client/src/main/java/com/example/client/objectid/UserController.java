package com.example.client.objectid;

import com.example.authenticate.AuthenticateJwt;
import com.example.client.Constants;
import com.strategicgains.hyperexpress.HyperExpress;
import com.strategicgains.hyperexpress.builder.DefaultUrlBuilder;
import com.strategicgains.hyperexpress.builder.TokenBinder;
import com.strategicgains.hyperexpress.builder.TokenResolver;
import com.strategicgains.repoexpress.mongodb.Identifiers;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.common.query.QueryFilter;
import org.restexpress.common.query.QueryOrder;
import org.restexpress.common.query.QueryRange;
import org.restexpress.query.QueryFilters;
import org.restexpress.query.QueryOrders;
import org.restexpress.query.QueryRanges;

import java.util.List;

/**
 * This is the 'controller' layer, where HTTP details are converted to domain
 * concepts and passed to the service layer. Then service layer response
 * information is enhanced with HTTP details, if applicable, for the response.
 * <p/>
 * This controller demonstrates how to process an entity that is identified by a
 * MongoDB ObjectId.
 */
public class UserController {
	
	private static final DefaultUrlBuilder LOCATION_BUILDER = new DefaultUrlBuilder();
	private final UserService service;
	private final String baseUrl;
	private final AuthenticateJwt jwtImpl = new AuthenticateJwt();
	
	/**
	 * 
	 * @param theUserService
	 * @param theBaseUrl
	 */
	public UserController(UserSevice theUserService, String theBaseUrl) {
		super();
		service = theUserService;
		baseUrl = theBaseUrl;
	}
	
	/**
	 * 
	 * @param theRequest
	 * @param theResponse
	 * @return saved
	 */
	public User create(Request theRequest, Response theResponse) {
		if(jwtImpl.authenticateJwt(theRequest, baseUrl) != true)) {
			theResponse.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
			return null;
		}
		User entity = theRequest.getBodyAs(User.class, "Resource details not provided");
		User saved = service.create(entity);
		
		// Construct the response
		theResponse.setResponseCreated();
		
		// Bind the resource with link URL tokens, etc.
		TokenResolver resolver = HyperExpress.bind(Constants.Url.USER_ID, Identifiers.MONGOID.format(saved.getId()));
		
		// Include the Location header
		String locationPattern = theRequest.getNamedUrl(HttpMethod.GET, Constants.Routes.SINGLE_USER);
		theResponse.addLocationHeader(LOCATION_BUILDER.build(locationPattern, resolver));
		
		// Return the newly-created resource
		return saved;
	}
	
	/**
	 * 
	 * @param theRequest
	 * @param theResponse
	 * @return entity
	 */
	public User read(Request theRequest, Response theResponse) {
		if(jwtImpl.authenticateJwt(theRequest, baseUrl) != true)) {
			theResponse.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
			return null;
		}
		String id = theRequest.getHeader(Constants.Url.USER_ID, "No resource ID supplied");
		User entity = service.read(Identifiers.MONGOID.parse(id));
		
		// Enrich the resource with links, etc.
		HyperExpress.bind(Constants.Url.USER_ID, Identifiers.MONGOID.format(entity.getID));
		
		return entity;
	}
	
	/**
	 * 
	 * @param theRequest
	 * @param theResponse
	 * @return entities
	 */
	public List<User> readAll(Request theRequest, Response theResponse) {
		if(jwtImpl.authenticateJwt(theRequest, baseUrl) != true)) {
			theResponse.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
			return null;
		}
		QueryFilter filter = QueryFilters.parseFrom(theRequest);
		QueryOrder order = QueryOrders.parseFrom(theRequest);
		QueryRange range = QueryRanges.parseFrom(theRequest, 20);
		boolean countOnly = Boolean.parseBoolean(theRequest.getQueryStringMap().getOrDefault("countOnly", "false"));
		List<User> entities = service.readAll(filter, range, order);
		long count = service.count(filter);
		theResponse.setCollectionResponse(range, entities.size(), count);
		
		// Bind the resources in the collection with link URL tokens, etc.
		HyperExpress.tokenBinder(new TokenBinder<User>() {
			@Override
			public void bind(User entity, TokenResolver resolver) {
				resolver.bind(Constants.Url.USER_ID, Identifiers.MONGOID.format(entity.getID()));
			}
		});
		
		if(countOnly) { // only return count in Content-Range header
			entities.clear();
			return entities;
		}
		
		return entities;
	}
	
	/**
	 * 
	 * @param theRequest
	 * @param theResponse
	 * @return entity
	 */
	public User update(Request theRequest, Response theResponse) {
		if(jwtImpl.authenticateJwt(theRequest, baseUrl) != true)) {
			theResponse.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
			return null;
		}
		String id = theRequest.getHeader(Constants.Url.USER_ID, "No resource ID supplied");
		User entity = theRequest.getBodyAs(User.class, "Resource details not provided");
		entity.setID(Identifiers.MONGOID.parse(id));
		service.update(entity);
		
		// new per http://stackoverflow.com/a/827045/580268
		entity = service.read(Identifiers.MONGOID.parse(id));
		theResponse.setResponseStatus(HttpResponseStatus.CREATED);
		
		// enrich the resource with links, etc.
		HyperExpress.bind(Constants.Url.USER_ID, Identifiers.MONGOID.format(entity.getID));
		
		return entity;
	}
	
	public void delete(Request theRequest, Response theResponse) {
		if(jwtImpl.authenticateJwt(theRequest, baseUrl) != true)) {
			theResponse.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
			return null;
		}
		String id = theRequest.getHeader(Constants.Url.USER_ID, "No resource ID supplied");
		service.delete(Identifiers.MONGOID.parse(id));
		
		theResponse.setResponseNoContent();
	}
}