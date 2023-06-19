package org.chaostocosmos.leap.service.filter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.chaostocosmos.leap.LeapException;
import org.chaostocosmos.leap.common.Constants;
import org.chaostocosmos.leap.common.LoggerFactory;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.security.UserCredentials;
import org.chaostocosmos.leap.security.SecurityManager;
import org.chaostocosmos.leap.session.Session;

/**
 * BasicAuthFilter object
 * 
 * @author 9ins
 */
public class BasicAuthFilter extends AbstractRequestFilter { 
    /**
     * Security manager object
     */
    SecurityManager securityManager;

    @Override
    public void filterRequest(Request request) throws Exception { 
        super.filterRequest(request);
        String sessionId = request.getCookie(Constants.SESSION_ID_KEY);
        Session session = super.sessionManager.getSessionCreateIfNotExists(sessionId);

        if(session == null && request.getClass().isAssignableFrom(Request.class)) {
            final String authorization = request.getReqHeader().get("Authorization");
            if (authorization != null && authorization.trim().startsWith("Basic")) {
                String base64Credentials = authorization.trim().substring("Basic".length()).trim();
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

    public UserCredentials login(String username, String password) throws LeapException {
        if(this.securityManager == null) {
            throw new IllegalStateException("Leap security manager not set. Can not sing in with "+username+"/"+password);
        }
        return this.securityManager.login(username, password);
    }

    public void register(UserCredentials user) throws LeapException {
        if(this.securityManager == null) {
            throw new IllegalStateException("Leap security manager not set. Can not sing up with "+user.toString());
        }   
        this.securityManager.register(user);
    }

    public UserCredentials logout(String username) throws LeapException {
        return this.securityManager.logout(username);
    }
}



