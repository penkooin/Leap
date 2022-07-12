package org.chaostocosmos.leap.http.context;

import java.util.Map;

/**
 * User
 * @author 9ins
 */
public class User {

    String username;
    String password;
    String grant;

    /**
     * Construct with user, password, grant
     * @param username
     * @param password
     * @param grant
     */
    public User(String username, String password, String grant) {
        this.username = username;
        this.password = password;
        this.grant = grant;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public GRANT getGrant() {
        return GRANT.valueOf(this.grant);
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
