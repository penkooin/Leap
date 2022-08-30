package org.chaostocosmos.leap.http.security;

import java.util.HashMap;

import org.chaostocosmos.leap.http.HTTPException;
import org.chaostocosmos.leap.http.annotation.FieldMapper;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.context.User;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.service.model.IAuthenticate;
import org.chaostocosmos.leap.http.session.SessionManager;

/**
 * BasicAuthorization
 * 
 * @author 9ins
 */
public class BasicAuthorization implements IAuthenticate {

    @FieldMapper(loadClass = SecurityManager.class)
    SecurityManager securityManager;

    @FieldMapper(loadClass = SessionManager.class)
    SessionManager sessionManager;

    Host<?> host;

    public BasicAuthorization(Host<?> host) {
        this.host = host;
    }

    @Override
    public User login(String username, String password) throws HTTPException {
        User user = this.securityManager.signIn(username, password);
        if(user == null) {            
            HTTPException httpe = new HTTPException(RES_CODE.RES401, new HashMap<>(), "User( "+username+" ) not found in server." );
            httpe.addHeader("WWW-Authenticate", "Basic");
            throw httpe;
        }
        host.getLogger().debug("User "+username+" is login.");  
        return user;
    }

    @Override
    public User logout(String username) throws HTTPException {
        User user = this.securityManager.logout(username);
        user.getSession().invalidate();
        return user;
    }

    @Override
    public void register(User user) throws HTTPException {
        this.securityManager.signUp(user);
    }
}

