package org.chaostocosmos.leap.http.service.filter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

import org.chaostocosmos.leap.http.HTTPException;
import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.annotation.PreFilterIndicates;
import org.chaostocosmos.leap.http.common.LoggerFactory;
import org.chaostocosmos.leap.http.context.User;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.session.Session;
import org.chaostocosmos.leap.http.security.SecurityManager;
import org.chaostocosmos.leap.http.service.model.IAuthenticate;

/**
 * BasicAuthFilter object
 * 
 * @author 9ins
 */
public class BasicAuthFilter extends AbstractRequestFilter implements ISecurityFilter, IAuthenticate { 
    /**
     * Security manager object
     */
    SecurityManager securityManager;

    @Override
    @PreFilterIndicates
    public void filterRequest(Request request) throws Exception { 
        super.filterRequest(request);
        String sessionId = request.getCookie("__Leap-Session-ID");
        Session session = super.sessionManager.getSession(sessionId);

        if(session == null && request.getClass().isAssignableFrom(Request.class)) {
            final String authorization = request.getReqHeader().get("Authorization");
            if (authorization != null && authorization.trim().startsWith("Basic")) {
                String base64Credentials = authorization.trim().substring("Basic".length()).trim();
                byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                final String[] values = credentials.split(":", 2);
                //System.out.println(values[0]+" "+values[1]);
                User user = login(values[0], values[1]);
                if(user != null) {
                    session = super.sessionManager.createSession(request);              
                    super.sessionManager.addSession(session);
                    user.setSession(session);
                    request.setSession(session);
                } else {
                    HTTPException httpe = new HTTPException(RES_CODE.RES401, new HashMap<>(), "User( "+values[0]+" ) not found in server." );
                    httpe.addHeader("WWW-Authenticate", "Basic");
                    throw httpe;
                }
                LoggerFactory.getLogger(request.getRequestedHost()).debug("User "+values[0]+" is login.");  
            } else {
                HTTPException httpe = new HTTPException(RES_CODE.RES401, new HashMap<>(), "Auth information not found!!!" );                
                httpe.addHeader("WWW-Authenticate", "Basic");
                throw httpe;
            }
        }
    }

    @Override
    public User login(String username, String password) throws HTTPException {
        if(this.securityManager == null) {
            throw new IllegalStateException("Leap security manager not set. Can not sing in with "+username+"/"+password);
        }   
        return this.securityManager.login(username, password);
    }

    @Override
    public void register(User user) throws HTTPException {
        if(this.securityManager == null) {
            throw new IllegalStateException("Leap security manager not set. Can not sing up with "+user.toString());
        }   
        this.securityManager.register(user);
    }

    @Override
    public User logout(String username) throws HTTPException {
        return this.securityManager.logout(username);
    }

    @Override
    public void setSecurityManager(SecurityManager securityManager) {
        this.securityManager = securityManager;        
    }

    @Override
    public SecurityManager getSecurityManager() {
        return this.securityManager;
    }
}
