package org.chaostocosmos.leap.filter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.chaostocosmos.leap.common.constant.Constants;
import org.chaostocosmos.leap.common.log.LoggerFactory;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.http.HttpRequest;
import org.chaostocosmos.leap.security.SecurityManager;
import org.chaostocosmos.leap.security.UserCredentials;
import org.chaostocosmos.leap.session.Session;

/**
 * BasicAuthFilter object
 * 
 * @author 9ins
 */
public class BasicAuthFilter<T> extends AbstractRequestFilter<HttpRequest<T>> { 

    /**
     * Security manager object
     */
    SecurityManager securityManager;

    @Override
    public void filterRequest(HttpRequest<T> request) throws Exception { 
        super.filterRequest(request);
        String sessionId = request.getCookie(Constants.SESSION_ID_KEY);
        Session session = super.sessionManager.getSessionIfExist(sessionId);

        if(session == null && request.getClass().isAssignableFrom(HttpRequest.class)) {
            final Object authorization = request.getHeaders().get("Authorization");
            if (authorization != null && authorization.toString().startsWith("Basic")) {
                String base64Credentials = authorization.toString().substring("Basic".length()).trim();
                byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                final String[] values = credentials.split(":", 2);
                //System.out.println(values[0]+" "+values[1]);
                UserCredentials user = login(values[0], values[1]);
                if(user != null) {
                    session = super.sessionManager.createSession(sessionId);
                    super.sessionManager.addSession(session);
                    user.setSession(session);
                    request.setSession(session);
                } else {
                    throw new LeapException(HTTP.RES401, "User( "+values[0]+" ) not found in server." );
                }
                LoggerFactory.getLogger(request.getRequestedHost()).debug("User "+values[0]+" is login.");  
            } else {
                throw new LeapException(HTTP.RES401, "Auth information not found!!!" ); 
            }
        }
    }

    /**
     * Login
     * @param username
     * @param password
     * @return
     * @throws LeapException
     */
    public UserCredentials login(String username, String password) throws LeapException {
        if(this.securityManager == null) {
            throw new IllegalStateException("Leap security manager not set. Can not sing in with "+username+"/"+password);
        }
        return this.securityManager.login(username, password);
    }

    /**
     * Register
     * @param user
     * @throws LeapException
     */
    public void register(UserCredentials user) throws LeapException {
        if(this.securityManager == null) {
            throw new IllegalStateException("Leap security manager not set. Can not sing up with "+user.toString());
        }   
        this.securityManager.register(user);
    }

    /**
     * Log out
     * @param username
     * @return
     * @throws LeapException
     */
    public UserCredentials logout(String username) throws LeapException {
        return this.securityManager.logout(username);
    }
}



