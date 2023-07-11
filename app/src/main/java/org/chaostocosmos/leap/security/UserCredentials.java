package org.chaostocosmos.leap.security;

import java.util.Map;

import org.chaostocosmos.leap.enums.GRANT;
import org.chaostocosmos.leap.session.Session;

/**
 * UserCredentials
 * 
 * This object represents user credentials that have username, password, grant and session information.
 * Leap service autheticates user trying to login by this object's username
 * 
 * @author 9ins
 */
public class UserCredentials {

    String username;
    String password;
    String grant;
    String token;
    Session session;

    /**
     * Construct with user, password, grant
     * @param username
     * @param password
     * @param grant
     */
    public UserCredentials(String username, String password, String grant) {
        this.username = username;
        this.password = password;
        this.grant = grant;
    }

    /**
     * Constructs with user Map
     * @param userMap
     */
    public UserCredentials(Map<String, Object> userMap) {        
        this((String) userMap.get("username"), (String) userMap.get("password"), (String) userMap.get("grant"));
    }

    /**
     * Get user name
     * @return
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Get password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Get grant of user
     * @return
     */
    public GRANT getGrant() {
        return GRANT.valueOf(this.grant);
    }

    /**
     * Get credential token
     * @return
     */
    public String getToken() {
        return this.token;
    }

    /**
     * Set credential totken
     * @param token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Get session
     * @return
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * Set session
     * @param session
     */
    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * Get user Map
     * @return
     */
    public Map<String, Object> getUserCredentialsMap() {
        return Map.of("username", getUsername(), "password", getPassword(), "grant", getGrant(), "token", getToken(), "session", getSession());
    }

    @Override
    public String toString() {
        return "{" +
            " username='" + getUsername() + "'" +
            ", password='" + getPassword() + "'" +
            ", grant='" + getGrant() + "'" +
            ", token='" + getToken() + "'" +
            ", session='" + getSession() + "'" +
            "}";
    }
}
