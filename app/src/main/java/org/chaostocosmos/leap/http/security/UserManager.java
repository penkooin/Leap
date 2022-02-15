package org.chaostocosmos.leap.http.security;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.Constants;
import org.chaostocosmos.leap.http.Context;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.filters.IAuthenticate;
import org.chaostocosmos.leap.http.user.GRANT;
import org.chaostocosmos.leap.http.user.User;

/**
 * UserManager 
 * 
 * @author 9ins
 */
public class UserManager implements IAuthenticate {    
    /**
     * User list
     */
    protected List<User> users;

    /**
     * Default constructor
     * @param context
     */
    public UserManager() {
        this.users = ((List<?>)Context.getConfigValue("server.users"))
                                      .stream()
                                      .map(u -> (Map<?, ?>)u)
                                      .map(m -> new User(m.get("username")+"", 
                                                         m.get("password")+"", 
                                                         GRANT.valueOf(m.get("grant")+"")))
                                      .collect(Collectors.toList());
    }

    @Override
    public boolean signIn(String username, String password) throws WASException {
        User user = this.users.stream().filter(u -> u.getUsername().equals(username)).findAny().orElseThrow(() -> new WASException(MSG_TYPE.ERROR, 25, username));
        if(!user.getPassword().equals(password)) {
            throw new WASException(MSG_TYPE.ERROR, 26, password);
        }
        return true;
    }

    @Override
    public void signUp(User user) throws WASException {
        if(this.users.stream().anyMatch(u -> u.getUsername().equals(user.getUsername()))) {
            throw new WASException(MSG_TYPE.ERROR, 32, user.getUsername()); 
        }
        if(!Constants.PASSWORD_REGEX.matcher(user.getPassword()).matches()) {
            throw new WASException(MSG_TYPE.ERROR, 33);
        }
        this.users.add(user);
        save(this.users);
    }

    /**
     * Save user list
     * @param users
     * @throws WASException
     */
    public void save(List<User> users) throws WASException {
        List<Map<String, Object>> list = users.stream().map(u -> u.getUserMap()).collect(Collectors.toList());
        Context.getConfigValue("server.users", list);
        Context.save();
    }    
}
