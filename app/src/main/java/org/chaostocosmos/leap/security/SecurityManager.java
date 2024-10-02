package org.chaostocosmos.leap.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.chaostocosmos.leap.common.constant.Constants;
import org.chaostocosmos.leap.common.log.Logger;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.context.META;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.exception.LeapException;

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
     * Host instance
     */
    Host<?> host;

    /**
     * For blocking request attack (ip = timestemp)
     */
    private Map<String, Map<String, Long>> requestAttackBlockingMap = new ConcurrentHashMap<>();    

    /**
     * Default constructor
     * @param host
     */
    public SecurityManager(Host<?> host) {
        this.host = host;
        this.logger = this.host.getLogger();
    }

    /**
     * Get UserCredential List 
     * @return
     */
    private Stream<UserCredentials> getUserCredentialStream() {
        return this.host.getUsers().stream().map(m -> new UserCredentials(m));
    }

    /**
     * Logout
     * @param username
     * @return
     */
    public UserCredentials logout(String username) {
        UserCredentials user = getUserCredentialStream().filter(u -> u.getUsername().equals(username)).findFirst().orElse(null);
        if(user != null) {
            user.getSession().invalidate();
            return user;
        }
        return null;
    }

    /**
     * Login
     * @param username
     * @param password
     * @return
     * @throws LeapException
     */
    public UserCredentials login(String username, String password) throws LeapException {
        UserCredentials user = getUserCredentialStream().filter(u -> u.getUsername().equals(username)).findAny().orElseThrow(() -> new LeapException(HTTP.RES401, username));
        return user;
    }

    /**
     * Register UserCredential
     * @param user
     * @throws LeapException
     */
    public void register(UserCredentials user) throws LeapException {
        if(getUserCredentialStream().anyMatch(u -> u.getUsername().equals(user.getUsername()))) {
            throw new LeapException(HTTP.RES401, user.getUsername()); 
        }
        if(!Constants.PASSWORD_REGEX.matcher(user.getPassword()).matches()) {
            throw new LeapException(HTTP.RES401, user.getPassword());
        }
        List<UserCredentials> users = getUserCredentialStream().collect(Collectors.toList());
        users.add(user);
        save(users);
    }

    /**
     * Login with Basic authetication string in request
     * @param authorization
     * @return
     */
    public UserCredentials authenticate(List<?> authorization) {
        if (authorization != null && authorization != null) {
            //System.out.println(values[0]+" "+values[1]);
            String[] values = getUserPassword(authorization.get(0));
            if(values != null) {
                UserCredentials user = login(values[0], values[1]);
                this.logger.debug("==================================================");  
                this.logger.debug("User "+values[0]+" is login. "+values[1]);  
                this.logger.debug("==================================================");  
                return user;    
            }
        }
        return null;//new LeapException(HTTP.RES401, 28, authorization);
    }

    /**
     * Get user, password ordered by index of array
     * @param authorization
     * @return
     */
    public String[] getUserPassword(Object authorization) {
        if(authorization != null && authorization.toString().startsWith("Basic")) {
            String base64Credentials = authorization.toString().substring("Basic".length()).trim();
            if(!base64Credentials.equals("")) {
                byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                String[] values = credentials.split(":", 2);
                return values;                    
            }
        }
        return null;
    }

    /**
     * Get user object by specified user name
     * @param username
     * @return
     */
    public UserCredentials getUserByName(String username) {
        return getUserCredentialStream().filter(u -> u.getUsername().equals(username)).findAny().orElseThrow();
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
     * @throws LeapException
     */
    public void save(List<UserCredentials> users) throws LeapException {
        List<Map<String, Object>> list = users.stream().map(u -> u.getUserCredentialsMap()).collect(Collectors.toList());
        Context.get().server().setValue("server.users", list);
        Context.get().save(META.SERVER);
    }

    /**
     * Check too many request attack on short time period.
     * @param connection
     * @return
     * @throws IOException
     */
    public boolean checkRequestAttack(String ip, String url) throws IOException {
        if(requestAttackBlockingMap.containsKey(ip)) {
            Map<String, Long> map = requestAttackBlockingMap.get(ip);
            for(Map.Entry<String, Long> entry : map.entrySet()) {
                String preContext = entry.getKey(); 
                long preTimestemp = entry.getValue();
                if(url.equals(preContext) && System.currentTimeMillis() - preTimestemp < this.host.getRequestBlockingInterval()) {
                    requestAttackBlockingMap.remove(ip);
                    return true;
                    //throw new LeapException(HTTP.RES429, new Exception("You requested too many on short period!!!  URI: "+url));
                }
            }
            map.put(url, System.currentTimeMillis());
        } else {
            requestAttackBlockingMap.put(ip, new ConcurrentHashMap<String, Long>());
        }
        requestAttackBlockingMap.get(ip).put(url, System.currentTimeMillis());
        return true;
    }
}
