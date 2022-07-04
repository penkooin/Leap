package org.chaostocosmos.leap.http.context;

import java.util.Map;

/**
 * User
 * @author 9ins
 */
public class User <T> extends Metadata<T> {

    String username;
    String password;
    GRANT grant;

    /**
     * Construct with user, password, grant
     * @param username
     * @param password
     * @param grant
     */
    public User(T userMap) {
        super(userMap);
    }

    public String getUsername() {
        return super.getValue("username");
    }

    public String getPassword() {
        return super.getValue("password");
    }

    public GRANT getGrant() {
        return GRANT.valueOf(super.getValue("grant"));
    }

    public Map<String, Object> getUserMap() {
        return Map.of("username", getUsername(), "password", getPassword(), "grant", getGrant());
    }

    @Override
    public String toString() {
        return "{" +
            " username='" + getUsername() + "'" +
            ", password='" + getPassword() + "'" +
            ", grant='" + getGrant() + "'" +
            "}";
    }    
}
