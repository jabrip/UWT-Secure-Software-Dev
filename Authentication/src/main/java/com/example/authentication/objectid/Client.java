package com.example.authentication.objectid;

import org.restexpress.plugin.hyperexpress.Linkable;

import com.strategicgains.repoexpress.mongodb.AbstractMongodbEntity;
import org.mongodb.morphia.annotations.Entity;

/**
 * This is a sample entity identified by a MongoDB ObjectID (instead of a UUID).
 * It also contains createdAt and updatedAt properties that are automatically
 * maintained by the persistence layer (SampleOidEntityRepository).
 */
@Entity("clients")
public class Client
        extends AbstractMongodbEntity
        implements Linkable {

    private String application;
    private String secret;
    private String apiKey;

    /**
     *
     */
    public Client() {
    }

    /**
     *
     * @param application
     * @param secret
     */
    public Client(final String application, final String secret) {
        this.application = application;
        this.secret = secret;
    }

    /**
     * @return the application
     */
    public String getApplication() {
        return application;
    }

    /**
     * @param application the application to set
     */
    public void setApplication(String application) {
        this.application = application;
    }

    /**
     * @return the secret
     */
    public String getSecret() {
        return secret;
    }

    /**
     * @param secret the secret to set
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * @return the apiKey
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * @param apiKey the apiKey to set
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    /**
     * 
     * @param other the client we're comparing with
     * @return if the two clients are equal
     */
    public boolean equals(Client other) {
    	if (application.compareTo(other.getApplication()) != 0) {
    		return false;
    	} else if (application.compareTo(other.getApiKey()) != 0) {
    		return false;
    	} else if (application.compareTo(other.getSecret()) != 0) {
    		return false;
    	} else {
    		return true;
    	}
    }

}