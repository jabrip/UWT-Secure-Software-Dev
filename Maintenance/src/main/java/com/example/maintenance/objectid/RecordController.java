package com.example.maintenance.objectid;

import com.example.authenticate.AuthenticateJwt;
import com.example.maintenance.Constants;
import com.strategicgains.hyperexpress.HyperExpress;
import com.strategicgains.hyperexpress.builder.DefaultUrlBuilder;
import com.strategicgains.hyperexpress.builder.TokenBinder;
import com.strategicgains.hyperexpress.builder.TokenResolver;
import com.strategicgains.repoexpress.mongodb.Identifiers;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 * concepts and passed to the record layer. Then record layer response
 * information is enhanced with HTTP details, if applicable, for the response.
 * <p/>
 * This controller demonstrates how to process an entity that is identified by a
 * MongoDB ObjectId.
 */
public class RecordController {

    private static final Logger LOG = LogManager.getLogger(RecordController.class.getName());
    private static final DefaultUrlBuilder LOCATION_BUILDER = new DefaultUrlBuilder();
    private final RecordService service;
    private final String baseUrl;

    public RecordController(RecordService recordService, String baseUrl) {
        super();
        this.service = recordService;
        this.baseUrl = baseUrl;
    }

    public Record create(Request request, Response response) {
        if (AuthenticateJwt.authenticateJwt(request, baseUrl) != true) {
            response.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
            return null;
        }
        Record entity = request.getBodyAs(Record.class, "Resource details not provided");
        Record saved = service.create(entity);

        // Construct the response for create...
        response.setResponseCreated();

        // Bind the resource with link URL tokens, etc. here...
        TokenResolver resolver = HyperExpress.bind(Constants.Url.RECORD_ID, Identifiers.MONGOID.format(saved.getId()));

        // Include the Location header...
        String locationPattern = request.getNamedUrl(HttpMethod.GET, Constants.Routes.SINGLE_RECORD);
        response.addLocationHeader(LOCATION_BUILDER.build(locationPattern, resolver));

        LOG.info("maintenance record created: " + Identifiers.MONGOID.format(saved.getId()));

        // Return the newly-created resource...
        return saved;
    }

    public Record read(Request request, Response response) {
        if (AuthenticateJwt.authenticateJwt(request, baseUrl) != true) {
            response.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
            return null;
        }
        String id = request.getHeader(Constants.Url.RECORD_ID, "No resource ID supplied");
        Record entity = service.read(Identifiers.MONGOID.parse(id));

        // enrich the resource with links, etc. here...
        HyperExpress.bind(Constants.Url.RECORD_ID, Identifiers.MONGOID.format(entity.getId()));

        return entity;
    }

    public List<Record> readAll(Request request, Response response) {
        if (AuthenticateJwt.authenticateJwt(request, baseUrl) != true) {
            response.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
            return null;
        }
        QueryFilter filter = QueryFilters.parseFrom(request);
        QueryOrder order = QueryOrders.parseFrom(request);
        QueryRange range = QueryRanges.parseFrom(request, 20);
        boolean countOnly = Boolean.parseBoolean(
                request.getQueryStringMap().getOrDefault("countOnly", "false"));
        List<Record> entities = service.readAll(filter, range, order);
        long count = service.count(filter);
        response.setCollectionResponse(range, entities.size(), count);

        // Bind the resources in the collection with link URL tokens, etc. here...
        HyperExpress.tokenBinder(new TokenBinder<Record>() {
            @Override
            public void bind(Record entity, TokenResolver resolver) {
                resolver.bind(Constants.Url.RECORD_ID, Identifiers.MONGOID.format(entity.getId()));
            }
        });

        if (countOnly) { // only return count in Content-Range header
            entities.clear();
            return entities;
        }
        return entities;
    }

    public Record update(Request request, Response response) {
        if (AuthenticateJwt.authenticateJwt(request, baseUrl) != true) {
            response.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
            return null;
        }
        String id = request.getHeader(Constants.Url.RECORD_ID, "No resource ID supplied");
        Record entity = request.getBodyAs(Record.class, "Resource details not provided");
        entity.setId(Identifiers.MONGOID.parse(id));
        service.update(entity);

        // new per http://stackoverflow.com/a/827045/580268
        entity = service.read(Identifiers.MONGOID.parse(id));
        response.setResponseStatus(HttpResponseStatus.CREATED);

        // enrich the resource with links, etc. here...
        HyperExpress.bind(Constants.Url.RECORD_ID, Identifiers.MONGOID.format(entity.getId()));

        LOG.info("maintenance record upated: " + Identifiers.MONGOID.format(entity.getId()));

        return entity;
        // original response returned nothing
        //response.setResponseNoContent();
    }

    public void delete(Request request, Response response) {
        if (AuthenticateJwt.authenticateJwt(request, baseUrl) != true) {
            response.setResponseStatus(HttpResponseStatus.UNAUTHORIZED);
        }
        String id = request.getHeader(Constants.Url.RECORD_ID, "No resource ID supplied");
        service.delete(Identifiers.MONGOID.parse(id));

        LOG.info("maintenance record deleted: " + Identifiers.MONGOID.parse(id));

        response.setResponseNoContent();
    }
}
