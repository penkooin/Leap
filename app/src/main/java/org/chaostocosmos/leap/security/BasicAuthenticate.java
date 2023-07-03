package org.chaostocosmos.leap.security;

import org.chaostocosmos.leap.LeapException;
import org.chaostocosmos.leap.annotation.FieldMapper;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
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
    public UserCredentials userCredentials(String username) throws LeapException {
        UserCredentials user = this.securityManager.login(username, null);
        if(user == null) {
            LeapException httpe = new LeapException(HTTP.RES401, "User( "+username+" ) not found in server."); 
            throw httpe;
        }
        host.getLogger().debug("User "+username+" is login.");
        return user;
    }

    @Override
    public boolean logout(String username) throws LeapException {
        UserCredentials user = this.securityManager.logout(username);
        user.getSession().invalidate();
        return true;
    }

    public void register(UserCredentials user) throws LeapException {
        this.securityManager.register(user);
    }
}

