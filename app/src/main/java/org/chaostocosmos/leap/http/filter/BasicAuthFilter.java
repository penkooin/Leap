package org.chaostocosmos.leap.http.filter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.MSG_TYPE;
import org.chaostocosmos.leap.http.WASException;

public class BasicAuthFilter implements IHttpFilter {
    
    @Override
    public void filterRequest(HttpRequestDescriptor request) throws Exception {
        final String authorization = request.getReqHeader().get("Authorization");
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            String base64Credentials = authorization.substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            final String[] values = credentials.split(":", 2);
            if(!login(values[0], values[1])) {
                throw new WASException(MSG_TYPE.ERROR, "error018");
            }
        } else {
            throw new WASException(MSG_TYPE.ERROR, "error018");
        }        
    }

    @Override
    public void filterResponse(HttpResponseDescriptor response) throws Exception {
    }

    private boolean login(String user, String password) {
        if(user.equals("chaos930") && password.equals("9393")) {
            return true;
        }
        return false;
    }
    
}
