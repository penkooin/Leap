package org.chaostocosmos.leap.http.filters;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.annotation.PreFilter;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.user.User;

/**
 * BasicAuthFilter object
 * @author 9ins
 */
public class BasicAuthFilter<R, S> extends AbstractHttpFilter<R, S> implements IAuthenticate { 

    @Override
    @PreFilter
    public void filterRequest(R r) throws Exception { 
        super.filterRequest(r);
        if(r.getClass().isAssignableFrom(HttpRequestDescriptor.class)) {
            HttpRequestDescriptor request = (HttpRequestDescriptor)r;
            final String authorization = request.getReqHeader().get("Authorization");
            if (authorization != null && authorization.trim().startsWith("Basic")) {
                String base64Credentials = authorization.trim().substring("Basic".length()).trim();
                byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                final String[] values = credentials.split(":", 2);
                //System.out.println(values[0]+" "+values[1]);
                if(!signIn(values[0], values[1])) {                    
                    throw new WASException(MSG_TYPE.HTTP, 401);
                }
                LoggerFactory.getLogger(request.getRequestedHost()).debug("User "+values[0]+" is login."); 
            } else {
                throw new WASException(MSG_TYPE.HTTP, 401, "Auth information not found!!!");
            }
        }
    }

    @Override
    public boolean signIn(String username, String password) throws WASException {
        if(super.userManager == null) {
            throw new IllegalStateException("Leap security manager not set. Can not sing in with "+username+"/"+password);
        }   
        //System.out.println(username + ":" + password+" "+super.userManager);
        return super.userManager.signIn(username, password);
    }

    @Override
    public void signUp(User user) throws WASException {
        if(super.userManager == null) {
            throw new IllegalStateException("Leap security manager not set. Can not sing up with "+user.toString());
        }   
        super.userManager.signUp(user);
    }
}
