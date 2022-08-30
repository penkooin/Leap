package org.chaostocosmos.leap.http.context;

import java.util.Map;

import org.chaostocosmos.leap.http.session.Session;

/**
 * User
 * @author 9ins
 */
public class User {

    String username;
    String password;
    String grant;
    Session session;

    /**
     * Constructs with user Map
     * @param userMap
     */
    public User(Map<String, Object> userMap) {        
        this((String) userMap.get("username"), (String) userMap.get("password"), (String) userMap.get("grant"));
    }

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
