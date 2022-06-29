package org.chaostocosmos.leap.http.services.filters;

import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.context.User;

/**
 * User authentication interface
 */
public interface IAuthenticate {
    /**
     * Sign in
     * @param username
     * @param password
     * @return
     * @throws WASException
     */
    public boolean signIn(String username, String password) throws WASException;

    /**
     * Sign up
     * @param user
     * @throws WASException
     */
    public void signUp(User user) throws WASException;
}
