package com.example.authentication;

/**
 *
 * @author gstafford
 */
public class Constants {

    /**
     * These define the URL parameters used in the route definition strings
     * (e.g. '{userId}').
     */
    public class Url {

        /**
         *
         */
        public static final String CLIENT_ID = "oid";

        /**
         *
         */
        public static final String API_KEY = "apiKey";

        /**
         *
         */
        public static final String SECRET = "secret";

        /**
         *
         */
        public static final String JWT = "jwt";
    }

    /**
     * These define the route names used in naming each route definitions. These
     * names are used to retrieve URL patterns within the controllers by name to
     * create links in responses.
     */
    public class Routes {

        /**
         *
         */
        public static final String SINGLE_CLIENT = "client.single.route";

        /**
         *
         */
        public static final String CLIENT_COLLECTION = "client.collection.route";
    }
}
