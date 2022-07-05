package org.chaostocosmos.leap.http;

import java.lang.reflect.Method;

public class ReflectionTest {
    

    public static void main(String[] args) throws Exception {
        String s = new String("");
        for(Method m : s.getClass().getMethods()) {
            System.out.println(m.getDeclaringClass().getName());
        }
    }
}
