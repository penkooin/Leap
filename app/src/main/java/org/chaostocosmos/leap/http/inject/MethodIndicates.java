package org.chaostocosmos.leap.http.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.chaostocosmos.leap.http.enums.REQUEST;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodIndicates {
    /**
     * Request type
     * @return
     */
    REQUEST method();
    
    /**
     * Servelt mapped path of uri
     * @return
     */
    String path();
}

