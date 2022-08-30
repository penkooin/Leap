package org.chaostocosmos.leap.http.security;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.HTTPException;
import org.chaostocosmos.leap.http.common.Constants;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.META;
import org.chaostocosmos.leap.http.context.User;
import org.chaostocosmos.leap.http.enums.RES_CODE;

/**
 * UserManager 
 * 
 * @author 9ins
 */
public class SecurityManager {    
    /**
     * User list
     */
    protected List<User> users;

    /**
     * Default constructor
     * @param context
     */
    public SecurityManager(String hostId) {
        this.users = Context.getHosts().getHost(hostId).<List<Map<String, Object>>>getUsers().stream().map(m -> new User(m)).collect(Collectors.toList());
    }

    public User getUserByName(String username) {
        return this.users.stream().filter(u -> u.getUsername().equals(username)).findAny().orElseThrow();
    }

    public User logout(String username) {
        return null;
    }

    public User logout(User user) {
        return logout(user.getUsername());
    }

    public User signIn(String username, String password) {
        System.out.println(this.users.toString());
        User user = this.users.stream().filter(u -> u.getUsername().equals(username)).findAny().orElseThrow(() -> new HTTPException(RES_CODE.RES401, Context.getMessages().<String>getErrorMsg(25, username)));
        if(!user.getPassword().equals(password)) {
            throw new HTTPException(RES_CODE.RES401, Context.getMessages().<String>getErrorMsg(16, password));
        }
        return user;
    }

    public void signUp(User user) throws HTTPException {
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
