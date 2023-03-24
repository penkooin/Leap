package org.chaostocosmos.leap.security;

import org.chaostocosmos.leap.annotation.FieldMapper;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.http.HTTPException;
import org.chaostocosmos.leap.session.SessionManager;

/**
 * BasicAuthorization
 * 
 * @author 9ins
 */
public class BasicAuthenticate implements IAuthenticate {

    @FieldMapper(mappingClass = SecurityManager.class, parameters = {Host.class})
    SecurityManager securityManager;

    @FieldMapper(mappingClass = SessionManager.class, parameters = {Host.class})
    SessionManager sessionManager;

    Host<?> host;

    @Override
    public UserCredentials userCredentials(String username) throws HTTPException {
        UserCredentials user = this.securityManager.login(username, null);
        if(user == null) {
            HTTPException httpe = new HTTPException(HTTP.RES401, "User( "+username+" ) not found in server."); 
            httpe.addHeader("WWW-Authenticate", "Basic");
            throw httpe;
        }
        host.getLogger().debug("User "+username+" is login.");
        return user;
    }

    @Override
    public boolean logout(String username) throws HTTPException {
        UserCredentials user = this.securityManager.logout(username);
        user.getSession().invalidate();
        return true;
    }

    public void register(UserCredentials user) throws HTTPException {
        this.securityManager.register(user);
    }
}

