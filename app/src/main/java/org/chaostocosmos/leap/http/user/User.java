package org.chaostocosmos.leap.http.user;

import java.util.Map;

/**
 * User
 * @author 9ins
 */
public class User {

    String username;
    String password;
    GRANT grant;

    /**
     * Construct with user, password, grant
     * @param username
     * @param password
     * @param grant
     */
    public User(String username, String password, GRANT grant) {
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
        return this.grant;
    }

    public Map<String, Object> getUserMap() {
        return Map.of("username", this.username, "password", this.password, "grant", this.grant.name());
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
