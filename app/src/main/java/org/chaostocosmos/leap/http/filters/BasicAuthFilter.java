package org.chaostocosmos.leap.http.filters;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.MSG_TYPE;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.annotation.PreFilter;
import org.chaostocosmos.leap.http.commons.LoggerFactory;
import org.chaostocosmos.leap.http.user.User;
import org.chaostocosmos.leap.http.user.UserManager;

/**
 * BasicAuthFilter object
 * @author 9ins
 */
public class BasicAuthFilter<R, S> extends AbstractHttpFilter<R, S> implements IAuthenticate { 

    @Override
    @PreFilter
    public void filterRequest(R r) throws WASException { 
        super.filterRequest(r);
        if(r.getClass().isAssignableFrom(HttpRequestDescriptor.class)) {
            HttpRequestDescriptor request = (HttpRequestDescriptor)r;
            final String authorization = request.getReqHeader().get("Authorization");
            if (authorization != null && authorization.trim().startsWith("Basic")) {
                String base64Credentials = authorization.trim().substring("Basic".length()).trim();
                byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                final String[] values = credentials.split(":", 2);
                if(!signIn(values[0], values[1])) {                    
                    throw new WASException(MSG_TYPE.HTTP, 401);
                }
                LoggerFactory.getLogger(request.getRequestedHost()).debug("User "+values[0]+" is login."); 
            } else {
                throw new WASException(MSG_TYPE.HTTP, 401);
            }
        }
    }

    @Override
    public boolean signIn(String username, String password) throws WASException {
        return UserManager.getInstance().signIn(username, password);
    }

    @Override
    public void signUp(User user) throws WASException {
        UserManager.getInstance().signUp(user);
    }
}
