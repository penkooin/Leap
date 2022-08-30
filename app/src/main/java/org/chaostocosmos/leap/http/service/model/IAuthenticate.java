package org.chaostocosmos.leap.http.service.model;

import org.chaostocosmos.leap.http.HTTPException;
import org.chaostocosmos.leap.http.context.User;

/**
 * User authentication interface
 */
public interface IAuthenticate {
    /**
     * Login with username, password
     * @param username
     * @param password
     * @return
     * @throws HTTPException
     */
    public User login(String username, String password) throws HTTPException;

    /**
     * Logout with username
     * @param username
     * @return
     * @throws HTTPException
     */
    public User logout(String username) throws HTTPException;

    /**
     * Register User
     * @param user
     * @throws HTTPException
     */
    public void register(User user) throws HTTPException;
}
