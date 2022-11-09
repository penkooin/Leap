package org.chaostocosmos.metadata;

import java.util.List;

public class MetaTest {

    @MetaField(expr = "hosts[0].logs")
    String logs;

    @MetaField(expr = "hosts[0].users[0].username")
    String username;

    @MetaField(expr = "hosts[0].port")
    Integer port;

    @MetaField(expr = "hosts[0].users")
    List<User> users;

    public List<User> getUsers() {
        return this.users;
    }

    @Override
    public String toString() {
        return "{" +
            " logs='" + logs + "'" +
            ", username='" + username + "'" +
            ", port='" + port + "'" +
            ", users='" + users.toString() + "'" +
            "}";
    }

}
