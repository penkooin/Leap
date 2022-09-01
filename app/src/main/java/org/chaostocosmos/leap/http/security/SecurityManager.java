package org.chaostocosmos.leap.http.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.HTTPException;
import org.chaostocosmos.leap.http.common.Constants;
import org.chaostocosmos.leap.http.common.LoggerFactory;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.META;
import org.chaostocosmos.leap.http.context.User;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.service.model.IAuthenticate;

import ch.qos.logback.classic.Logger;

/**
 * UserManager 
 * 
 * @author 9ins
 */
public class SecurityManager implements IAuthenticate {    
    /**
     * Logger object
     */
    Logger logger;

    /**
     * User list
     */
    protected List<User> users;

    /**
     * Default constructor
     * @param context
     */
    public SecurityManager(String hostId) {
        this.logger = Context.getHost(hostId).getLogger();
        this.users = Context.getHosts().getHost(hostId).<List<Map<String, Object>>>getUsers().stream().map(m -> new User(m)).collect(Collectors.toList());
    }

    @Override
    public User logout(String username) {
        User user = this.users.stream().filter(u -> u.getUsername().equals(username)).findFirst().orElse(null);
        if(user != null) {
            user.getSession().invalidate();
            this.users.remove(user);    
        }
        return null;
    }

    @Override
    public User login(String username, String password) throws HTTPException {
        User user = this.users.stream().filter(u -> u.getUsername().equals(username)).findAny().orElseThrow(() -> new HTTPException(RES_CODE.RES401, Context.getMessages().<String>getErrorMsg(25, username)));
        if(user != null && !user.getPassword().equals(password)) {
            throw new HTTPException(RES_CODE.RES401, Context.getMessages().<String>getErrorMsg(16, password));
        }
        return user;
    }

    @Override
    public void register(User user) throws HTTPException {
        if(this.users.stream().anyMatch(u -> u.getUsername().equals(user.getUsername()))) {
            throw new HTTPException(RES_CODE.RES401, Context.getMessages().<String>getErrorMsg(17, user.getUsername())); 
        }
        if(!Constants.PASSWORD_REGEX.matcher(user.getPassword()).matches()) {
            throw new HTTPException(RES_CODE.RES401, Context.getMessages().<String>getErrorMsg(18));
        }
        this.users.add(user);
        save(this.users);
    }

    /**
     * Login with Basic authetication string in request
     * @param authorization
     * @return
     */
    public synchronized User loginBasicAuth(String authorization) {
        if (authorization != null && authorization.trim().startsWith("Basic")) {
            String base64Credentials = authorization.trim().substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            final String[] values = credentials.split(":", 2);
            //System.out.println(values[0]+" "+values[1]);
            User user = login(values[0], values[1]);
            if(user == null) {
                HTTPException httpe = new HTTPException(RES_CODE.RES401, new HashMap<>(), "User( "+values[0]+" ) not found in server." );
                httpe.addHeader("WWW-Authenticate", "Basic");
                throw httpe;
            }
            this.logger.debug("User "+values[0]+" is login.");  
            return user;
        }
        HTTPException httpe = new HTTPException(RES_CODE.RES401, new HashMap<>(), "Auth information not found!!!" );                
        httpe.addHeader("WWW-Authenticate", "Basic");
        throw httpe;
    }

    /**
     * Get user object by specified user name
     * @param username
     * @return
     */
    public User getUserByName(String username) {
        return this.users.stream().filter(u -> u.getUsername().equals(username)).findAny().orElseThrow();
    }

    /**
     * Logout user
     * @param user
     * @return
     */
    public User logout(User user) {
        return logout(user.getUsername());
    }

    /**
     * Save user list
     * @param users
     * @throws HTTPException
     */
    public void save(List<User> users) throws HTTPException {
        List<Map<String, Object>> list = users.stream().map(u -> u.getUserMap()).collect(Collectors.toList());
        Context.getServer().setValue("server.users", list);
        Context.save(META.SERVER);
    }
}
