package com.example.authentication.objectid;

import com.auth0.jwt.Algorithm;
import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.auth0.jwt.internal.org.apache.commons.codec.binary.Base64;
//import com.auth0.jwt.internal.org.bouncycastle.crypto.tls.SignatureAlgorithm;


import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import org.restexpress.Request;
import org.restexpress.Response;

import com.example.authentication.Constants;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * @author gstafford
 */
public class JwtController {

    private static Logger LOG = LogManager.getLogger(JwtController.class.getName());
    private String baseUrl;
    private int jwtExpireLength;
    private String jwtIssuer;

    /**
     *
     * @param baseUrl
     * @param jwtExpireLength
     * @param jwtIssuer
     */
    public JwtController(String baseUrl, int jwtExpireLength, String jwtIssuer) {
        super();
        this.baseUrl = baseUrl;
        this.jwtExpireLength = jwtExpireLength;
        this.jwtIssuer = jwtIssuer;
    }

    /**
     *
     * @param request
     * @param response
     * @return
     */
    public Object createJwt(Request request, Response response) {
        String apiKey, secret, jwt;

        try {
            apiKey = request.getQueryStringMap().get(Constants.Url.API_KEY);
            if (apiKey == null) {
                LOG.error("request.getQueryStringMap().get(Constants.Url.API_KEY)"
                        + " failed: API key is null");
                return "API key is null";
            }
            secret = request.getQueryStringMap().get(Constants.Url.SECRET);
            if (secret == null) {
                LOG.error("request.getQueryStringMap().get(Constants.Url.SECRET)"
                        + " failed: Secret is null");
                return "Secret is null";
            }

            // http://www.epochconverter.com/
            long epoch_now = System.currentTimeMillis() / 1000;
            long epoch_expire = epoch_now + jwtExpireLength;

            JWTSigner jwts = new JWTSigner(secret.getBytes("UTF-8"));
            Map<String, Object> payload = new HashMap<>();
            payload.put("iss", jwtIssuer);
            payload.put("ait", epoch_now);
            payload.put("exp", epoch_expire);
            payload.put("apiKey", apiKey);
            
            JWTSigner.Options options = new JWTSigner.Options();
            options.setAlgorithm(Algorithm.HS256);
            
            jwt = jwts.sign(payload, options);
        } catch (Exception e) {
            LOG.error("createJwt() failed: " + ExceptionUtils.getRootCauseMessage(e));
            LOG.debug("createJwt() failed: " + ExceptionUtils.getStackTrace(e));
            return "JWT creation failed" + e;
        }
        return jwt;
    }

    /**
     *
     * @param request
     * @param response
     * @return
     */
    public Object validateJwt(Request request, Response response) {
        try {
            String jwt = request.getHeader(Constants.Url.JWT, "No JWT supplied");
            String alg = getSignatureAlgorithm(jwt);
            String apiKey = getApiKey(jwt);
            String secret = getSecret(apiKey);
            //System.out.println("apiKey is " + apiKey + "\nsecret is " + secret);
            new Base64(true);
			Map<String, Object> decodedPayload
                    = new JWTVerifier(secret).verify(jwt);

            if (!alg.equals("HS256") // prevent hack using 'none'
                    || Long.parseLong(decodedPayload.get("exp").toString())
                    <= System.currentTimeMillis() / 1000) {
                return false;
            }
        } catch (RuntimeException | NoSuchAlgorithmException | InvalidKeyException | IOException | SignatureException | JWTVerifyException e) {
            LOG.error("validateJwt() failed: " + ExceptionUtils.getRootCauseMessage(e));            LOG.debug("validateJwt() failed: " + ExceptionUtils.getStackTrace(e));
            return "JWT validation failed" + e;
        }

        return true;
    }

    private String getApiKey(String jwt) {
        String apiKey = "";
        try {
            String[] base64EncodedSegments = jwt.split(Pattern.quote("."));
            String base64EncodedClaims = base64EncodedSegments[1];
            byte[] decoded = Base64.decodeBase64(base64EncodedClaims);
            String claims = new String(decoded, "UTF-8") + "\n";
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(claims);
            LOG.info("JWT claims: " + json.toJSONString());
            apiKey = json.get("apiKey").toString();
        } catch (UnsupportedEncodingException | ParseException e) {
            LOG.error("getApiKey() failed: " + ExceptionUtils.getRootCauseMessage(e));
            LOG.debug("getApiKey() failed: " + ExceptionUtils.getStackTrace(e));
            return "Get API key failed";
        }
        return apiKey;
    }

    private String getSignatureAlgorithm(String jwt) {
        String alg = "";
        try {
            String[] base64EncodedSegments = jwt.split(Pattern.quote("."));
            String base64EncodedHeader = base64EncodedSegments[0];
            byte[] decoded = Base64.decodeBase64(base64EncodedHeader);
            String header = new String(decoded, "UTF-8") + "\n";
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(header);
            LOG.info("JWT header: " + json.toJSONString());
            alg = json.get("alg").toString();
        } catch (UnsupportedEncodingException | ParseException e) {
            LOG.error("validateJwt() failed: " + ExceptionUtils.getRootCauseMessage(e));
            LOG.debug("validateJwt() failed: " + ExceptionUtils.getStackTrace(e));
            return "Get signature algorithm failed";
        }
        return alg;
    }

    private String getSecret(String apiKey) {
        String output, secret = "";
        try {
            URL url = new URL("http://" + baseUrl + "/clients/find/secret");
            LOG.info("Authentication service URL called: " + url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("filter", "apiKey::" + apiKey);

            if (conn.getResponseCode() != 200) {
                //System.out.println(conn.getResponseCode());
                return String.valueOf("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            while ((output = br.readLine()) != null) {
                secret = output;
            }
            conn.disconnect();
            br.close();
        } catch (IOException e) {
            LOG.error("getSecret() failed: " + ExceptionUtils.getRootCauseMessage(e));
            LOG.debug("getSecret() failed: " + ExceptionUtils.getStackTrace(e));
            return "Get secret failed" + e;
        }
        return secret.replaceAll("\"", "");
    }
}
