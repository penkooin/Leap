package org.chaostocosmos.leap.http.security;

import org.chaostocosmos.leap.http.HTTPException;

/**
 * User authentication interface
 * 
 * @author 9ins
 */
public interface IAuthenticate {
    /**
     * Retrive UserCredentials object for authentication
     * @param username
     * @return
     * @throws HTTPException
     */
    public UserCredentials userCredentials(String username) throws HTTPException;

    /**
     * Logout with username
     * @param username
     * @return
     * @throws HTTPException
     */
    public boolean logout(String username) throws HTTPException;
}
