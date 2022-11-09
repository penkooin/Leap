package org.chaostocosmos.metadata;

/**
 * User
 */
public class User {
    
    @MetaField(expr = "hosts[0].users[i].username")
    String username;

    @MetaField(expr = "hosts[0].resources.streaming-buffer-size")
    Address address;

    @MetaField(expr = "hosts[0]")
    Class<?> clazz;

    @Override
    public String toString() {
        return "{" +
            " username='" + username + "'" +
            " address='" + address + "'" +
            "}";
    }    
}
