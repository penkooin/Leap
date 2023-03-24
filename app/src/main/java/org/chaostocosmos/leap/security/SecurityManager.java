package org.chaostocosmos.leap.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.common.Constants;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.META;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.http.HTTPException;

import ch.qos.logback.classic.Logger;

/**
 * UserManager 
 * 
 * @author 9ins
 */
public class SecurityManager {    
    /**
     * Logger object
     */
    Logger logger;

    /**
     * User list
     */
    protected List<UserCredentials> users;

    /**
     * Default constructor
     * @param context
     */
    public SecurityManager(String hostId) {
        this.logger = Context.host(hostId).getLogger();
        this.users = Context.hosts().getHost(hostId).<List<Map<String, Object>>>getUsers().stream().map(m -> new UserCredentials(m)).collect(Collectors.toList());
    }

    public UserCredentials logout(String username) {
        UserCredentials user = this.users.stream().filter(u -> u.getUsername().equals(username)).findFirst().orElse(null);
        if(user != null) {
            user.getSession().invalidate();
            this.users.remove(user);
        }
        return null;
    }

    public UserCredentials login(String username, String password) throws HTTPException {
        UserCredentials user = this.users.stream().filter(u -> u.getUsername().equals(username)).findAny().orElseThrow(() -> new HTTPException(HTTP.RES401, 25, username));
        return user;
    }

    public void register(UserCredentials user) throws HTTPException {
        if(this.users.stream().anyMatch(u -> u.getUsername().equals(user.getUsername()))) {
            throw new HTTPException(HTTP.RES401, 17, user.getUsername()); 
        }
        if(!Constants.PASSWORD_REGEX.matcher(user.getPassword()).matches()) {
            throw new HTTPException(HTTP.RES401, 18);
        }
        this.users.add(user);
        save(this.users);
    }

    /**
     * Login with Basic authetication string in request
     * @param authorization
     * @return
     */
    public synchronized UserCredentials authenticate(String authorization) {
        if (authorization != null && authorization.trim().startsWith("Basic")) {
            String base64Credentials = authorization.trim().substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            String[] values = credentials.split(":", 2);
            //System.out.println(values[0]+" "+values[1]);
            UserCredentials user = login(values[0], values[1]);
            this.logger.debug("==================================================");  
            this.logger.debug("User "+values[0]+" is login.");  
            this.logger.debug("==================================================");  
            return user;
        }
        throw new HTTPException(HTTP.RES401, 28, authorization);
    }

    /**
     * Get user object by specified user name
     * @param username
     * @return
     */
    public UserCredentials getUserByName(String username) {
        return this.users.stream().filter(u -> u.getUsername().equals(username)).findAny().orElseThrow();
    }

    /**
     * Logout user
     * @param user
     * @return
     */
    public UserCredentials logout(UserCredentials user) {
        return logout(user.getUsername());
    }

    /**
     * Save user list
     * @param users
     * @throws HTTPException
     */
    public void save(List<UserCredentials> users) throws HTTPException {
        List<Map<String, Object>> list = users.stream().map(u -> u.getUserCredentialsMap()).collect(Collectors.toList());
        Context.server().setValue("server.users", list);
        Context.save(META.SERVER);
    }
}
