package org.chaostocosmos.metadata;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test; 

public class MetaInjectorTest {

    public static MetaStorage metaStorage = new MetaStorage(Paths.get("D:/0.github/Leap/config/"));

    @Test
    public static void testInject() throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, NoSuchFieldException {
        MetaTest obj = new MetaTest();
        obj = (MetaTest) MetaInjector.inject(metaStorage.getMetadata("hosts.json"), obj);
        System.out.println(obj.toString());
        List<User> users = obj.getUsers();
        System.out.println(users.get(0).username);
    }

    public static void save() throws IOException {
        Metadata metadata = metaStorage.getMetadata("hosts.yml");
        metadata.save(new File("D:/0.github/Leap/config/hosts.json"));
    }

    public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, NoSuchFieldException {
        testInject();
    }
}
