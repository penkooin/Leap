package org.chaostocosmos.leap.security;

import org.chaostocosmos.leap.exception.LeapException;

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
     * @throws LeapException
     */
    public UserCredentials userCredentials(String username) throws LeapException;

    /**
     * Logout with username
     * @param username
     * @return
     * @throws LeapException
     */
    public boolean logout(String username) throws LeapException;
}
